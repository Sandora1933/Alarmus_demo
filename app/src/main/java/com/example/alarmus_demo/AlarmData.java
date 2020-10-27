package com.example.alarmus_demo;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.format.Time;

public class AlarmData {
    public static final int ALARM_SELECTED_MODE_SOUND_VIBRATE = 0;
    public static final int ALARM_SELECTED_MODE_SOUND_ONLY = 1;
    public static final int ALARM_SELECTED_MODE_VIBRATE_ONLY = 2;
    public static final int ALARM_SELECTED_MODE_NO_SOUND = 3;

    private Integer hour,minute;
    private boolean[] days;
    private Integer alarmSelectedMode;

    public AlarmData(){
        alarmSelectedMode=ALARM_SELECTED_MODE_SOUND_ONLY;
        hour = 0;
        minute = 0;
        days = new boolean[7];
    }

    public Integer getHour() { return hour; }

    public Integer getMinute() { return minute; }

    public void setHour(Integer hour) { this.hour = Math.max(0,Math.min(23,hour)); }

    public void setMinute(Integer minute) { this.minute = Math.max(0,Math.min(59,minute)); }

    public String getTimeString(){
        String time=((Integer)(this.hour/10)).toString()+((Integer)(this.hour%10)).toString()+" : "+
                ((Integer)(this.minute/10)).toString()+((Integer)(this.minute%10)).toString();
        return time;
    }

    public void setDay(boolean value,int day){
        if (day>=0 && day<7){
            days[day]=value;
        }
    }

    public void setDays(boolean[] isDayActiveArray){
        days=isDayActiveArray;
    }

    public boolean getDay(int day){
        if (day>=0 && day<=6)
            return days[day];
        else
            return false;
    }

    public boolean[] getDays(){
        return days;
    }

    public void setAlarmSelectedMode(Integer newMode){
        if (newMode>=0 && newMode<=3){
            alarmSelectedMode=newMode;
        }
    }

    public Integer getAlarmSelectedMode(){
        return alarmSelectedMode;
    }


}
