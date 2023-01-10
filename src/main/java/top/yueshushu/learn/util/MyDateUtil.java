package top.yueshushu.learn.util;

import cn.hutool.core.date.DateUtil;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * @ClassName:MyDateUtil
 * @Description TODO
 * @Author 岳建立
 * @Date 2021/11/12 22:14
 * @Version 1.0
 **/
public class MyDateUtil {
    private static LocalTime MORNING_START_TIME = LocalTime.parse("09:20:00");
    private static LocalTime MORNING_END_TIME = LocalTime.parse("11:30:05");

    private static LocalTime AFTERNOON_START_TIME = LocalTime.parse("13:00:00");
    private static LocalTime AFTERNOON_END_TIME = LocalTime.parse("15:00:05");

    /**
     * 当前时间是否在下午3点之后
     *
     * @return
     */
    public static boolean after15Hour() {
        Date now = DateUtil.date();
        //组装一个下午3点的时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        Date hour15 = calendar.getTime();
        if(now.before(hour15)){
            return false;
        }
        return true;
    }
    /**
     * 当前时间是否在下午3点之后
     * @return
     */
    public static boolean before930(){
        Date now= DateUtil.date();
        //组装一个下午3点的时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        Date hour930 = calendar.getTime();
        if (now.before(hour930)) {
            return true;
        }
        return false;
    }


    /**
     * 是否是 9点 20到 11:30 的时间
     */
    public static boolean isMorning() {
        // 进行延迟， 如果时间在 14:59 之后，则睡眠 1分钟。
        LocalTime now = LocalTime.now();
        if (now.isAfter(MORNING_START_TIME) && now.isBefore(MORNING_END_TIME)) {
            return true;
        }
        return false;
    }

    /**
     * 是否是 13 点 到 15:00 的时间
     */
    public static boolean isAfternoon() {
        // 进行延迟， 如果时间在 14:59 之后，则睡眠 1分钟。
        LocalTime now = LocalTime.now();
        if (now.isAfter(AFTERNOON_START_TIME) && now.isBefore(AFTERNOON_END_TIME)) {
            return true;
        }
        return false;
    }


    /**
     * 当前时间是否 9点半到 15点之间
     *
     * @return
     */
    public static boolean isWorkingTime() {
        return isMorning() || isAfternoon();
    }

    public static void main(String[] args) {
        // System.out.println(after15Hour());
        System.out.println(convertToTodayDate(null, "112201"));
    }

    public static Date convertToTodayDate(String dateStr, String timeStr) {
        // String --> LocalDate
        LocalDate localDate = StringUtils.hasText(dateStr) ? LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd")) : LocalDate.now();
        // String --> LocalTime
        LocalTime localTime = StringUtils.hasText(timeStr) ? LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HHmmss")) : LocalTime.now();
        return Date.from(LocalDateTime.of(localDate, localTime).atZone(ZoneId.systemDefault()).toInstant());
    }
}
