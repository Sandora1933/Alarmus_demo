package com.example.alarmus_demo.model;

public class SongEntity {

    private String songTitle, singerTitle;
    private int songNumber;
    private boolean isActive; //Does participate in any alarm priority

    public SongEntity(String songTitle, String singerTitle, int songNumber, boolean isActive) {
        this.songTitle = songTitle;
        this.singerTitle = singerTitle;
        this.songNumber = songNumber;
        this.isActive = isActive;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSingerTitle() {
        return singerTitle;
    }

    public int getSongNumber() {
        return songNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}
