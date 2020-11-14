package com.example.alarmus_demo;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

public class AlarmData implements Parcelable {

    //Constants for alarmSelectedMode
    public static final int ALARM_SELECTED_MODE_SOUND_VIBRATE = 0;
    public static final int ALARM_SELECTED_MODE_SOUND_ONLY = 1;
    public static final int ALARM_SELECTED_MODE_VIBRATE_ONLY = 2;
    public static final int ALARM_SELECTED_MODE_NO_SOUND = 3;

    //Constants for volumeIncreaseMode
    public static final int VOL_INCREASE_MODE_NEVER = 0;
    public static final int VOL_INCREASE_MODE_15s = 1;
    public static final int VOL_INCREASE_MODE_30s = 2;
    public static final int VOL_INCREASE_MODE_45s = 3;
    public static final int VOL_INCREASE_MODE_60s = 4;

    private Integer hour,minute;    //Selected time
    private boolean isActive;   //Is alarm ON

    //Volume menu fields
    private int volumePower;
    private int volumeIncreaseMode;

    //Mode menu fields
    private Integer alarmSelectedMode;
    private boolean[] days;

    public AlarmData(){
        alarmSelectedMode = ALARM_SELECTED_MODE_SOUND_ONLY;
        hour = 0;
        minute = 0;
        days = new boolean[7];
    }

    //************ Parcelable implementation ***********

    protected AlarmData(Parcel in) {
        if (in.readByte() == 0) {
            hour = null;
        } else {
            hour = in.readInt();
        }
        if (in.readByte() == 0) {
            minute = null;
        } else {
            minute = in.readInt();
        }

        isActive = in.readByte() != 0;
        volumePower = in.readInt();
        volumeIncreaseMode = in.readInt();

        if (in.readByte() == 0) {
            alarmSelectedMode = null;
        } else {
            alarmSelectedMode = in.readInt();
        }

        days = in.createBooleanArray();
    }

    public static final Creator<AlarmData> CREATOR = new Creator<AlarmData>() {
        @Override
        public AlarmData createFromParcel(Parcel in) {
            return new AlarmData(in);
        }

        @Override
        public AlarmData[] newArray(int size) {
            return new AlarmData[size];
        }
    };

    //************ Getters *************

    public Integer getHour() { return hour; }
    public Integer getMinute() { return minute; }
    public boolean isActive() { return isActive; }

    public String getTimeString(){
        String time=((Integer)(this.hour/10)).toString()+((Integer)(this.hour%10)).toString()+" : "+
                ((Integer)(this.minute/10)).toString()+((Integer)(this.minute%10)).toString();
        return time;
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
    public Integer getAlarmSelectedMode(){
        return alarmSelectedMode;
    }

    //************ Setters *************

    public void setHour(Integer hour) { this.hour = Math.max(0,Math.min(23,hour)); }
    public void setMinute(Integer minute) { this.minute = Math.max(0,Math.min(59,minute)); }

    public void setAsActive(){ isActive = true; }
    public void setAsNotActive() { isActive = false; }

    public void setDay(boolean value,int day){
        if (day>=0 && day<7){
            days[day]=value;
        }
    }

    public void setDays(boolean[] isDayActiveArray){
        days=isDayActiveArray;
    }

    public void setAlarmSelectedMode(Integer newMode){
        if (newMode>=0 && newMode<=3){
            alarmSelectedMode=newMode;
        }
    }

    //************** Parcelable methods ****************

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (hour == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(hour);
        }
        if (minute == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(minute);
        }

        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeInt(volumePower);
        dest.writeInt(volumeIncreaseMode);

        if (alarmSelectedMode == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(alarmSelectedMode);
        }

        dest.writeBooleanArray(days);
    }
}
