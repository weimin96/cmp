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

    private static final LinkedHashMap<String, String> sinceDbDate = new LinkedHashMap<>();


    /**
     * 读取sinceDb内容
     *
     * SinceDb格式
     * id pos
     */
    public static synchronized LinkedHashMap<String, String> getSinceDb() {
        if (sinceDbDate.size() > 0){
            return sinceDbDate;
        }
        try {
            Path file = Paths.get(sinceDbPath);
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            List<String> lines = Files.readAllLines(file);
            lines.forEach(str -> {
                if (!StringUtils.isEmpty(str)) {
                    String[] split = str.split(" ");
                    sinceDbDate.put(split[0], split[1]);
                }
            });

        } catch (IOException e) {
            logger.error("读取sinceDb异常", e);
        }
        return sinceDbDate;
    }

    public static void writeSinceDb(LinkedHashMap<String, String> sinceDb) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(sinceDbPath), true))) {
            for (Map.Entry<String, String> entry : sinceDb.entrySet()) {
                bufferedWriter.newLine();
                bufferedWriter.write(entry.getKey() + " " + entry.getValue());
            }
        } catch (IOException e) {
            logger.error("sinceDb写入异常", e);
        }

    }
}
