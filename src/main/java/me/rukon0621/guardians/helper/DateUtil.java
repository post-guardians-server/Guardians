package me.rukon0621.guardians.helper;

import java.time.DayOfWeek;
import java.util.Date;

public class DateUtil {


    //새로운 Date 시간을 만듬
    public static Date makeTime(long second) {
        return new Date(second * 1000);
    }

    public static long now() {
        return new Date().getTime();
    }

    public static String formatDate(long second) {
        if(second<60) {
            return String.format("%d초", second);
        }
        else if (second<3600) {
            return String.format("%d분 %d초", second/60, second%60);
        }
        else if (second<86400) {
            return String.format("%d시간 %d분 %d초", second/3600, second%3600/60, second%3600%60);
        }
        else {
            return String.format("%d일 %d시간 %d분 %d초", second/86400, (second/3600)%24, second%3600/60, second%3600%60);
        }
    }

    public static String getKoreanWeekDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

    public static Date makeTime(long minutes, long second) {
        return new Date(minutes * 1000 * 60 + second * 1000);
    }
    public static Date makeTime(long hours, long minutes, int second) {
        return new Date(hours*1000*3600 + minutes * 1000 * 60 + second * 1000);
    }
    public static Date makeTime(long days, long hours, long minutes, long second) {
        return new Date(days*1000*86400 + hours*1000*3600 + minutes * 1000 * 60 + second * 1000);
    }

    //두 시간의 차이를 ms단위로 반환
    public static Long getDiff(Date d1, Date d2) {
        return d1.getTime() - d2.getTime();
    }

    //ms를 통해 Date 객체를 반환
    public static Date getDate(Long time) {
        return new Date(time);
    }

    //두개의 시간을 더함
    public static Date addDate(Date d1, Date d2) {
        return new Date(d1.getTime()+d2.getTime());
    }

    public static String toSimpleTime(Date date) {
        return (date.getYear() - 100) +
                "/" + (date.getMonth() + 1) +
                "/" + date.getDate() +
                " " + date.getHours() +
                ":" + date.getMinutes() +
                ":" + date.getSeconds();
    }
}
