package com.example.alarmus_demo.model;

public class CardInfoEntity {

    private String title, url, shortInfo;

    public CardInfoEntity(String title, String url, String shortInfo) {
        this.title = title;
        this.url = url;
        this.shortInfo = shortInfo;
    }

    public String getTitle() {
        return title;
    }
    public String getUrl() {
        return url;
    }
    public String getShortInfo() {
        return shortInfo;
    }
}
