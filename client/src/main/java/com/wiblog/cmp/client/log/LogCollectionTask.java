package com.wiblog.cmp.client.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 日志收集工作线程
 *
 * @author pwm
 */
public class LogCollectionTask implements Runnable {

    private final RabbitTemplate rabbitTemplate;

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
     * 共享注册信息
     */
    private LinkedHashMap<String, String> data;

    /**
     * 文件格式
     */
    private String charset = null;

    private static final Logger logger = LoggerFactory.getLogger(LogCollectionTask.class);

    public LogCollectionTask(String id, String path, long pos, InheritableThreadLocal<LinkedHashMap<String, String>> inheritableThreadLocal, RabbitTemplate rabbitTemplate) {
        this.id = id;
        this.path = path;
        this.pos = pos;
        this.rabbitTemplate = rabbitTemplate;
        this.data = inheritableThreadLocal.get();
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
        List<String> list = getFileLog();
        if (list.size() > 0) {
            list.forEach(e -> {
                rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_KEY, RabbitmqConfig.ROUTING_KEY, e);
            });
        }
    }

    /**
     * 从文件中获取最新日志的内容
     */
    private List<String> getFileLog() {
        File file = new File(this.path);
        if (file.exists()) {

            if (this.charset == null) {
                this.charset = FileUtils.getFileCharset(file);
            }

            List<String> list = new ArrayList<>();

            RandomAccessFile randomAccessFile;
            try {
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(this.pos);

                String tmp = "";
                while ((tmp = randomAccessFile.readLine()) != null) {
                    String str = new String(tmp.getBytes(StandardCharsets.ISO_8859_1), Charset.forName(this.charset));
                    if (!StringUtils.isEmpty(str)) {
                        list.add(str);
                    }
                }
                this.pos = randomAccessFile.getFilePointer();
                this.data.put(this.id, String.valueOf(this.pos));
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
