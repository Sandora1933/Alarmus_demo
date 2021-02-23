package com.example.alarmus_demo;

import android.content.SharedPreferences;

public class DataAccessManager {

    SharedPreferences sp;

    public DataAccessManager(SharedPreferences sp){
        this.sp = sp;
    }

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

    public int loadHour(){
        return sp.getInt("hour", 0);
    }

    public int loadMinute(){
        return sp.getInt("minute", 0);
    }

    public boolean loadIsActive(){
        return sp.getBoolean("is_active", false);
    }

}
