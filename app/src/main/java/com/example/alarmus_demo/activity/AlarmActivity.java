package com.example.alarmus_demo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.alarmus_demo.R;

public class AlarmActivity extends AppCompatActivity {

    //************  Views  ****************

    EditText clockEditText;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        initViews();

        //TODO: get from SharedPreferences
        //Initial alarm mode (but this data should be retrieved from sharedPreferences)
        alarmSelectedMode = ALARM_SELECTED_MODE_SOUND_ONLY;

        //Initial settings for timeEditText
        isTimeChangedByHand = true;
        cursorPosition = 0;
        final String separator = " : ";

        //TODO: get from SharedPreferences
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
                        nString = inputSymbol + "8" + separator + "88";
                        clockEditText.setText(nString);
                    }
                    else if (cursorPosition == 1){
                        nString = s.toString().charAt(cursorPosition) + String.valueOf(inputSymbol) + separator + "88";
                        Toast.makeText(AlarmActivity.this, "charAt0 - " + s.toString().charAt(0), Toast.LENGTH_SHORT).show();
                        clockEditText.setText(nString);
                    }
                    else if (cursorPosition == 2){
                        nString = String.valueOf(s.toString().charAt(1)) + String.valueOf(s.toString().charAt(2)) + separator + inputSymbol + "8";
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
                }
                isTimeChangedByHand = true;
            }
        });

    }

    private void initViews(){
        clockEditText = findViewById(R.id.clockEditText);

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
    //TODO: add alarm mode to sharedPreferences

    private void setUpAlarmModePanel(){
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

}