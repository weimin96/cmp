package com.wiblog.cmp.server.util;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pwm
 * @date 2020/9/13
 */
public class StatusInfo {

    private Map<String, String> info = new HashMap<>();

    public static final class Builder {

        private StatusInfo result;

        private Builder() {
            result = new StatusInfo();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public StatusInfo build() {

            Runtime runtime = Runtime.getRuntime();
            int totalMem = (int) (runtime.totalMemory() / 1048576);
            int freeMem = (int) (runtime.freeMemory() / 1048576);
            int usedPercent = (int) (((float) totalMem - freeMem) / (totalMem) * 100.0);

            result.info.put("服务运行时间", getUpTime());
            result.info.put("cpu核心数", String.valueOf(runtime.availableProcessors()));
            result.info.put("总内存", totalMem + "mb");
            result.info.put("内存占用", (totalMem - freeMem) + "mb" + " (" + usedPercent + "%)");

            return result;
        }
    }

    public Map<String, String> getInfo() {
        return info;
    }

    public static String getUpTime() {
        long diff = ManagementFactory.getRuntimeMXBean().getUptime();
        diff /= 1000 * 60;
        long minutes = diff % 60;
        diff /= 60;
        long hours = diff % 24;
        diff /= 24;
        long days = diff;
        StringBuilder buf = new StringBuilder();
        if (days == 1) {
            buf.append("1 day ");
        } else if (days > 1) {
            buf.append(Long.valueOf(days).toString()).append(" days ");
        }
        DecimalFormat format = new DecimalFormat();
        format.setMinimumIntegerDigits(2);
        buf.append(format.format(hours)).append(":")
                .append(format.format(minutes));
        return buf.toString();
    }
}
