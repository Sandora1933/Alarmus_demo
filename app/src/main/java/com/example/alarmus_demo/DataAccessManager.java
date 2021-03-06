package com.example.alarmus_demo;

import android.content.SharedPreferences;

public class DataAccessManager {

    SharedPreferences sp;

    public DataAccessManager(SharedPreferences sp){
        this.sp = sp;
    }

    // Saving methods

    public void saveHour(int hour){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("hour", hour);
        editor.apply();
    }

    public void saveMinute(int minute){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("minute", minute);
        editor.apply();
    }

    public void saveIsActive(boolean isActive){
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("is_active", isActive);
        editor.apply();
    }

    public void saveVolumePower(int power){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("volume_power", power);
        editor.apply();
    }

    public void saveMaxVolumePower(int power){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("max_volume_power", 0);
        editor.apply();
    }

    public void saveVolumeSelectedMode(int mode){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("volume_selected_mode", mode);
        editor.apply();
    }

    public void saveAlarmSelectedMode(int mode){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("alarm_selected_mode", mode);
        editor.apply();
    }

    public void saveDay(int day, boolean value){
        if (day>=0 && day<7){
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("day" + day, value);
            editor.apply();
        }
    }

    public void saveDays(boolean[] isDayActiveArray){
        for (int i = 0; i < 7; i++){
            saveDay(i, isDayActiveArray[i]);
        }
    }

    // Loading methods

    public int loadHour(){
        return sp.getInt("hour", 0);
    }

    public int loadMinute(){
        return sp.getInt("minute", 0);
    }

    public boolean loadIsActive(){
        return sp.getBoolean("is_active", false);
    }

    public int loadVolumePower() {
        return sp.getInt("volume_power", 0);
    }

    public int loadMaxVolumePower(){
        return sp.getInt("max_volume_power", 0);
    }

    public int loadVolumeSelectedMode(){
        return sp.getInt("volume_selected_mode", 0);
    }

    public int loadAlarmSelectedMode(){
        return sp.getInt("alarm_selected_mode", 0);
    }

    public boolean loadDay(int day){
        return sp.getBoolean("day" + day, false);
    }

    public boolean[] loadDays(){
        boolean[] days = new boolean[7];

        for (int i = 0; i < 7; i++){
            days[i] = loadDay(i);
        }

        return days;
    }

}
