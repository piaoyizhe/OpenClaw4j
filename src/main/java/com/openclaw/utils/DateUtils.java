package com.openclaw.utils;


import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class DateUtils {
    public static String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static String HH_mm = "HH:mm";
    public static String HH_mm_ss = "HH:mm:ss";
    public static String yyyy_MM_dd = "yyyy-MM-dd";

    public static void main(String[] args) {
        System.out.println(DateUtils.StringToLong("yyyy-MM-dd HH:mm:ss", "2021-05-28 22:00:00"));
        System.out.println(convertMillisToDHMS(578307));
    }

    /*--------------------------------------以下是获取时间----------------------------------------*/

    /**
     * getLong
     *
     * @return
     */
    public static long getLong() {
        return new Date().getTime();
    }

    /**
     * getDate
     *
     * @return
     */
    public static Date getDate() {
        String DateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        return new Date(DateTime);
    }

    /**
     * getString
     *
     * @return
     */
    public static String getString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    /**
     * 往前推几分钟
     *
     * @param num
     * @return
     */
    public static String getCurrentBackupTime(int num) {
        long current = new Date().getTime();
        long datel = current - num * 60 * 1000;
        return longToDateStr(datel);
    }

    public static Date getCurrentBackupTime(Date date, int num) {
        long current = date.getTime() - num * 60 * 1000;
        return new Date(current);
    }

    public static String longToDateStr(long time) {
        Date dt = new Date(time);
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
        return dateTime;
    }

    public static String longToDateStr(long time, String format) {
        Date dt = new Date(time);
        String dateTime = new SimpleDateFormat(format).format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
        return dateTime;
    }

    /**
     * getTimestamp
     *
     * @return
     */
    public static Timestamp getTimestamp() {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        try {
            ts = Timestamp.valueOf(nowTime);
            System.out.println(ts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ts;
    }

    /*--------------------------------------以下是toDate----------------------------------------*/

    /**
     * toDate
     *
     * @param s
     * @return
     */
    public static Date toDate(String s) {
        Date date = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = formatter.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * toDate
     *
     * @param l
     * @return
     */
    public static Date toDate(long l) {
        return new Date(l);
    }

    /**
     * toDate
     *
     * @param timestamp
     * @return
     */
    public static Date toTimestamp(Timestamp timestamp) {
        Date date = new Date();
        try {
            date = timestamp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String dateToString(Date date, String... format) {
        if (date == null)
            return "";

        SimpleDateFormat sdf = null;
        if (null != format && format.length > 0)
            sdf = new SimpleDateFormat(format[0]);
        else
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static long dateToLong(Date date) {
        return date.getTime();
    }

    /*--------------------------------------以下是toTimestamp----------------------------------------*/

    /**
     * ToTimestamp
     *
     * @param tsStr
     * @return
     */
    public static Timestamp ToTimestamp(String tsStr) {
        // String tsStr = "2011-05-09 11:49:45"; 必须为这种格式才能转换成 Timestamp
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        try {
            ts = Timestamp.valueOf(tsStr);
            System.out.println(ts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ts;
    }

    /**
     * ToTimestamp
     *
     * @param s
     * @return
     */
    public static Timestamp ToTimestamp(long s) {
        Timestamp t = new Timestamp(s);
        return t;
    }

    /**
     * ToTimestamp
     *
     * @param date
     * @return
     */
    public static Timestamp ToTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     * 获取模板时间 示例:yyyy-MM-dd HH:mm:ss
     *
     * @param format
     * @return
     */
    public static String getFormat(String format) {
        String nowTime = new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
        return nowTime;
    }

    /**
     * 获取模板时间 示例:yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static long getFormatLong(String format) {
        String nowTime = new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
        return StringToLong(format, nowTime);
    }

    /**
     * 字符串转换时间
     *
     * @param format 示例:"yyyy-MM-dd HH:mm:ss"
     * @param time   "2004-03-26 13:31:40"
     * @return
     */
    public static long StringToLong(String format, String time) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = null;
        long longTime = 0;
        try {
            date = dateFormat.parse(time);
            longTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return longTime;
    }

    /**
     * 获取前一天日期
     *
     * @return
     */
    public static String getYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        return yesterday;
    }

    /**
     * 获取后一天日期
     *
     * @return
     */
    public static String getTomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, +1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        return yesterday;
    }


    public static int getWeekOfYear(String currentDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(currentDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.setTime(date);
            return calendar.get(Calendar.WEEK_OF_YEAR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getCurrentWeekOfYear() {
        try {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.setTime(date);
            return calendar.get(Calendar.WEEK_OF_YEAR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @param time long的字符串
     * @return
     */
    public static String toString(String time) {
        // 格式 24小时制：2016-07-06 09:39:58
        DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // HH表示24小时制；
        return dFormat.format(toDate(Long.valueOf(time)));
    }

    /**
     * @param time long的字符串
     * @return
     */
    public static String toString(String time, String format) {
        // 格式 24小时制：2016-07-06 09:39:58
        DateFormat dFormat = new SimpleDateFormat(format); // HH表示24小时制；
        return dFormat.format(toDate(Long.valueOf(time)));
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static String getCurrentTime(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    public static Timestamp getCrruentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }


    /**
     * 判断一个时间是否在某一个时间段内
     *
     * @param targetDate 目标时间
     * @param beginDate  时间段的开始时间
     * @param endDate    时间段的结束时间
     * @return
     */
    public static boolean isBetween(Date targetDate, Date beginDate,
                                    Date endDate) {
        boolean temp = false;
        long target = targetDate.getTime();
        long begin = beginDate.getTime();
        long end = endDate.getTime();
        if (target >= begin && target <= end) {// 需要考虑在考勤中 时间 分钟以后的秒跟毫秒忽略
            temp = true;
        }
        return temp;
    }

    public static String DateToString(Date date, String format) {
        if (date == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 当前日期往前推天数,如需要往后填写负数
     *
     * @param dayNum
     * @return
     */
    public static String getCurrentBackup(int dayNum) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -(dayNum));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(calendar.getTime());
    }

    /**
     * 某个日期往前推天数,如需要往后填写负数
     *
     * @param date
     * @param dayNum
     * @return
     */
    public static String getDateBackup(Date date, int dayNum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -(dayNum));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(calendar.getTime());
    }

    public static Date getDateBackup2(Date date, int dayNum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -(dayNum));
        return calendar.getTime();
    }

    public static String getDateBackup(Date date, int dayNum, String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -(dayNum));
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(calendar.getTime());
    }

    /**
     * 将毫秒时间转换为天、小时、分钟和秒格式化字符串
     *
     * 2025-07-14 添加
     * @param millis 需要转换的毫秒数
     * @return 格式化后的字符串（如：0天 0时 9分 38秒）
     */
    /**
     * 将毫秒转换为格式化时间字符串，省略为零的单位
     *
     * @param millis 需要转换的毫秒数
     * @return 格式化后的字符串（如："9分38秒" 或 "1天10时"）
     */
    public static String convertMillisToDHMS(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("时间不能为负数");
        }
        List<String> parts = new ArrayList<>();
        // 计算天数并判断是否添加
        long days = millis / (24 * 60 * 60 * 1000);
        if (days > 0) {
            parts.add(days + "天");
        }
        millis %= (24 * 60 * 60 * 1000);

        // 计算小时并判断是否添加
        long hours = millis / (60 * 60 * 1000);
        if (hours > 0) {
            parts.add(hours + "时");
        }
        millis %= (60 * 60 * 1000);

        // 计算分钟并判断是否添加
        long minutes = millis / (60 * 1000);
        if (minutes > 0) {
            parts.add(minutes + "分");
        }
        millis %= (60 * 1000);

        // 计算秒并判断是否添加
        long seconds = millis / 1000;
        if (seconds > 0 || parts.isEmpty()) { // 若其他单位均无，则至少显示秒
            parts.add(seconds + "秒");
        }
        return String.join("", parts);
    }
}
