package com.example.alarmus_demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.alarmus_demo.model.SongEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class AlertReceiver extends BroadcastReceiver {

    public static final int MELODY_DURATION = 5000;

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isAnySongExist;
        boolean isAnyActiveSongExist = false;

        SharedPreferences sp = context.getSharedPreferences("alarm_data", Context.MODE_PRIVATE);
        DataAccessManager dam = new DataAccessManager(sp);

        // Getting song
        Gson gson = new Gson();
        String songListJson = sp.getString("song_list", null);
        String firstPrioritySongEntityPath = null;

        if (songListJson != null){
            Type listType = new TypeToken<ArrayList<SongEntity>>(){}.getType();
            ArrayList<SongEntity> songEntityArrayList = gson.fromJson(songListJson, listType);

            //Check if list contains any active songs
            for (SongEntity entity : songEntityArrayList){
                if (entity.getSongPriority() > 0){
                    isAnyActiveSongExist = true;
                }
            }


            // Check if arrayList is not empty (if yes -> play default)
            //isAnySongExist = songEntityArrayList.size() != 0;

            if (isAnyActiveSongExist){
                SongEntity firstPrioritySongEntity = null;
                for (SongEntity entity : songEntityArrayList){
                    if (entity.getSongPriority() == 1){
                        firstPrioritySongEntity = entity;
                    }
                }

                // What should we play
                firstPrioritySongEntityPath = firstPrioritySongEntity.getSongPath();
            }
        }
        else{
            isAnyActiveSongExist = false;
        }


        // Getting other data
        int mode = dam.loadAlarmSelectedMode();
        int volumePower = dam.loadVolumePower();
        int maxVolumePower = dam.loadMaxVolumePower();

        // Set up appropriate volume power
        float log1 = (float) (Math.log(maxVolumePower - volumePower) / Math.log(maxVolumePower));

        //int mode = intent.getIntExtra("mode", 1);
        Toast.makeText(context, "mode, volume received : " + mode + ", " + volumePower, Toast.LENGTH_SHORT).show();

        if (mode == 0){
            //Toast.makeText(context, "vibro + sound", Toast.LENGTH_SHORT).show();

            //MediaPlayer
//            MediaPlayer mp = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
//            mp.setAudioStreamType(AudioManager.STREAM_ALARM);
//            mp.setVolume(log1, log1);
//            mp.start();

            MediaPlayer mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);
            try {

                // If any song exists in storage (sp) then get this first priority song
                if (isAnyActiveSongExist){
                    mp.setDataSource(firstPrioritySongEntityPath);
                }
                else{
                    mp.setDataSource(context, Settings.System.DEFAULT_RINGTONE_URI);
                }

                mp.setVolume(log1, log1);
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mp.start();

//            CountDownTimer timer = new CountDownTimer(1000, MELODY_DURATION){
//                @Override
//                public void onTick(long millisUntilFinished) {
//                    // Noting to do here
//                }
//
//                @Override
//                public void onFinish() {
//                    if (mp.isPlaying()){
//                        mp.stop();
//                        mp.release();
//                    }
//                }
//            };
//
//            timer.start();

            sendNotification(context, mode);
        }
        else if (mode == 1){
            //Toast.makeText(context, "sound only", Toast.LENGTH_SHORT).show();

            MediaPlayer mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);
            try {

                // If any song exists in storage (sp) then get this first priority song
                if (isAnyActiveSongExist){
                    mp.setDataSource(firstPrioritySongEntityPath);
                }
                else{
                    mp.setDataSource(context, Settings.System.DEFAULT_RINGTONE_URI);
                }

                mp.setVolume(log1, log1);
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mp.start();

            CountDownTimer timer = new CountDownTimer(1000, MELODY_DURATION){
                @Override
                public void onTick(long millisUntilFinished) {
                    // Noting to do here
                }

                @Override
                public void onFinish() {
                    if (mp.isPlaying()){
                        mp.stop();
                        mp.release();
                    }
                }
            };

            timer.start();

            sendNotification(context, mode);
        }
        else if (mode == 2){
            //Toast.makeText(context, "vibro only", Toast.LENGTH_SHORT).show();

//            Vibrator vibrator; // :D
//            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//            vibrator.vibrate(5000);

            sendNotification(context, mode);
        }
        else {
            //Toast.makeText(context, "no sound", Toast.LENGTH_SHORT).show();
            sendNotification(context, mode);
        }

    }

    private void sendNotification(Context context, int mode){
        NotificationHelper helper = new NotificationHelper(context, mode);
        NotificationCompat.Builder builder = helper.getChannelNotification(String.valueOf(mode), mode);

        helper.getManager().notify(1, builder.build());
    }

}
