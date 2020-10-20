package com.example.alarmus_demo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.alarmus_demo.model.SongEntity;

import java.util.ArrayList;

public class SongController{

    private SharedPreferences activeSongsListPreferences;

    private ArrayList<SongEntity> activeSongsList;

    public SongController(SharedPreferences activeSongsListPreferences){
        this.activeSongsListPreferences = activeSongsListPreferences;
    }

    public ArrayList<SongEntity> getActiveSongsList() {
        return activeSongsList;
    }
}
