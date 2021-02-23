package com.example.alarmus_demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int mode = intent.getIntExtra("mode", 1);

        if (mode == 0){
            Toast.makeText(context, "vibro + sound", Toast.LENGTH_SHORT).show();

            //MediaPlayer
            MediaPlayer mp = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
            mp.start();

            Vibrator vibrator; // :D
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(5000);

            sendNotification(context, mode);
        }
        else if (mode == 1){
            Toast.makeText(context, "sound only", Toast.LENGTH_SHORT).show();

            //MediaPlayer
            MediaPlayer mp = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
            mp.start();

            sendNotification(context, mode);
        }
        else if (mode == 2){
            Toast.makeText(context, "vibro only", Toast.LENGTH_SHORT).show();

            Vibrator vibrator; // :D
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(5000);

            sendNotification(context, mode);
        }
        else {
            Toast.makeText(context, "no sound", Toast.LENGTH_SHORT).show();
            sendNotification(context, mode);
        }

    }

    private void sendNotification(Context context, int mode){
        NotificationHelper helper = new NotificationHelper(context);
        NotificationCompat.Builder builder = helper.getChannelNotification(String.valueOf(mode));
        helper.getManager().notify(1, builder.build());
    }

}
