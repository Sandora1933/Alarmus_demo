package com.example.alarmus_demo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alarmus_demo.AlarmController;
import com.example.alarmus_demo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AlarmActivity extends AppCompatActivity {




    //************  Views  ****************

    Switch setTimeSwitch;

    EditText clockEditText;

    //Views - Alarm volume panel
    TextView increaseVolumeTextView;
    Button neverVolumeTimersButton, c30sVolumeTimersButton, c15sVolumeTimersButton, c45sVolumeTimersButton,
            c60sVolumeTimersButton;

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

    //**************   Variables   *****************

    boolean isTimeChangedByHand; //Detects when timeEditText is being changed by user (not by system)
    int cursorPosition; //Cursor for timeEditText
    int alarmSelectedMode; //0: sound+vibrate, 1: sound, 2: vibrate, 3: noSound
    boolean[] isDayActiveArray; //Array of states (active or not) of days' buttons
    boolean isMoreVolumeButtonsActive;


    SharedPreferences sharedPreferences;    //Storage

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        initViews();


        SharedPreferences sharedPref = getSharedPreferences("alarm_data", MODE_PRIVATE);
        AlarmController.setup(sharedPref);

        isTimeChangedByHand=false;
        clockEditText.setText(AlarmController.getTimeString());

        //DONE: get from SharedPreferences
        //Initial alarm mode (but this data should be retrieved from sharedPreferences)
        alarmSelectedMode = AlarmController.getAlarmSelectedMode();

        //Initial settings for timeEditText
        isTimeChangedByHand = true;
        cursorPosition = 0;
        final String separator = " : ";

        isMoreVolumeButtonsActive = false;

        //DONE: get from SharedPreferences
        initDayButtonsPanel();  //Initial values for dayButtons and states (should retrieved from SharedPref)

        setUpAlarmModePanel();
        setUpAlarmDaysPanel();

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
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchOnOffClick(buttonView,isChecked);
            }
        });
    }

    private void initViews(){
        clockEditText = findViewById(R.id.clockEditText);
        increaseVolumeTextView = findViewById(R.id.increaseVolumeTextView);

        setTimeSwitch = findViewById(R.id.setTimeSwitch);

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
        isDayActiveArray = new boolean[]{false, false, false, false, false, false, false};
        for (int i=0;i<7;i++){
            isDayActiveArray[i]=AlarmController.getDay(i);
        }
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

    //*********   Detecting alarm mode change   *************
    //DONE: add alarm mode to sharedPreferences

    private void setUpAlarmModePanel(){
        AlarmController.setAlarmSelectedMode(alarmSelectedMode);
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

    public void switchOnOffClick(CompoundButton buttonView, boolean isChecked){
        if (isChecked) {
            try{
                String timeText=clockEditText.getText().toString();
                String[] numbers=timeText.split(" : ");
                int hour;
                try {
                    hour = Integer.parseInt(numbers[0]);
                }
                catch (NumberFormatException e)
                {
                    hour = 0;
                }
                int minute;
                try {
                    minute = Integer.parseInt(numbers[1]);
                }
                catch (NumberFormatException e)
                {
                    minute = 0;
                }
                AlarmController.setHour(hour);
                AlarmController.setMinute(minute);
                isTimeChangedByHand=false;
                clockEditText.setText(AlarmController.getTimeString());
            }
            catch (Exception e){

            }
        } else {
            clockEditText.setText(AlarmController.getTimeString());
        }

    }

    //*********   Alarm mode buttons clicked   **********

    public void soundPlusVibrateButtonClicked(View view) {
        alarmSelectedMode = 0;
        setUpAlarmModePanel();
    }

    public void soundButtonClicked(View view) {
        alarmSelectedMode = 1;
        setUpAlarmModePanel();
    }

    public void vibrateButtonClicked(View view) {
        alarmSelectedMode = 2;
        setUpAlarmModePanel();
    }

    public void noSoundButtonClicked(View view) {
        alarmSelectedMode = 3;
        setUpAlarmModePanel();
    }

    //**********   Setting up alarm days views   *************

    private void setUpAlarmDaysPanel(){

        AlarmController.setDays(isDayActiveArray);

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

    public void mondayButtonClicked(View view) {
        if (isDayActiveArray[0]){
            isDayActiveArray[0] = false;
        }
        else {
            isDayActiveArray[0] = true;
        }
        setUpAlarmDaysPanel();
    }

    public void tuesdayButtonClicked(View view) {
        if (isDayActiveArray[1]){
            isDayActiveArray[1] = false;
        }
        else {
            isDayActiveArray[1] = true;
        }
        setUpAlarmDaysPanel();
    }

    public void wednesdayButtonClicked(View view) {
        if (isDayActiveArray[2]){
            isDayActiveArray[2] = false;
        }
        else {
            isDayActiveArray[2] = true;
        }
        setUpAlarmDaysPanel();
    }

    public void thursdayButtonClicked(View view) {
        if (isDayActiveArray[3]){
            isDayActiveArray[3] = false;
        }
        else {
            isDayActiveArray[3] = true;
        }
        setUpAlarmDaysPanel();
    }

    public void fridayButtonClicked(View view) {
        if (isDayActiveArray[4]){
            isDayActiveArray[4] = false;
        }
        else {
            isDayActiveArray[4] = true;
        }
        setUpAlarmDaysPanel();
    }

    public void saturdayButtonClicked(View view) {
        if (isDayActiveArray[5]){
            isDayActiveArray[5] = false;
        }
        else {
            isDayActiveArray[5] = true;
        }
        setUpAlarmDaysPanel();
    }

    public void sundayButtonClicked(View view) {
        if (isDayActiveArray[6]){
            isDayActiveArray[6] = false;
        }
        else {
            isDayActiveArray[6] = true;
        }
        setUpAlarmDaysPanel();
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

            ObjectAnimator volIncreaseTextAnimator = ObjectAnimator.ofFloat(increaseVolumeTextView, "translationX", -450f);
            volIncreaseTextAnimator.setRepeatCount(0);
            volIncreaseTextAnimator.setDuration(300);

            ObjectAnimator neverVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(neverVolumeTimersButton, "translationX", -450f);
            neverVolIncreaseButtonAnimator.setRepeatCount(0);
            neverVolIncreaseButtonAnimator.setDuration(400);
            neverVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c30sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c30sVolumeTimersButton, "translationX", -450f);
            c30sVolIncreaseButtonAnimator.setRepeatCount(0);
            c30sVolIncreaseButtonAnimator.setDuration(400);
            c30sVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c15sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c15sVolumeTimersButton, "translationX", -450f);
            c15sVolumeTimersButton.setAlpha(1.0f);
            c15sVolIncreaseButtonAnimator.setRepeatCount(0);
            c15sVolIncreaseButtonAnimator.setDuration(400);
            c15sVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c45sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c45sVolumeTimersButton, "translationX", -310f);
            c45sVolumeTimersButton.setAlpha(1.0f);
            c45sVolIncreaseButtonAnimator.setRepeatCount(0);
            c45sVolIncreaseButtonAnimator.setDuration(400);
            c45sVolIncreaseButtonAnimator.setStartDelay(100);

            ObjectAnimator c60sVolIncreaseButtonAnimator = ObjectAnimator.ofFloat(c60sVolumeTimersButton, "translationX", -170f);
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
}