package com.example.alarmus_demo.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Property;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alarmus_demo.AlarmController;
import com.example.alarmus_demo.AlarmData;
import com.example.alarmus_demo.AlertReceiver;
import com.example.alarmus_demo.DataAccessManager;
import com.example.alarmus_demo.R;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmActivity extends AppCompatActivity {

    //************  Views  ****************

    Switch setTimeSwitch;

    EditText clockEditText;

    //Views - Alarm volume panel
    TextView increaseVolumeTextView;
    Button neverVolumeTimersButton, c30sVolumeTimersButton, c15sVolumeTimersButton, c45sVolumeTimersButton,
            c60sVolumeTimersButton;
    SeekBar volumeSeekBar;

    //Views - Alarm mode panel
    Button songPlusVibrateButton;
    ImageButton songImageButton, vibrateImageButton, noSoundImageButton;

    //Views - Alarm mode days panel
    Button mondayButton, tuesdayButton, wednesdayButton, thursdayButton, fridayButton,
            saturdayButton, sundayButton;
    Button[] dayButtonArray; //Array for all these buttons (easier to go through)


    //**************   Constants   *****************

    public static final int ALARM_SELECTED_MODE_SOUND_VIBRATE = 0;
    public static final int ALARM_SELECTED_MODE_SOUND_ONLY = 1;
    public static final int ALARM_SELECTED_MODE_VIBRATE_ONLY = 2;
    public static final int ALARM_SELECTED_MODE_NO_SOUND = 3;

    public static final int VOL_INCREASE_MODE_NEVER = 0;
    public static final int VOL_INCREASE_MODE_15s = 1;
    public static final int VOL_INCREASE_MODE_30s = 2;
    public static final int VOL_INCREASE_MODE_45s = 3;
    public static final int VOL_INCREASE_MODE_60s = 4;

    private static final String APP_PREFERENCES = "alarm_data";
    public static final String ALARM_DATA_KEY_PREFERENCES = "alarm_preferences";

    //**************   Variables   *****************

    boolean isTimeChangedByHand; //Detects when timeEditText is being changed by user (not by system)
    int cursorPosition; //Cursor for timeEditText
    int alarmSelectedMode; //0: sound+vibrate, 1: sound, 2: vibrate, 3: noSound
    int volumeIncreaseMode; //0: never, 1: 15s, 2: 30s, 3: 45s, 4: 60s
    double volumePowerPercent;  // 0-100% of volume power value (Does not correlate with system alarm value yet)
    int maxVolumePower;
    boolean[] isDayActiveArray; //Array of states (active or not) of days' buttons
    boolean isMoreVolumeButtonsActive;

    SharedPreferences sharedPreferences;    //Storage
    AlarmController alarmController;
    AlarmData alarmData;

    DataAccessManager dataAccessManager;

    // To work with volume seek bar
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        initViews();
        initDayButtonsPanel();

        sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolumePower = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

        alarmData = new AlarmData();
        dataAccessManager = new DataAccessManager(sharedPreferences);

        //Filling alarm data
        alarmData.setHour(dataAccessManager.loadHour());
        alarmData.setMinute(dataAccessManager.loadMinute());

        // Initial power is 75% of max
        alarmData.setVolumePower((int) ((int) maxVolumePower * 0.75));

        // Set up seek bar
        // TODO: transfer to distinct method setUpSeekBar()
        volumeSeekBar.setMax(maxVolumePower);
        volumeSeekBar.setProgress(alarmData.getVolumePower());

        if (dataAccessManager.loadIsActive()){
            alarmData.setAsActive();
        }
        else {
            alarmData.setAsNotActive();
        }

        alarmData.setVolumeSelectedMode(dataAccessManager.loadVolumeSelectedMode());
        alarmData.setAlarmSelectedMode(dataAccessManager.loadAlarmSelectedMode());

        alarmData.setDays(dataAccessManager.loadDays());

//        alarmController = new AlarmController();
//        alarmController.setup(sharedPreferences);
//        alarmData = alarmController.data;

        isTimeChangedByHand=false;

        //TODO: organise into distinct method setUpWidgets()

        clockEditText.setText(alarmData.getTimeString());
        //clockEditText.setText(alarmController.getTimeString());

        setTimeSwitch.setChecked(alarmData.isActive());

        //setUpVolumePanel
        setUpVolumeIncreasePanel();
        setUpAlarmModePanel();

        setUpAlarmDaysPanel();

        //DONE: get from SharedPreferences
        //Initial alarm mode (but this data should be retrieved from sharedPreferences)
        //alarmSelectedMode = alarmController.getAlarmSelectedMode();

        //Initial settings for clockEditText
        isTimeChangedByHand = true;
        cursorPosition = 0;
        final String separator = " : ";

        isMoreVolumeButtonsActive = false;

        //loadAlarmDataPreferences();

        //DONE: get from SharedPreferences
        //initDayButtonsPanel();  //Initial values for dayButtons and states (should retrieved from SharedPref)

        //setUpAlarmModePanel();
        //setUpAlarmDaysPanel();

        clockEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                clockEditText.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));
                cursorPosition = 0;
            }
        });

        clockEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                clockEditText.setSelection(cursorPosition);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isTimeChangedByHand){
                    isTimeChangedByHand = false;

                    char inputSymbol = s.charAt(start);
                    String nString;

                    if (cursorPosition == 0){
                        nString = inputSymbol + "0" + separator + "00";
                        clockEditText.setText(nString);
                    }
                    else if (cursorPosition == 1){
                        nString = s.toString().charAt(cursorPosition) + String.valueOf(inputSymbol) + separator + "00";
                        Toast.makeText(AlarmActivity.this, "charAt0 - " + s.toString().charAt(0), Toast.LENGTH_SHORT).show();
                        clockEditText.setText(nString);
                    }
                    else if (cursorPosition == 2){
                        nString = String.valueOf(s.toString().charAt(1)) + String.valueOf(s.toString().charAt(2)) + separator + inputSymbol + "0";
                        clockEditText.setText(nString);
                    }
                    else if (cursorPosition == 3){
                        nString = String.valueOf(s.toString().charAt(1)) + String.valueOf(s.toString().charAt(2)) +
                                separator + String.valueOf(s.toString().charAt(s.length() - 2)) + inputSymbol;
                        clockEditText.setText(nString);

                        closeKeyboard();
                        clockEditText.clearFocus();
                        clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isTimeChangedByHand){
                    cursorPosition++;
                    setTimeSwitch.setChecked(false);
                }
                isTimeChangedByHand = true;

            }
        });


        setTimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchOnOffClick(buttonView, isChecked);
            }
        });

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(AlarmActivity.this, "volume changed to : " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        //saveAlarmDataPreferences();

        Integer hour, minute;

        String hourS = clockEditText.getText().toString().split(" : ")[0];
        if (hourS.charAt(0) == '0'){
            hour = Integer.parseInt(String.valueOf(hourS.charAt(1)));
        }
        else {
            hour = Integer.parseInt(hourS);
        }

        String minuteS = clockEditText.getText().toString().split(" : ")[1];
        if (minuteS.charAt(0) == '0'){
            minute = Integer.parseInt(String.valueOf(minuteS.charAt(1)));
        }
        else {
            minute = Integer.parseInt(minuteS);
        }

        boolean isActive = setTimeSwitch.isChecked();
        int currentVolumePower = volumeSeekBar.getProgress();

        dataAccessManager.saveHour(hour);
        dataAccessManager.saveMinute(minute);
        dataAccessManager.saveIsActive(isActive);
        dataAccessManager.saveVolumePower(currentVolumePower);
        dataAccessManager.saveMaxVolumePower(maxVolumePower);

    }

    private void initViews(){
        clockEditText = findViewById(R.id.clockEditText);
        increaseVolumeTextView = findViewById(R.id.increaseVolumeTextView);

        setTimeSwitch = findViewById(R.id.setTimeSwitch);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);

        //Initialising timers volume buttons
        neverVolumeTimersButton = findViewById(R.id.neverVolumeTimersButton);
        c30sVolumeTimersButton = findViewById(R.id.c30sVolumeTimersButton);
        c15sVolumeTimersButton = findViewById(R.id.c15sVolumeTimersButton);
        c45sVolumeTimersButton = findViewById(R.id.c45sVolumeTimersButton);
        c60sVolumeTimersButton = findViewById(R.id.c60sVolumeTimersButton);

        //Initialising alarm mode panel buttons
        songPlusVibrateButton = findViewById(R.id.soundAndVibroButton);
        songImageButton = findViewById(R.id.soundImageButton);
        vibrateImageButton = findViewById(R.id.vibroImageButton);
        noSoundImageButton = findViewById(R.id.noSoundButton);

        //Initialising alarm mode days panel buttons
        mondayButton = findViewById(R.id.mondayButton);
        tuesdayButton = findViewById(R.id.tuesdayButton);
        wednesdayButton = findViewById(R.id.wednesdayButton);
        thursdayButton = findViewById(R.id.thursdayButton);
        fridayButton = findViewById(R.id.fridayButton);
        saturdayButton = findViewById(R.id.saturdayButton);
        sundayButton = findViewById(R.id.sundayButton);
    }

    private void initDayButtonsPanel(){
//        isDayActiveArray = new boolean[]{false, false, false, false, false, false, false};
//        for (int i=0;i<7;i++){
//            isDayActiveArray[i] = alarmController.getDay(i);
//        }
        dayButtonArray = new Button[]{mondayButton, tuesdayButton, wednesdayButton, thursdayButton,
                fridayButton, saturdayButton, sundayButton};
    }

    //********   activity transition   *********

    public void musicMenuButtonClicked(View view) {
        Intent musicListActivityIntent = new Intent(AlarmActivity.this, MusicListActivity.class);
        startActivity(musicListActivityIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void infoMenuButtonClicked(View view) {
        Intent infoActivityIntent = new Intent(AlarmActivity.this, InfoActivity.class);
        startActivity(infoActivityIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    //------------------------------------------

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }

    //*********   Detecting volume increase change   ***********

    private void setUpVolumeIncreasePanel(){

        dataAccessManager.saveVolumeSelectedMode(alarmData.getVolumeSelectedMode());
        volumeIncreaseMode = alarmData.getVolumeSelectedMode();

        if (volumeIncreaseMode == VOL_INCREASE_MODE_NEVER){
            neverVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_never_selected));
            neverVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));

            c15sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c15sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c30sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c30sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c45sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c45sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c60sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c60sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
        }
        else if (volumeIncreaseMode == VOL_INCREASE_MODE_15s){
            neverVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_never_unselected));
            neverVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c15sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_selected));
            c15sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));

            c30sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c30sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c45sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c45sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c60sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c60sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
        }
        else if (volumeIncreaseMode == VOL_INCREASE_MODE_30s){
            neverVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_never_unselected));
            neverVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c15sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c15sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c30sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_selected));
            c30sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));

            c45sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c45sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c60sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c60sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
        }
        else if (volumeIncreaseMode == VOL_INCREASE_MODE_45s){
            neverVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_never_unselected));
            neverVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c15sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c15sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c30sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c30sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c45sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_selected));
            c45sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));

            c60sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c60sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
        }
        else if (volumeIncreaseMode == VOL_INCREASE_MODE_60s){
            neverVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_never_unselected));
            neverVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c15sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c15sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c30sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c30sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c45sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_unselected));
            c45sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

            c60sVolumeTimersButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_increase_alarm_selected));
            c60sVolumeTimersButton.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));
        }

    }


    //*********   Detecting alarm mode change   *************
    //DONE: add alarm mode to sharedPreferences

    private void setUpAlarmModePanel(){

        dataAccessManager.saveAlarmSelectedMode(alarmData.getAlarmSelectedMode());
        alarmSelectedMode = dataAccessManager.loadAlarmSelectedMode();

        if (alarmSelectedMode == ALARM_SELECTED_MODE_SOUND_VIBRATE){
            songPlusVibrateButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_selected));
            songImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
            vibrateImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
            noSoundImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
        }
        else if (alarmSelectedMode == ALARM_SELECTED_MODE_SOUND_ONLY){
            songPlusVibrateButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
            songImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_selected));
            vibrateImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
            noSoundImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
        }
        else if (alarmSelectedMode == ALARM_SELECTED_MODE_VIBRATE_ONLY){
            songPlusVibrateButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
            songImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
            vibrateImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_selected));
            noSoundImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
        }
        else {
            songPlusVibrateButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
            songImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
            vibrateImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_unselected));
            noSoundImageButton.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.button_alarmmode_sv_selected));
        }
    }

    //*********   Time set switch  *********** (by Shymon)

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void switchOnOffClick(CompoundButton buttonView, boolean isChecked){
        if (!isChecked) {

            alarmData.setAsNotActive();
            dataAccessManager.saveIsActive(false);

            cancelAlarm();

//            try{
//                String timeText = clockEditText.getText().toString();
//                String[] numbers = timeText.split(" : ");
//
//                int hour, minute;
//
//                try {
//                    hour = Integer.parseInt(numbers[0]);
//                    minute = Integer.parseInt(numbers[1]);
//                }
//                catch (NumberFormatException e)
//                {
//                    Toast.makeText(this, "Exception: number-format", Toast.LENGTH_SHORT).show();
//                    hour = 0;
//                    minute = 0;
//                }
//
//                AlarmController.setHour(hour);
//                AlarmController.setMinute(minute);
//
//                isTimeChangedByHand = false;
//
//                //clockEditText.setText(AlarmController.getTimeString());
//            }
//            catch (Exception e){
//
//            }
//
//            cancelAlarm();

        }
        else {

            alarmData.setAsActive();

//            Toast.makeText(this, clockEditText.getText().toString(), Toast.LENGTH_SHORT).show();

            String timeText = clockEditText.getText().toString();
            String[] numbers = timeText.split(" : ");

            int hour, minute;

            try{
                hour = Integer.parseInt(numbers[0]);
                minute = Integer.parseInt(numbers[1]);
            }
            catch (Exception e){
                Toast.makeText(this, "parseInt exception", Toast.LENGTH_SHORT).show();
                hour = 0;
                minute = 0;
            }

            Toast.makeText(this, "" + hour + ":" + minute + " ready to set", Toast.LENGTH_SHORT).show();

//            alarmController.setHour(hour);
//            alarmController.setMinute(minute);

            dataAccessManager.saveHour(hour);
            dataAccessManager.saveMinute(minute);
            dataAccessManager.saveIsActive(true);

            Calendar c = Calendar.getInstance();

            c.set(Calendar.HOUR_OF_DAY, dataAccessManager.loadHour());
            c.set(Calendar.MINUTE, dataAccessManager.loadMinute());
            c.set(Calendar.SECOND, 0);

            setAlarm(c);

//            //clockEditText.setText(AlarmController.getTimeString());
//            clockEditText.setText("20:09");
//
//            Calendar c = Calendar.getInstance();
//
//            //alarm data is null here so returned
//
////            if (alarmData == null){
////                Toast.makeText(this, "alarmData is null", Toast.LENGTH_SHORT).show();
////                return;
////            }
////
////            if (alarmData.getHour() == null){
////                Toast.makeText(this, "hour is null", Toast.LENGTH_SHORT).show();
////                return;
////            }
////
////            if (alarmData.getMinute() == null){
////                Toast.makeText(this, "minute is null", Toast.LENGTH_SHORT).show();
////                return;
////            }
////
////            c.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
////            c.set(Calendar.MINUTE, alarmData.getMinute());
////            c.set(Calendar.SECOND, 0);
//
//            c.set(Calendar.HOUR_OF_DAY, 20);
//            c.set(Calendar.MINUTE, 9);
//            c.set(Calendar.SECOND, 0);
//
//            setAlarm(c);

        }

    }

    //Commit 1.0.13 - making setAlarm() function
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setAlarm(Calendar c){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent activateAlarmIntent = new Intent(this, AlertReceiver.class);

        int mode = alarmData.getAlarmSelectedMode();

        activateAlarmIntent.putExtra("mode", mode);
        PendingIntent activateAlarmPendingIntent = PendingIntent.getBroadcast(this, 1,
                activateAlarmIntent, 0);

        if (c.before(Calendar.getInstance())){
            c.add(Calendar.DATE, 1);
        }

        String alarmInfo = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        int aMode = alarmData.getAlarmSelectedMode();
        Toast.makeText(this, "was set for: " + alarmInfo + " with mode " + aMode, Toast.LENGTH_SHORT).show();

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, activateAlarmPendingIntent);
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), activateAlarmPendingIntent);
    }

    private void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent activateAlarmIntent = new Intent(this, AlertReceiver.class);
        PendingIntent deactivateAlarmPendingIntent = PendingIntent.getBroadcast(this, 1,
                activateAlarmIntent, 0);

        Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();

        alarmManager.cancel(deactivateAlarmPendingIntent);
    }

    //*********   Volume increase buttons clicked   **********

    public void neverVolumeButtonClicked(View view) {
        //volumeIncreaseMode = 0;
        alarmData.setVolumeSelectedMode(0);
        setUpVolumeIncreasePanel();
    }

    public void c15sVolumeButtonClicked(View view) {
        //volumeIncreaseMode = 1;
        alarmData.setVolumeSelectedMode(1);
        setUpVolumeIncreasePanel();
    }

    public void c30sVolumeButtonClicked(View view) {
        //volumeIncreaseMode = 2;
        alarmData.setVolumeSelectedMode(2);
        setUpVolumeIncreasePanel();
    }

    public void c45sVolumeButtonClicked(View view) {
        //volumeIncreaseMode = 3;
        alarmData.setVolumeSelectedMode(3);
        setUpVolumeIncreasePanel();
    }

    public void c60sVolumeButtonClicked(View view) {
        //volumeIncreaseMode = 4;
        alarmData.setVolumeSelectedMode(4);
        setUpVolumeIncreasePanel();
    }

    //*********   Alarm mode buttons clicked   **********

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void soundPlusVibrateButtonClicked(View view) {
        //alarmSelectedMode = 0;
        alarmData.setAlarmSelectedMode(0);
        setUpAlarmModePanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void soundButtonClicked(View view) {
        //alarmSelectedMode = 1;
        alarmData.setAlarmSelectedMode(1);
        setUpAlarmModePanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void vibrateButtonClicked(View view) {
        //alarmSelectedMode = 2;
        alarmData.setAlarmSelectedMode(2);
        setUpAlarmModePanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void noSoundButtonClicked(View view) {
        //alarmSelectedMode = 3;
        alarmData.setAlarmSelectedMode(3);
        setUpAlarmModePanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    //**********   Setting up alarm days views   *************

    private void setUpAlarmDaysPanel(){

        dataAccessManager.saveDays(alarmData.getDays());
        isDayActiveArray = dataAccessManager.loadDays();

        for (int i = 0; i < isDayActiveArray.length; i++){
            if (isDayActiveArray[i]){
                dayButtonArray[i].setBackgroundDrawable(getResources().
                        getDrawable(R.color.colorWidgetBackground));
                dayButtonArray[i].setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));
            }
            else {
                dayButtonArray[i].setBackgroundDrawable(getResources().
                        getDrawable(R.drawable.button_day_unselected));
                dayButtonArray[i].setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
            }
        }
    }

    //*************   Alarm days buttons clicked   ***************

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void mondayButtonClicked(View view) {
//        if (isDayActiveArray[0]){
//            isDayActiveArray[0] = false;
//        }
//        else {
//            isDayActiveArray[0] = true;
//        }

        boolean previousState = dataAccessManager.loadDay(0);
        if (previousState){
//            dataAccessManager.saveDay(0, false);
            alarmData.setDay(false, 0);
        }
        else{
            alarmData.setDay(true, 0);
        }

        // Probably high weight -> do saving in onStop()
        setUpAlarmDaysPanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void tuesdayButtonClicked(View view) {
//        if (isDayActiveArray[1]){
//            isDayActiveArray[1] = false;
//        }
//        else {
//            isDayActiveArray[1] = true;
//        }

        boolean previousState = dataAccessManager.loadDay(1);
        if (previousState){
            alarmData.setDay(false, 1);
        }
        else{
            alarmData.setDay(true, 1);
        }

        setUpAlarmDaysPanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void wednesdayButtonClicked(View view) {

        boolean previousState = dataAccessManager.loadDay(2);
        if (previousState){
            alarmData.setDay(false, 2);
        }
        else{
            alarmData.setDay(true, 2);
        }

        setUpAlarmDaysPanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void thursdayButtonClicked(View view) {

        boolean previousState = dataAccessManager.loadDay(3);
        if (previousState){
            alarmData.setDay(false, 3);
        }
        else{
            alarmData.setDay(true, 3);
        }

        setUpAlarmDaysPanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void fridayButtonClicked(View view) {

        boolean previousState = dataAccessManager.loadDay(4);
        if (previousState){
            alarmData.setDay(false, 4);
        }
        else{
            alarmData.setDay(true, 4);
        }

        setUpAlarmDaysPanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saturdayButtonClicked(View view) {

        boolean previousState = dataAccessManager.loadDay(5);
        if (previousState){
            alarmData.setDay(false, 5);
        }
        else{
            alarmData.setDay(true, 5);
        }

        setUpAlarmDaysPanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sundayButtonClicked(View view) {

        boolean previousState = dataAccessManager.loadDay(6);
        if (previousState){
            alarmData.setDay(false, 6);
        }
        else{
            alarmData.setDay(true, 6);
        }

        setUpAlarmDaysPanel();
        setTimeSwitch.setChecked(false);
        switchOnOffClick(setTimeSwitch, false);
    }

    //Animation for moving for volume buttons
    public void moreVolumeTimersButtonClicked(View view) throws InterruptedException {

        if (isMoreVolumeButtonsActive){
            isMoreVolumeButtonsActive = false;

            ObjectAnimator volIncreaseTextAnimator = ObjectAnimator.ofFloat(increaseVolumeTextView, "translationX", 0f);
            volIncreaseTextAnimator.setRepeatCount(0);
            volIncreaseTextAnimator.setDuration(300);

            ObjectAnimator neverVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(neverVolumeTimersButton, "translationX", 0f);
            neverVolIncreaseButtonAnimator.setRepeatCount(0);
            neverVolIncreaseButtonAnimator.setDuration(400);
            neverVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c30sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c30sVolumeTimersButton, "translationX", 0f);
            c30sVolIncreaseButtonAnimator.setRepeatCount(0);
            c30sVolIncreaseButtonAnimator.setDuration(400);
            c30sVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c15sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c15sVolumeTimersButton, "translationX", 0f);
            //c15sVolumeTimersButton.setAlpha(1.0f);
            c15sVolIncreaseButtonAnimator.setRepeatCount(0);
            c15sVolIncreaseButtonAnimator.setDuration(400);
            c15sVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c45sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c45sVolumeTimersButton, "translationX", 0f);
            //c45sVolumeTimersButton.setAlpha(1.0f);
            c45sVolIncreaseButtonAnimator.setRepeatCount(0);
            c45sVolIncreaseButtonAnimator.setDuration(400);
            c45sVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c60sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c60sVolumeTimersButton, "translationX", 0f);
            //c60sVolumeTimersButton.setAlpha(1.0f);
            c60sVolIncreaseButtonAnimator.setRepeatCount(0);
            c60sVolIncreaseButtonAnimator.setDuration(400);
            c60sVolIncreaseButtonAnimator.setStartDelay(100);

            List<Animator> buttonListAnimator = new ArrayList<>();
            buttonListAnimator.add(volIncreaseTextAnimator);
            buttonListAnimator.add(neverVolIncreaseButtonAnimator);
            buttonListAnimator.add(c30sVolIncreaseButtonAnimator);
            buttonListAnimator.add(c15sVolIncreaseButtonAnimator);
            buttonListAnimator.add(c45sVolIncreaseButtonAnimator);
            buttonListAnimator.add(c60sVolIncreaseButtonAnimator);

            AnimatorSet set = new AnimatorSet();
            //set.playSequentially(buttonListAnimator);
            set.playTogether(buttonListAnimator);
            set.start();

        }
        else {
            isMoreVolumeButtonsActive = true;

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int displayWidth = size.x;
            float width50Percent = size.x * 0.5f;

            ObjectAnimator volIncreaseTextAnimator = ObjectAnimator.ofFloat(increaseVolumeTextView, "translationX", -size.x * 0.5f);
            volIncreaseTextAnimator.setRepeatCount(0);
            volIncreaseTextAnimator.setDuration(300);

            ObjectAnimator neverVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(neverVolumeTimersButton, "translationX", -size.x * 0.4f);
            neverVolIncreaseButtonAnimator.setRepeatCount(0);
            neverVolIncreaseButtonAnimator.setDuration(400);
            neverVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c30sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c30sVolumeTimersButton, "translationX", -size.x * 0.4f);
            c30sVolIncreaseButtonAnimator.setRepeatCount(0);
            c30sVolIncreaseButtonAnimator.setDuration(400);
            c30sVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c15sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c15sVolumeTimersButton, "translationX", -size.x * 0.4f);
            c15sVolumeTimersButton.setAlpha(1.0f);
            c15sVolIncreaseButtonAnimator.setRepeatCount(0);
            c15sVolIncreaseButtonAnimator.setDuration(400);
            c15sVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c45sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c45sVolumeTimersButton, "translationX", -size.x * 0.265f);
            c45sVolumeTimersButton.setAlpha(1.0f);
            c45sVolIncreaseButtonAnimator.setRepeatCount(0);
            c45sVolIncreaseButtonAnimator.setDuration(400);
            c45sVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c60sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c60sVolumeTimersButton, "translationX", -size.x * 0.13f);
            c60sVolumeTimersButton.setAlpha(1.0f);
            c60sVolIncreaseButtonAnimator.setRepeatCount(0);
            c60sVolIncreaseButtonAnimator.setDuration(400);
            c60sVolIncreaseButtonAnimator.setStartDelay(100);

            List<Animator> buttonListAnimator = new ArrayList<>();
            buttonListAnimator.add(volIncreaseTextAnimator);
            buttonListAnimator.add(neverVolIncreaseButtonAnimator);
            buttonListAnimator.add(c30sVolIncreaseButtonAnimator);
            buttonListAnimator.add(c15sVolIncreaseButtonAnimator);
            buttonListAnimator.add(c45sVolIncreaseButtonAnimator);
            buttonListAnimator.add(c60sVolIncreaseButtonAnimator);

            AnimatorSet set = new AnimatorSet();
            //set.playSequentially(buttonListAnimator);
            set.playTogether(buttonListAnimator);
            set.start();
        }

    }

    // Property for animation (object Animator)
    Property<View, Float> fractionXProperty = new Property<View, Float>(Float.TYPE, "translation_x_fraction") {
        @Override
        public Float get(View object) {
            return object.getWidth() <= 0 ? 0 : object.getTranslationX() / object.getWidth();
        }

        @Override
        public void set(View object, Float value) {
            object.setTranslationX(object.getWidth() * value);
        }
    };

    //********** Loading AlarmData preferences **********

    //Called when app is firstly launched
    public void setDefaultAlarmDataPreferences(){
        alarmData = new AlarmData();
    }

    //Loading from sharedPreferences
    public void loadAlarmDataPreferences(){
        //we have fields in this activity and assign them with values from storage

        String alarmDataJson = sharedPreferences.getString(ALARM_DATA_KEY_PREFERENCES, "null");

        if (!alarmDataJson.equals("null")){
            Gson gson = new Gson();
            alarmData = gson.fromJson(alarmDataJson, AlarmData.class);
        }
        else {
            Toast.makeText(this, "sharedPref is null: Strange stuff", Toast.LENGTH_SHORT).show();
        }

    }

    

    //Called when activity onStop() or onDestroy()
    public void saveAlarmDataPreferences(){
        //TODO: Scan views and update alarmData variable then save it to storage

        Integer hour, minute;

        String hourS = clockEditText.getText().toString().split(" : ")[0];
        if (hourS.charAt(0) == '0'){
            hour = Integer.parseInt(String.valueOf(hourS.charAt(1)));
        }
        else {
            hour = Integer.parseInt(hourS);
        }

        String minuteS = clockEditText.getText().toString().split(" : ")[1];
        if (minuteS.charAt(0) == '0'){
            minute = Integer.parseInt(String.valueOf(minuteS.charAt(1)));
        }
        else {
            minute = Integer.parseInt(minuteS);
        }

        boolean isActive = setTimeSwitch.isChecked();

        //volumePower - do some stuff with Bar
        int volumePower = volumeSeekBar.getProgress();
        //volumeIncreaseMode exists

        //alarmSelectedMode exists
        //Days exists

        //Saving to sharedPreferences
        AlarmData updatedAlarmData = new AlarmData();
        updatedAlarmData.setHour(hour);
        updatedAlarmData.setMinute(minute);
        if (isActive) { updatedAlarmData.setAsActive(); }
        else { updatedAlarmData.setAsNotActive(); }
        updatedAlarmData.setVolumePower(volumePower);

        //Saving
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String alarmDataJson = gson.toJson(alarmData);
        editor.putString(ALARM_DATA_KEY_PREFERENCES, alarmDataJson);
        editor.apply();
        Toast.makeText(this, "alarm-pref saved to storage", Toast.LENGTH_SHORT).show();

    }

}