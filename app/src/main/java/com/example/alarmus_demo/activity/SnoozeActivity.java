package com.example.alarmus_demo.activity;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.alarmus_demo.AlertReceiver;
import com.example.alarmus_demo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SnoozeActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_5MIN = 11;
    public static final int REQUEST_CODE_10MIN = 12;
    public static final int REQUEST_CODE_15MIN = 13;
    public static final int REQUEST_CODE_30MIN = 14;
    public static final int REQUEST_CODE_60MIN = 15;

    CountDownTimer timer;

    TextView secondsLeftDigitTextView;
    CardView card5minLeft, card10minLeft, card15minLeft, card30minLeft, card60minLeft;
    TextView snooze5minTextView, snooze10minTextView, snooze15minTextView, snooze30minTextView,
        snooze60minTextView;

    CardView timeSnoozeCardView;
    TextView btn5minOptionTextView, btn10minOptionTextView, btn15minOptionTextView,
            btn30minOptionTextView, btn60minOptionTextView;
    TextView btn5minOptionValueTextView, btn10minOptionValueTextView, btn15minOptionValueTextView,
            btn30minOptionValueTextView, btn60minOptionValueTextView;

    int snoozeMode; // none - 0, 5min - 1, 10min - 2, 15min - 3, 30 min - 4, 60 min - 5
    int minutesLeft;
    long timeLeftInMillis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snooze_new);

        // Waking up the phone
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        init();
        timeSnoozeCardView.setBackgroundResource(R.drawable.card_time_snooze);

        snooze5minTextView.setVisibility(View.INVISIBLE);
        snooze10minTextView.setVisibility(View.INVISIBLE);
        snooze15minTextView.setVisibility(View.INVISIBLE);
        snooze30minTextView.setVisibility(View.INVISIBLE);
        snooze60minTextView.setVisibility(View.INVISIBLE);

        card5minLeft.setVisibility(View.INVISIBLE);
        card10minLeft.setVisibility(View.INVISIBLE);
        card15minLeft.setVisibility(View.INVISIBLE);
        card30minLeft.setVisibility(View.INVISIBLE);
        card60minLeft.setVisibility(View.INVISIBLE);

    }

    private void init(){
        timeSnoozeCardView = findViewById(R.id.timeSnoozeCardView);

        minutesLeft = 0;
        timeLeftInMillis = 0;

        secondsLeftDigitTextView = findViewById(R.id.secondsLeftDigitTextView);

        btn5minOptionTextView = findViewById(R.id.btn5minTextView);
        btn10minOptionTextView = findViewById(R.id.btn10minTextView);
        btn15minOptionTextView = findViewById(R.id.btn15minTextView);
        btn30minOptionTextView = findViewById(R.id.btn30minTextView);
        btn60minOptionTextView = findViewById(R.id.btn60minTextView);

        btn5minOptionValueTextView = findViewById(R.id.btn5minValueTextView);
        btn10minOptionValueTextView = findViewById(R.id.btn10minValueTextView);
        btn15minOptionValueTextView = findViewById(R.id.btn15minValueTextView);
        btn30minOptionValueTextView = findViewById(R.id.btn30minValueTextView);
        btn60minOptionValueTextView = findViewById(R.id.btn60minValueTextView);

        card5minLeft = findViewById(R.id.card5minLeft);
        card10minLeft = findViewById(R.id.card10minLeft);
        card15minLeft = findViewById(R.id.card15minLeft);
        card30minLeft = findViewById(R.id.card30minLeft);
        card60minLeft = findViewById(R.id.card60minLeft);

        card5minLeft.setBackgroundResource(R.drawable.card_snooze);
        card10minLeft.setBackgroundResource(R.drawable.card_snooze);
        card15minLeft.setBackgroundResource(R.drawable.card_snooze);
        card30minLeft.setBackgroundResource(R.drawable.card_snooze);
        card60minLeft.setBackgroundResource(R.drawable.card_snooze);

        snooze5minTextView = findViewById(R.id.snooze5minTextView);
        snooze10minTextView = findViewById(R.id.snooze10minTextView);
        snooze15minTextView = findViewById(R.id.snooze15minTextView);
        snooze30minTextView = findViewById(R.id.snooze30minTextView);
        snooze60minTextView = findViewById(R.id.snooze60minTextView);

    }

    public void btn5minSnoozeClicked(View view) {

        if (snoozeMode != 1){

            // Remove previous
            switch (snoozeMode){
                case 2:
                    btn10minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn10minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze10minTextView.setVisibility(View.INVISIBLE);
                    card10minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    btn15minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn15minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze15minTextView.setVisibility(View.INVISIBLE);
                    card15minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    btn30minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn30minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze30minTextView.setVisibility(View.INVISIBLE);
                    card30minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 5:
                    btn60minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn60minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze60minTextView.setVisibility(View.INVISIBLE);
                    card60minLeft.setVisibility(View.INVISIBLE);
                    break;
            }

            snoozeMode = 1;
            //activateVisibility(snoozeMode);
            btn5minOptionTextView.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
            btn5minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));
            snooze5minTextView.setVisibility(View.VISIBLE);
            card5minLeft.setVisibility(View.VISIBLE);

            secondsLeftDigitTextView.setText(String.valueOf(5 * 60));

        }

    }

    public void btn10minSnoozeClicked(View view) {

        if (snoozeMode != 2){

            // Remove previous
            switch (snoozeMode){
                case 1:
                    btn5minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn5minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze5minTextView.setVisibility(View.INVISIBLE);
                    card5minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    btn15minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn15minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze15minTextView.setVisibility(View.INVISIBLE);
                    card15minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    btn30minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn30minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze30minTextView.setVisibility(View.INVISIBLE);
                    card30minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 5:
                    btn60minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn60minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze60minTextView.setVisibility(View.INVISIBLE);
                    card60minLeft.setVisibility(View.INVISIBLE);
                    break;
            }

            snoozeMode = 2;
            //activateVisibility(snoozeMode);
            btn10minOptionTextView.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
            btn10minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));
            snooze10minTextView.setVisibility(View.VISIBLE);
            card10minLeft.setVisibility(View.VISIBLE);

            secondsLeftDigitTextView.setText(String.valueOf(10 * 60));
        }

    }

    public void btn15minSnoozeClicked(View view) {

        if (snoozeMode != 3){

            // Remove previous
            switch (snoozeMode){
                case 1:
                    btn5minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn5minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze5minTextView.setVisibility(View.INVISIBLE);
                    card5minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    btn10minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn10minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze10minTextView.setVisibility(View.INVISIBLE);
                    card10minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    btn30minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn30minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze30minTextView.setVisibility(View.INVISIBLE);
                    card30minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 5:
                    btn60minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn60minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze60minTextView.setVisibility(View.INVISIBLE);
                    card60minLeft.setVisibility(View.INVISIBLE);
                    break;
            }

            snoozeMode = 3;
            //activateVisibility(snoozeMode);
            btn15minOptionTextView.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
            btn15minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));
            snooze15minTextView.setVisibility(View.VISIBLE);
            card15minLeft.setVisibility(View.VISIBLE);

            secondsLeftDigitTextView.setText(String.valueOf(15 * 60));
        }

    }

    public void btn30minSnoozeClicked(View view) {

        if (snoozeMode != 4){

            // Remove previous
            switch (snoozeMode){
                case 1:
                    btn5minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn5minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze5minTextView.setVisibility(View.INVISIBLE);
                    card5minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    btn10minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn10minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze10minTextView.setVisibility(View.INVISIBLE);
                    card10minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    btn15minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn15minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze15minTextView.setVisibility(View.INVISIBLE);
                    card15minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 5:
                    btn60minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn60minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze60minTextView.setVisibility(View.INVISIBLE);
                    card60minLeft.setVisibility(View.INVISIBLE);
                    break;
            }

            snoozeMode = 4;
            //activateVisibility(snoozeMode);
            btn30minOptionTextView.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
            btn30minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));
            snooze30minTextView.setVisibility(View.VISIBLE);
            card30minLeft.setVisibility(View.VISIBLE);

            secondsLeftDigitTextView.setText(String.valueOf(30 * 60));
        }

    }

    public void btn60minSnoozeClicked(View view) {

        if (snoozeMode != 5){

            // Remove previous
            switch (snoozeMode){
                case 1:
                    btn5minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn5minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze5minTextView.setVisibility(View.INVISIBLE);
                    card5minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    btn10minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn10minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze10minTextView.setVisibility(View.INVISIBLE);
                    card10minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    btn15minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn15minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze15minTextView.setVisibility(View.INVISIBLE);
                    card15minLeft.setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    btn30minOptionTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    btn30minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorUnActiveText));
                    snooze30minTextView.setVisibility(View.INVISIBLE);
                    card30minLeft.setVisibility(View.INVISIBLE);
                    break;
            }

            snoozeMode = 5;
            //activateVisibility(snoozeMode);
            btn60minOptionTextView.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
            btn60minOptionValueTextView.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));
            snooze60minTextView.setVisibility(View.VISIBLE);
            card60minLeft.setVisibility(View.VISIBLE);

            secondsLeftDigitTextView.setText(String.valueOf(60 * 60));
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopClicked(View view) {
        //System.exit(1);
        //finishAffinity();
//        finish();
//        System.exit(0);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void snooze5minTextViewClicked(View view) {
        setExactAlarmForOption(REQUEST_CODE_5MIN);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void snooze10minTextViewClicked(View view) {
        setExactAlarmForOption(REQUEST_CODE_10MIN);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void snooze15minTextViewClicked(View view) {
        setExactAlarmForOption(REQUEST_CODE_15MIN);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void snooze30minTextViewClicked(View view) {
        setExactAlarmForOption(REQUEST_CODE_30MIN);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void snooze60minTextViewClicked(View view) {
        setExactAlarmForOption(REQUEST_CODE_60MIN);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setExactAlarmForOption(int requestCode){

        int minutesMore;
        switch (requestCode){
            case REQUEST_CODE_5MIN:
                minutesMore = 5;
                break;
            case REQUEST_CODE_10MIN:
                minutesMore = 10;
                break;
            case REQUEST_CODE_15MIN:
                minutesMore = 15;
                break;
            case REQUEST_CODE_30MIN:
                minutesMore = 30;
                break;
            case REQUEST_CODE_60MIN:
                minutesMore = 60;
                break;
            default:
                minutesMore = 0;
                break;
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, 0);

        long currentTime = System.currentTimeMillis();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, currentTime + (minutesMore-3) * 60 * 1000,
                pendingIntent);

        Toast.makeText(this, "set for " + minutesMore + " min", Toast.LENGTH_SHORT).show();
        //startTimer(snoozeMode);

    }

    private void startTimer(int mode){

        switch (mode){
            case 1:
                minutesLeft = 5;
                break;
            case 2:
                minutesLeft = 10;
                break;
            case 3:
                minutesLeft = 15;
                break;
            case 4:
                minutesLeft = 30;
                break;
            case 5:
                minutesLeft = 60;
                break;
            default:
                minutesLeft = 0;
        }

        timeLeftInMillis = minutesLeft * 60 * 1000;
        updateTimerText(timeLeftInMillis);

//        timer = new CountDownTimer(timeLeftInMillis, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                timeLeftInMillis = millisUntilFinished;
//                updateTimerText(timeLeftInMillis);
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        }.start();
    }

    private void updateTimerText(long millisLeft){
        secondsLeftDigitTextView.setText("" + millisLeft / 1000);
    }

}
