package com.example.alarmus_demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.example.alarmus_demo.activity.AlarmStartActivity;

import java.util.Calendar;

public class AlertReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO: Transfer day through intent, not by means of accessManager here

        SharedPreferences sp;
        sp = context.getSharedPreferences("alarm_data", Context.MODE_PRIVATE);
        DataAccessManager dam = new DataAccessManager(sp);

        // In Java calendar there are such constants:
        // Mon - 2, Tue - 3, Wen - 4, Th - 5, Fr - 6, Sat - 7, Sun - 1
        // But actually : Mon - 0, Tue - 1...

        // Check if current day is active
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        int currentDayActual;
        if (currentDay == 1){   // Sunday
            currentDayActual = 6;
        }
        else{
            currentDayActual = currentDay - 2;
        }

        boolean isCurrentDayActive = dam.loadDay(currentDayActual);
        if (isCurrentDayActive || dam.loadIsNoneActive()){ // Option when none chosen but need to play once

            if (dam.loadIsNoneActive()){
                dam.saveIsActive(false);
            }

            Intent i = new Intent(context, AlarmStartActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }




    }
}
