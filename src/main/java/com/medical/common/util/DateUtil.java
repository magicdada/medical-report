package com.medical.common.util;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 日期工具类
 *
 * @author wangda
 * @since 2026/08/11
 */
public class DateUtil {

    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd";
    public static final String MONTH_FORMAT = "yyyy-MM";
    public static final String MONTH_LABEL_FORMAT = "MMM";

    /**
     * 获取最近N个月的月份标签和key
     *
     * @param months 月份数
     * @return 月份列表，每项包含 key(yyyy-MM) 和 label(MMM)
     */
    public static List<Map<String, String>> getRecentMonths(int months) {
        List<Map<String, String>> result = new ArrayList<>();
        SimpleDateFormat keyFormat = new SimpleDateFormat(MONTH_FORMAT);
        SimpleDateFormat labelFormat = new SimpleDateFormat(MONTH_LABEL_FORMAT, Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();

        for (int i = months - 1; i >= 0; i--) {
            Calendar month = (Calendar) cal.clone();
            month.add(Calendar.MONTH, -i);

            Map<String, String> item = new HashMap<>(2);
            item.put("key", keyFormat.format(month.getTime()));
            item.put("label", labelFormat.format(month.getTime()));
            result.add(item);
        }
        return result;
    }

    /**
     * 获取日期的月份key（yyyy-MM）
     *
     * @param date 日期
     * @return 月份key
     */
    public static String getMonthKey(Date date) {
        return new SimpleDateFormat(MONTH_FORMAT).format(date);
    }

    /**
     * 格式化日期
     *
     * @param date    日期
     * @param pattern 格式
     * @return 格式化后的字符串
     */
    public static String toString(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 格式化日期（默认格式）
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String toString(Date date) {
        return toString(date, STANDARD_FORMAT);
    }
}
