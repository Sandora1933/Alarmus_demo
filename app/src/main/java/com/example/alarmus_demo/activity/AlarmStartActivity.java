package com.example.alarmus_demo.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AsyncPlayer;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.alarmus_demo.DataAccessManager;
import com.example.alarmus_demo.NotificationHelper;
import com.example.alarmus_demo.R;
import com.example.alarmus_demo.model.SongEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class AlarmStartActivity extends AppCompatActivity {

    public static final int MELODY_DURATION = 5000;
    boolean isAnyActiveSongExist = false;

    SharedPreferences sp;
    DataAccessManager dam;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_start);

        // Waking up the phone
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        sp = getSharedPreferences("alarm_data", Context.MODE_PRIVATE);
        dam = new DataAccessManager(sp);

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
        Toast.makeText(this, "mode, volume received : " + mode + ", " + volumePower, Toast.LENGTH_SHORT).show();

        if (mode == 0){

            AsyncPlayer asyncPlayer = new AsyncPlayer("player");
            Uri uri;

            if (isAnyActiveSongExist){
                uri = Uri.parse(firstPrioritySongEntityPath);
            }
            else{
                uri = Settings.System.DEFAULT_RINGTONE_URI;
            }

            asyncPlayer.play(this, uri, false,
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build());

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

            sendNotification(this, mode);
        }
        else if (mode == 1){
            //Toast.makeText(context, "sound only", Toast.LENGTH_SHORT).show();

            AsyncPlayer asyncPlayer = new AsyncPlayer("player");
            Uri uri;

            if (isAnyActiveSongExist){
                uri = Uri.parse(firstPrioritySongEntityPath);
            }
            else{
                uri = Settings.System.DEFAULT_RINGTONE_URI;
            }

            asyncPlayer.play(this, uri, false,
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build());


            sendNotification(this, mode);
        }
        else if (mode == 2){
            sendNotification(this, mode);
        }
        else {
            sendNotification(this, mode);
        }

    }

    private void sendNotification(Context context, int mode){
        NotificationHelper helper = new NotificationHelper(context, mode);
        NotificationCompat.Builder builder = helper.getChannelNotification(String.valueOf(mode), mode);

        helper.getManager().notify(1, builder.build());
    }

}
