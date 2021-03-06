package com.example.alarmus_demo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationHelper extends ContextWrapper {

    public static final String CHANNEL_VIBRO_ID = "Channel_vibro_id";
    public static final String CHANNEL_NO_VIBRO_ID = "Channel_no_vibro_id";

    public static final String CHANNEL_VIBRO_NAME = "Channel_vibro_name";
    public static final String CHANNEL_NO_VIBRO_NAME = "Channel_no_vibro_name";

    private NotificationManager mManager;

    public NotificationHelper(Context base, int mode) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel(mode);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel(int mode){

        if (mode == 0 || mode == 2){    //  Modes with vibration
            NotificationChannel channelWithVibration = new NotificationChannel(CHANNEL_VIBRO_ID,
                    CHANNEL_VIBRO_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channelWithVibration.enableLights(true);
            channelWithVibration.enableVibration(true);
            channelWithVibration.shouldVibrate();
            channelWithVibration.setVibrationPattern(new long[] {0, 5000});
            channelWithVibration.setLightColor(R.color.colorPrimary);
            channelWithVibration.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            getManager().createNotificationChannel(channelWithVibration);
        }
        else {
            NotificationChannel channelWithoutVibration = new NotificationChannel(CHANNEL_NO_VIBRO_ID,
                    CHANNEL_NO_VIBRO_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channelWithoutVibration.enableLights(true);
            channelWithoutVibration.setLightColor(R.color.colorPrimary);
            channelWithoutVibration.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            getManager().createNotificationChannel(channelWithoutVibration);
        }

    }

    public NotificationManager getManager(){
        if (mManager == null){
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification(String title, int mode){

        if (mode == 0 || mode == 2){
            return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_VIBRO_ID)
                    .setContentTitle(title)
                    .setContentText("Content text")
                    .setSmallIcon(R.drawable.ic_baseline_alarm_24);
        }
        else {
            return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_NO_VIBRO_ID)
                    .setContentTitle(title)
                    .setContentText("Content text")
                    .setSmallIcon(R.drawable.ic_baseline_alarm_24);
        }


    }

}
