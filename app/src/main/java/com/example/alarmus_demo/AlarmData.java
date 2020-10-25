package com.example.alarmus_demo;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.format.Time;

public class AlarmData {
    private Integer hour,minute;
    private boolean state;//false - off, true - on
    private boolean[] days;
    private boolean allDays;


    public AlarmData(){
        allDays = false;
        state = false;
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

    // *********** Old code ************************

    /*public boolean checkTime(Time currentTime){
        Integer h=currentTime.hour;
        Integer m=currentTime.minute;
        return (hour.equals(h) && minute.equals(m));
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean isState() {
        return state;
    }

    public boolean getDay(int day){
        return days[day];
    }

    public void changeDay(int day){
        days[day]=!days[day];
    }

    public void setDay(int day,boolean val){
        days[day]=val;
    }

    public boolean checkDay(int day){
        return (allDays || days[day]);
    }

    public void setAllDays(boolean allDays) {
        this.allDays = allDays;
    }

    public boolean isAllDays() {
        return allDays;
    }*/
}
