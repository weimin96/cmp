package com.wiblog.cmp.client.log;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wiblog.cmp.common.bean.InstanceInfo;
import com.wiblog.cmp.common.constant.CmpConstant;
import com.wiblog.cmp.common.logger.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * <p>日志文件夹监听</p>
 * <p>
 * 扫描日志文件夹，每隔scanFrequency秒检查目录中日志文件的变化情况，有变化监听句柄才会重新启动，
 * ignoreOlder，将忽略在指定时间范围之前修改的任何文件
 * </p>
 * 文件路径+文件名作为文件标识，inode是linux特有标识，windows下不可用
 *
 * @author pwm
 */
@Component
public class LogScannerTask {

    private static final Logger logger = LoggerFactory.getLogger(LogScannerTask.class);

    public String logDir;

    /**
     * 扫描频率 秒
     */
    private int scanFrequency = 15;

    /**
     * 读取文件频率
     */
    private int statInterval = 5;

    /**
     * 超过这个值的时间内没有更新内容，就关闭监听它的文件句柄 秒
     */
    private int closeInactive = 60;

    /**
     * 文件的最后更新时间超过这个值就忽略 秒
     */
    private int ignoreOlder = 86400;

    private final ScheduledExecutorService scheduler;

    private final ScheduledFuture<?> scannerFuture;

    private final LinkedHashMap<String, String> sinceDb;

    private final RabbitTemplate rabbitTemplate;

    private final InstanceInfo instanceInfo;

    /**
     * 正在监听文件的线程
     */
    public static final ConcurrentHashMap<String, Object> WATCHER = new ConcurrentHashMap<>();

    public LogScannerTask(RabbitTemplate rabbitTemplate, LogConfigProperties properties, InstanceInfo instanceInfo) {
        logger.info("初始化日志收集客户端");
        this.logDir = properties.getLogDir();
        if (this.logDir == null) {
            logger.error("[cmp.log.logDir]未指定日志文件夹");
        }

        // 读取注册表文件
        this.sinceDb = SinceDbUtils.getSinceDb();

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("cmp-log-pool-%d")
                .setDaemon(true).build();
        this.scheduler = new ScheduledThreadPoolExecutor(5, threadFactory);

        this.scannerFuture = this.scheduler.scheduleWithFixedDelay(new LogScannerRunnable(), 0, scanFrequency, TimeUnit.SECONDS);
        this.rabbitTemplate = rabbitTemplate;
        this.instanceInfo = instanceInfo;
    }

    /**
     * 监听文件列表
     * 1、新增文件-加入 2、文件被修改- 3、
     */
    class LogScannerRunnable implements Runnable {

        @Override
        public void run() {
            this.work();
        }

        private void work() {
            // 更新sinceDb内容
            SinceDbUtils.writeSinceDb(sinceDb);
            long nowTime = System.currentTimeMillis();
            File root = new File(logDir);
            // 层级遍历文件树
            LinkedList<File> files = new LinkedList<>();
            files.addLast(root);
            while (files.size() > 0) {
                File childFile = files.removeFirst();
                File[] childFiles = childFile.listFiles();
                if (childFiles == null) {
                    logger.error("没有扫描到日志文件{}", logDir);
                    // 关闭定时器
                    scannerFuture.cancel(true);
                    return;
                }
                for (File file : childFiles) {
                    if (file.isDirectory()) {
                        files.addLast(file);
                        continue;
                    }
                    // 不是out/log结尾忽略
                    if (!file.getName().endsWith(".out") && !file.getName().endsWith(".log")) {
                        continue;
                    }
                    String path = file.getAbsolutePath();
                    // 文件唯一标识
                    String id = DigestUtils.md5DigestAsHex(path.getBytes());
                    long lastModifiedTime = file.lastModified();
                    // 超过一定时间文件没有更新 则关闭文件句柄
                    if (lastModifiedTime + closeInactive < nowTime) {
                        // 超过时间范围的忽略
                        if (lastModifiedTime + ignoreOlder < nowTime) {
                            continue;
                        }
                        ScheduledFuture<?> future = (ScheduledFuture<?>) WATCHER.remove(id);
                        if (future != null) {
                            future.cancel(true);
                        }
                    }


                    // 获取行号
                    String posStr = sinceDb.get(id);
                    long pos = 0;
                    if (!StringUtils.isEmpty(posStr)) {
                        pos = Long.parseLong(posStr);
                    }
                    // 放入sinceDb等待下次更新
                    sinceDb.putIfAbsent(id, String.valueOf(pos));
                    // 如果该文件没有被监听
                    if (!WATCHER.containsKey(id)) {
                        // 启动文件监听器
                        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(new LogCollectionTask(id, path, pos),
                                0L, statInterval, TimeUnit.SECONDS);
                        WATCHER.put(id, future);
                    }
                }
            }
        }
    }

    /**
     * 日志发送工作线程
     */
    class LogCollectionTask implements Runnable {

        /**
         * 日志文件id
         */
        private final String id;

        /**
         * 日志文件路径
         */
        private final String path;

        /**
         * 定位
         */
        private long pos;

        /**
         * 行号
         */
        private long row;

        /**
         * 文件格式
         */
        private String charset = null;

        public LogCollectionTask(String id, String path, long pos) {
            this.id = id;
            this.path = path;
            this.pos = pos;
        }

        @Override
        public void run() {
            try {
                this.work();
            } catch (Exception e) {
                logger.error("日志收集异常", e);
            }
        }

        private void work() throws Exception {
            logger.info("开始日志收集");
            List<LogMessage> list = getFileLog();
            if (list.size() > 0) {
                list.forEach(e -> {
                    rabbitTemplate.convertAndSend(CmpConstant.Logger.EXCHANGE_KEY, CmpConstant.Logger.ROUTING_KEY, JSONObject.toJSONString(e));
                });
            }
        }

        /**
         * 从文件中获取最新日志的内容
         */
        private List<LogMessage> getFileLog() {
            File file = new File(this.path);
            if (file.exists()) {

                if (this.charset == null) {
                    this.charset = FileUtils.getFileCharset(file);
                }

                List<LogMessage> list = new ArrayList<>();

                RandomAccessFile randomAccessFile;
                try {
                    randomAccessFile = new RandomAccessFile(file, "rw");
                    randomAccessFile.seek(this.pos);

                    String tmp;
                    while ((tmp = randomAccessFile.readLine()) != null) {
                        String str = new String(tmp.getBytes(StandardCharsets.ISO_8859_1), Charset.forName(this.charset));
                        if (!StringUtils.isEmpty(str)) {
                            // 不是首行合并到上一行
                            if (!FileUtils.isFirstLine(str) && list.size() > 0) {
                                LogMessage logMessage = list.get(list.size() - 1);
                                logMessage.str(logMessage.getStr() + System.lineSeparator() + str);
                            } else {
                                LogMessage logMessage = buildMessage(str);
                                list.add(logMessage);
                            }

                        }
                    }
                    this.pos = randomAccessFile.getFilePointer();
                    sinceDb.put(this.id, String.valueOf(this.pos));
                    randomAccessFile.close();
                    return list;
                } catch (IOException e) {
                    logger.error("读取日志文件异常", e);
                    return Collections.emptyList();
                }

            } else {
                return Collections.emptyList();
            }
        }
    }

    private LogMessage buildMessage(String str) {
        return LogMessage.builder()
                .str(str)
                .instanceId(this.instanceInfo.getInstanceId())
                .appName(this.instanceInfo.getAppName())
                .build();
    }
}
