package com.day.getbazzarspring.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static final SimpleDateFormat Fsdf_cn = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");

    //设置时区,默认为北京时区
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Fsdf_cn.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    public static Timestamp StringToStamp(String data) {
        try {
            return new Timestamp(sdf.parse(data).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String DataToString(Date date) {
        return sdf.format(date);
    }

    public static String DataToString(Date date, SimpleDateFormat sdf) {
        return sdf.format(date);
    }

    public static String getCurrentTimeAsString() {
        return DataToString(new Date());
    }


}
