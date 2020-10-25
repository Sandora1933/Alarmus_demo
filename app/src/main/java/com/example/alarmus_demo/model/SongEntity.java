package com.example.alarmus_demo.model;

public class SongEntity {

    private String songTitle, singerTitle;
    private String songPath;
    private int songPriority;
    private boolean isActive; //Does participate in any alarm priority
    private boolean isPlayedNext; //Will it be played the next day? blue digit if yes

    public SongEntity(String songTitle, String singerTitle, String songPath, int songPriority,
                      boolean isActive, boolean isPlayedNext) {
        this.songTitle = songTitle;
        this.singerTitle = singerTitle;
        this.songPath = songPath;
        this.songPriority = songPriority;
        this.isActive = isActive;
        this.isPlayedNext = isPlayedNext;
    }

    public String getSongTitle() {
        return songTitle;
    }
    public String getSingerTitle() {
        return singerTitle;
    }
    public int getSongPriority() {
        return songPriority;
    }
    public String getSongPath() { return songPath; }
    public boolean isActive() { return isActive; }
    public boolean isPlayedNext() { return isPlayedNext; }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public void setPlayedNext(boolean playedNext) { isPlayedNext = playedNext; }
    public void setSongPriority(int songPriority) {
        this.songPriority = songPriority;
    }
}
