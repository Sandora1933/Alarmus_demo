package com.example.alarmus_demo;

import android.content.Context;
import android.content.SharedPreferences;

public class AlarmController {
    private static final String APP_PREFERENCES = "alarm_data";
    public static SharedPreferences sharedPreferences;

    private static AlarmData data;

    public static void setup(){
        data = new AlarmData();
        if(sharedPreferences.contains("hour")){
            setHour(sharedPreferences.getInt("hour", 0));
        }else {
            setHour(0);
        }
        if(sharedPreferences.contains("minute")){
            setMinute(sharedPreferences.getInt("minute", 0));
        }else {
            setMinute(0);
        }
    }

    public static Integer getHour() {
        return data.getHour();
    }

    public static Integer getMinute() {
        return data.getMinute();
    }

    public static void setHour(Integer hour) {
        data.setHour(hour);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("hour",data.getMinute());
        editor.apply();
    }

    public static void setMinute(Integer minute) {
        data.setMinute(minute);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("minute",data.getMinute());
        editor.apply();
    }

    public static String getTimeString(){return data.getTimeString();}
}
