package com.example.alarmus_demo;

import android.content.Context;
import android.content.SharedPreferences;

public class AlarmController {
    private static final String APP_PREFERENCES = "alarm_data";
    private static SharedPreferences sharedPreferences;

    public AlarmData data;

    public void setup(SharedPreferences sharedP){
        sharedPreferences=sharedP;
        data = new AlarmData();

        if (sharedPreferences.contains("hour")){
            setHour(sharedPreferences.getInt("hour", 0));
        }
        else {
            setHour(0);
        }

        if (sharedPreferences.contains("minute")){
            setMinute(sharedPreferences.getInt("minute", 0));
        }
        else {
            setMinute(0);
        }

        for (int i = 0; i < 7; i++){
            if(sharedPreferences.contains("day"+String.valueOf(i))){
                setDay(sharedPreferences.getBoolean("day"+String.valueOf(i), false), i);
            }
            else {
                setDay(false, i);
            }
        }

        if (sharedPreferences.contains("alarmSelectedMode")){
            setAlarmSelectedMode(sharedPreferences.getInt("alarmSelectedMode", 1));
        }
        else {
            setAlarmSelectedMode(1);
        }
    }

    public Integer getHour() {
        return data.getHour();
    }

    public Integer getMinute() {
        return data.getMinute();
    }

    public Integer getAlarmSelectedMode() {
        return data.getAlarmSelectedMode();
    }

    public boolean getDay(int day){
        if (day>=0 && day<7){
            return data.getDay(day);
        }else {
            return false;
        }
    }

    public void setDay(boolean value,int day){
        if (day>=0 && day<7){
            data.setDay(value,day);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("day"+String.valueOf(day),data.getDay(day));
            editor.apply();
        }
    }

    public void setDays(boolean[] isDayActiveArray){
        for (int i=0;i<7;i++){
            setDay(isDayActiveArray[i], i);
        }
    }

    public void setHour(Integer hour) {
        data.setHour(hour);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("hour",data.getHour());
        editor.apply();
    }

    public void setMinute(Integer minute) {
        data.setMinute(minute);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("minute",data.getMinute());
        editor.apply();
    }

    public void setAlarmSelectedMode(Integer newMode){
        data.setAlarmSelectedMode(newMode);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("alarmSelectedMode",data.getAlarmSelectedMode());
        editor.apply();
    }

    public String getTimeString(){return data.getTimeString();}
}
