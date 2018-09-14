package com.tang.mqtt.info;

import com.google.gson.Gson;
import com.tang.mqtt.HttpRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonTime {
    public String time;
    public int code;
    public String message;

    public static String getServiceTime() {
        String sr = HttpRequest.sendPost("http://www.jsrfiot.com/Server/Time", null);
        if (null == sr) {
            return null;
        }
        Gson gson = new Gson();
        JsonTime time = gson.fromJson(sr, JsonTime.class);
        return time.time;
    }

    public static boolean getTimeIfInTimeOut(String time, long timeOut) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time1 = time;
            String time2 = getServiceTime();
            if (null == time2) {
                return false;
            }
            Date d1 = format.parse(time1);
            Date d2 = format.parse(time2);
            long diff = d2.getTime() - d1.getTime();
            long diffSeconds = diff;
            return (diffSeconds / 1000) <= timeOut;
        } catch (Exception e) {
            return false;
        }
    }
}