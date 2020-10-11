package com.wiblog.cmp.client.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 日志收集工作线程
 */
public class LogCollectionTask implements Runnable {

    private RabbitTemplate rabbitTemplate;

    private String logFilePath;

    /**
     * 定位
     */
    private long pointer = 0L;

    /**
     * 文件格式
     */
    private String charset = null;

    private static final Logger logger = LoggerFactory.getLogger(LogCollectionTask.class);

    public LogCollectionTask(String logFilePath,RabbitTemplate rabbitTemplate) {
        this.logFilePath = logFilePath;
        this.rabbitTemplate = rabbitTemplate;
    }

    private static String getFilecharset(File sourceFile) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];

        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                return charset;
            }

            if (first3Bytes[0] == -1 && first3Bytes[1] == -2) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == -2 && first3Bytes[1] == -1) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == -17 && first3Bytes[1] == -69 && first3Bytes[2] == -65) {
                charset = "UTF-8";
                checked = true;
            }

            bis.reset();
            if (!checked) {
                int var6 = 0;

                label74:
                do {
                    do {
                        if ((read = bis.read()) == -1) {
                            break label74;
                        }

                        ++var6;
                        if (read >= 240 || 128 <= read && read <= 191) {
                            break label74;
                        }

                        if (192 <= read && read <= 223) {
                            read = bis.read();
                            continue label74;
                        }
                    } while (224 > read || read > 239);

                    read = bis.read();
                    if (128 <= read && read <= 191) {
                        read = bis.read();
                        if (128 <= read && read <= 191) {
                            charset = "UTF-8";
                        }
                    }
                    break;
                } while (128 <= read && read <= 191);
            }

            bis.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return charset;
    }

    @Override
    public void run() {
        try {
            logger.info("开始日志收集");
            this.work();
        } catch (Exception e) {
            logger.error("日志收集异常", e);
        }
    }

    private void work() throws Exception {
        List<String> list = getLog();
        if (list.size()>0){
            list.forEach(e->{
                rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_KEY, RabbitmqConfig.ROUTING_KEY,e);
            });
        }

    }

    private List<String> getLog() {
        File file = this.getLogFile();
        if (file != null && file.exists()){

            if (this.charset == null){
                this.charset = getFilecharset(file);
            }

            List list = new ArrayList();

            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(this.pointer);

                String tmp = "";
                while ((tmp = randomAccessFile.readLine())!= null){
                    String str = new String(tmp.getBytes("ISO-8859-1"), Charset.forName(this.charset));
                    if (!StringUtils.isEmpty(str)){
                        list.add(str);
                    }
                }
                this.pointer = randomAccessFile.getFilePointer();
                randomAccessFile.close();
                return list;
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }

        }else{
            return Collections.emptyList();
        }
    }

    private File getLogFile(){
        try {
            if (!StringUtils.isEmpty(this.logFilePath)){
                return new File(this.logFilePath);
            }
        }catch (Exception e){
            logger.error("打开日志失败",e);
        }
        return null;
    }
}
