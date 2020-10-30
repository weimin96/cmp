package com.wiblog.cmp.client.log;

import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author pwm
 * @date 2020/10/26
 */
public class FileUtils {

    public static String getFileCharset(File sourceFile) {
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

                label74:
                do {
                    do {
                        if ((read = bis.read()) == -1) {
                            break label74;
                        }

                        if (read >= 240 || 128 <= read && read <= 191) {
                            break label74;
                        }

                        if (192 <= read && read <= 223) {
                            read = bis.read();
                            continue label74;
                        }
                    } while (224 > read);

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

    public static boolean isFirstLine(String str){
        String regex = "^\\[.*";
        return str.matches(regex);
    }

    public static String removeWrap(String str){
        String lineSeparator = System.lineSeparator();
        if (str.endsWith(lineSeparator)){
            str = str.substring(0,str.length()-lineSeparator.length());
        }
        return str;
    }


    public static void main(String[] args) {
        String regex = "^\\[.*";
        String a = "INFO][2020-10-29 14:06][sundun.ZfptServiceApplication]Started ZfptServiceApplication in 41.998 seconds (JVM running for 49.185)";
        System.out.println(a.matches(regex));
    }
}
