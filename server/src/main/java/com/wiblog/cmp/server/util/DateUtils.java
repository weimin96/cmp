package com.wiblog.cmp.server.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author panweimin
 * @create 2020-10-29 19:52
 */
public class DateUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSS";
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL = new ThreadLocal<>();

    public static SimpleDateFormat getDateFormat() {
        SimpleDateFormat df = THREAD_LOCAL.get();
        if (df == null) {
            df = new SimpleDateFormat(DATE_FORMAT);
            THREAD_LOCAL.set(df);
        }
        return df;
    }

    public static String formatDate(Date date) {
        return getDateFormat().format(date);
    }

    public static Date parse(String strDate) {
        try {
            return getDateFormat().parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
