package com.example.alarmus_demo.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alarmus_demo.R;
import com.example.alarmus_demo.adapter.SongAdapter;
import com.example.alarmus_demo.model.SongEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity{

    //************  Views  ****************

    EditText songEditText;  //Field for song search

    //RecyclerView - songs
    RecyclerView songRecyclerView;
    SongAdapter songRecyclerViewAdapter;
    RecyclerView.LayoutManager songRecyclerViewManager;

    //**************   Constants   *****************

    //Storage access permission code
    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST = 1;

    //**************   Variables   *****************

    //Data
    List<SongEntity> songEntityList;    //Songs which retrieved from external storage
    List<SongEntity> activeSongEntityList;  //Songs which are saved in our storage(sharedPref)

    SharedPreferences sharedPreferences;    //Storage

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        initViews();

        sharedPreferences = getSharedPreferences("active_songs", MODE_PRIVATE);

        loadActiveSongList();   //Loading songs from our storage
        Toast.makeText(this, "kol-vo active - " + activeSongEntityList.size(), Toast.LENGTH_SHORT).show();

        songEntityList = new ArrayList<>();
        songRecyclerView = findViewById(R.id.songRecyclerView);

        //Request permission for data retrieving
        if (ContextCompat.checkSelfPermission(MusicListActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MusicListActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(MusicListActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST);
            }
            else {
                ActivityCompat.requestPermissions(MusicListActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST);
            }
        }
        else{
            setUpRecyclerView();
        }

        //editText detector for searching songs
        songEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        saveActiveSongList(); //Saving activeSongList to out storage(sharedPref)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case EXTERNAL_STORAGE_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(MusicListActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                        setUpRecyclerView();
                    }
                }
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void initViews(){
        songEditText = findViewById(R.id.songEditText);
    }

    //Filter for recyclerView
    private void filter(String text) {
        ArrayList<SongEntity> filteredList = new ArrayList<>();
        for (SongEntity song : songEntityList) {
            if (song.getSongTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(song);
            }
        }
        songRecyclerViewAdapter.filterList(filteredList);
    }

    //Getting songs from external storage to our songList<>
    private void getMusicFromStorage(){
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()){
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);

//                This info we may know after comparing with activeSongList<>
//                If activeSongList<> contains any songs with such TITLE then it has priority
//                And we are ready to assign priority variable, otherwise it is 0
                int currentPriority = 0;
                boolean isCurrentActive = false;

                if (activeSongEntityList != null) {
                    for (SongEntity song : activeSongEntityList){
                        if (song.getSongTitle().equals(currentTitle)){
                            //Such song exists in activeSongList<>
                            currentPriority = song.getSongPriority();
                            isCurrentActive = true;
                        }
                    }
                }

                songEntityList.add(new SongEntity(currentTitle, currentArtist,
                        currentPriority, isCurrentActive));
            } while (songCursor.moveToNext());
        }
    }

    private void setUpRecyclerView(){
        songRecyclerViewAdapter = new SongAdapter(this, songEntityList, activeSongEntityList);
        songRecyclerViewManager = new LinearLayoutManager(this);
        getMusicFromStorage();

        songRecyclerView.setAdapter(songRecyclerViewAdapter);
        songRecyclerView.setLayoutManager(songRecyclerViewManager);

        songRecyclerViewAdapter.setOnSongItemClickListener(new SongAdapter.OnSongItemClickListener() {
            @Override
            public void onClick(int position) {

                songEntityList.get(position).setActive(true);
                songEntityList.get(position).setSongPriority(activeSongEntityList.size());

                activeSongEntityList.add(songEntityList.get(position));
                songRecyclerViewAdapter.notifyDataSetChanged();

                Toast.makeText(MusicListActivity.this, "title - " +
                        songEntityList.get(position).getSongTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //********   activity transition   *********

    public void alarmMenuButtonClicked(View view){
        Intent alarmMenuActivityIntent = new Intent(MusicListActivity.this, AlarmActivity.class);
        startActivity(alarmMenuActivityIntent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void infoMenuButtonClicked(View view){
        Intent infoMenuActivityIntent = new Intent(MusicListActivity.this, InfoActivity.class);
        startActivity(infoMenuActivityIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    //-------------------------------------------

    //Saving activeSongList<> to storage(SharedPreferences)
    private void saveActiveSongList(){
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String activeSongListJson = gson.toJson(activeSongEntityList);

        editor.putString("active_song_list", activeSongListJson);
        editor.apply();
        Toast.makeText(this, "data saved to storage", Toast.LENGTH_SHORT).show();
    }

    //Loading activeSongList<> from storage or creating new (if list is null)
    private void loadActiveSongList(){
        Gson gson = new Gson();
        String activeSongListJson = sharedPreferences.getString("active_song_list", null);

        Type listType = new TypeToken<ArrayList<SongEntity>>(){}.getType();
        activeSongEntityList = gson.fromJson(activeSongListJson, listType);

        if (activeSongListJson == null){
            activeSongEntityList = new ArrayList<SongEntity>();
        }
    }

}
