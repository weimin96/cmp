package com.wiblog.cmp.client.log;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;

/**
 * 日志文件夹监听
 * <p>
 * 扫描日志文件夹，每隔scanFrequency秒检查目录中日志文件的变化情况，有变化监听句柄才会重新启动，
 * ignoreOlder，将忽略在指定时间范围之前修改的任何文件
 * <p>
 * 文件路径+文件名作为文件标识，inode是linux特有标识，windows下不可用
 *
 * @author pwm
 */
@Component
public class LogScannerTask {

    private static final Logger logger = LoggerFactory.getLogger(LogScannerTask.class);

    @Value("${cmp.log.logDir:/}")
    public String logDir;

    /**
     * 扫描频率 秒
     */
    private int scanFrequency = 15;

    /**
     * 读取文件频率
     */
    private int readFrequency = 10;

    /**
     * 文件的最后更新时间超过这个值就忽略 秒
     */
    private int ignoreOlder = 86400;

    private final ScheduledExecutorService scheduler;

    private final ScheduledFuture<?> scannerFuture;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 正在监听文件的线程
     */
    public static final ConcurrentHashMap<String, Object> WATCHER = new ConcurrentHashMap<>();

    public LogScannerTask() {

        // 读取注册表文件
        LinkedHashMap<String, String> sinceDb = SinceDbUtils.getSinceDb();
        inheritableThreadLocal.set(sinceDb);

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("cmp-log-pool-%d")
                .setDaemon(true).build();
        this.scheduler = new ScheduledThreadPoolExecutor(5, threadFactory);

        this.scannerFuture = this.scheduler.scheduleWithFixedDelay(new LogScannerRunnable(), 0, scanFrequency, TimeUnit.SECONDS);
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
            // TODO 心跳检测
            logger.info("开始监听日志文件夹");
            // 更新sinceDb内容
            LinkedHashMap<String, String> sinceDb = inheritableThreadLocal.get();
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
                    // 超过时间范围的忽略
                    long lastModifiedTime = file.lastModified();
                    if (lastModifiedTime + ignoreOlder < nowTime) {
                        continue;
                    }
                    String path = file.getAbsolutePath();
                    // 文件唯一标识
                    String id = DigestUtils.md5DigestAsHex(path.getBytes());
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
                        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(new LogCollectionTask(id,path, pos,inheritableThreadLocal,rabbitTemplate),
                                0L, readFrequency, TimeUnit.SECONDS);
                        WATCHER.put(id, future);
                    }
                }
            }
        }


    }

    public static void main(String[] args) {
        int pos = Integer.parseInt(null);
    }
}
