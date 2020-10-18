package com.wiblog.cmp.client.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 日志收集工作线程
 */
public class LogCollectionTask implements Runnable {

    private RabbitTemplate rabbitTemplate;

    /**
     * 日志文件目录
     */
    private String logDir;

    /**
     * 最新日志文件路径
     */
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

    public LogCollectionTask(String logDir, RabbitTemplate rabbitTemplate) {
        this.logDir = logDir;
        this.rabbitTemplate = rabbitTemplate;
    }

    private static String getFileCharset(File sourceFile) {
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
        List<String> list = getFileLog();
        if (list.size()>0){
            list.forEach(e->{
                rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_KEY, RabbitmqConfig.ROUTING_KEY,e);
            });
        }

    }

    /**
     * 从文件中获取最新日志的内容
     */
    private List<String> getFileLog() {
        File file = this.getLogFile();
        if (file != null && file.exists()){

            if (this.charset == null){
                this.charset = getFileCharset(file);
            }

            List<String> list = new ArrayList<>();

            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(this.pointer);

                String tmp = "";
                while ((tmp = randomAccessFile.readLine())!= null){
                    String str = new String(tmp.getBytes(StandardCharsets.ISO_8859_1), Charset.forName(this.charset));
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

    /**
     * 获取最新的日志文件
     * @return File
     */
    private File getLogFile(){
        try {
            // 直接从缓存路径获取
            if (!StringUtils.isEmpty(this.logFilePath)){
                return new File(this.logFilePath);
            }
            // 存在文件夹 读取日志文件
            if (!StringUtils.isEmpty(this.logDir)){
                String fileName;
                fileName = getLatestFile(this.logDir);
                if (!StringUtils.isEmpty(fileName)){
                    this.logFilePath = this.logDir+"/"+fileName;
                    return new File(this.logFilePath);
                }
            }

        }catch (Exception e){
            logger.error("打开日志失败",e);
        }
        return null;
    }

    /**
     * 获取最新的日志文件
     * @param p
     * @return
     */
    public String getLatestFile(String p) {
        File path = new File(p);
        File[] files = path.listFiles();
        if (files != null && files.length != 0) {
            List<File> fileList = new ArrayList();
            File[] var4 = files;
            int var5 = files.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                File file = var4[var6];
                if ((file.getName().endsWith(".out") || file.getName().endsWith(".log")) && file.getName().startsWith("log")) {
                    fileList.add(file);
                }
            }

            if (fileList.size() == 0) {
                return null;
            } else {
                files = (File[])fileList.toArray(new File[fileList.size()]);
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File file1, File file2) {
                        return (int)(file2.lastModified() - file1.lastModified());
                    }
                });
                return files[0].getName();
            }
        } else {
            return null;
        }
    }
}
