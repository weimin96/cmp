package com.wiblog.cmp.client.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * SinceDb操作工具
 *
 * @author panweimin
 * @create 2020-10-27 9:04
 */
public class SinceDbUtils {

    private static final Logger logger = LoggerFactory.getLogger(SinceDbUtils.class);

    /**
     * 注册表文件路径
     */
    public static String sinceDbPath;

    /**
     * sinceDbName文件名
     */
    public static final String sinceDbName = "cmp_sinceDb_";

    static {
        sinceDbPath = System.getProperty("java.io.tmpdir") +
                sinceDbName +
                DigestUtils.md5DigestAsHex(System.getProperty("user.dir").getBytes());
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.io.tmpdir"));
    }

    private static final LinkedHashMap<String, String> SINCE_DB_DATE = new LinkedHashMap<>();

    /**
     * 读写锁
     */
    private static final ReentrantReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();

    private static final Lock READ_LOCK = READ_WRITE_LOCK.readLock();
    private static final Lock WRITE_LOCK = READ_WRITE_LOCK.writeLock();


    /**
     * 读取sinceDb内容
     * <p>
     * SinceDb格式
     * id pos
     */
    public static LinkedHashMap<String, String> getSinceDb() {
        if (SINCE_DB_DATE.size() > 0) {
            return SINCE_DB_DATE;
        }
        READ_LOCK.lock();
        try {
            Path file = Paths.get(sinceDbPath);
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            List<String> lines = Files.readAllLines(file);
            lines.forEach(str -> {
                if (!StringUtils.isEmpty(str)) {
                    String[] split = str.split(" ");
                    SINCE_DB_DATE.put(split[0], split[1]);
                }
            });

        } catch (IOException e) {
            logger.error("读取sinceDb异常", e);
        } finally {
            READ_LOCK.unlock();
        }
        return SINCE_DB_DATE;
    }

    public static void writeSinceDb(LinkedHashMap<String, String> sinceDb) {
        WRITE_LOCK.lock();
        // append=false时会先清空文件内容
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(sinceDbPath), false))) {
            for (Map.Entry<String, String> entry : sinceDb.entrySet()) {
                bufferedWriter.newLine();
                bufferedWriter.write(entry.getKey() + " " + entry.getValue());
            }
        } catch (IOException e) {
            logger.error("sinceDb写入异常", e);
        } finally {
            WRITE_LOCK.unlock();
        }

    }
}
