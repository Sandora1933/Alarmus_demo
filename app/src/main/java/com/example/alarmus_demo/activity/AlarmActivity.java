package com.example.alarmus_demo.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Property;
import android.view.Display;
import android.view.KeyEvent;
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

import com.example.alarmus_demo.AlarmData;
import com.example.alarmus_demo.AlertReceiver;
import com.example.alarmus_demo.DataAccessManager;
import com.example.alarmus_demo.NumericKeyBoardTransformationMethod;
import com.example.alarmus_demo.R;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {

    //************  Views  ****************

    Switch setTimeSwitch;
    EditText clockEditText;
    TextView setTimeTextView;

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

    public static final int OVERLAY_REQUEST_CODE = 512;

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

    //**************   Variables   *****************

    boolean isTimeChangedByHand; //Detects when timeEditText is being changed by user (not by system)
    int cursorPosition; //Cursor for timeEditText
    int alarmSelectedMode; //0: sound+vibrate, 1: sound, 2: vibrate, 3: noSound
    int volumeIncreaseMode; //0: never, 1: 15s, 2: 30s, 3: 45s, 4: 60s
    double volumePowerPercent;  // 0-100% of volume power value (Does not correlate with system alarm value yet)
    int maxVolumePower;
    boolean[] isDayActiveArray; //Array of states (active or not) of days' buttons
    boolean isMoreVolumeButtonsActive;

    boolean enterClickAllowed;

    Drawable switchThumbDrawable, switchTrackDrawable;

    SharedPreferences sharedPreferences;    //Storage

    AlarmData alarmData;
    DataAccessManager dataAccessManager;

    // To work with volume seek bar
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_new);

        // Request permission for Xiaomi devices (Show on lockScreen & Display Pop-Up)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                if ("xiaomi".equals(Build.MANUFACTURER.toLowerCase(Locale.ROOT))) {
                    final Intent intent =new Intent("miui.intent.action.APP_PERM_EDITOR");
                    intent.setClassName("com.miui.securitycenter",
                            "com.miui.permcenter.permissions.PermissionsEditorActivity");
                    intent.putExtra("extra_pkgname", getPackageName());
                    new AlertDialog.Builder(this)
                            .setTitle("Please Enable the additional permissions")
                            .setMessage("You will not receive notifications while the app is in background if you disable these permissions")
                            .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(intent);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setCancelable(false)
                            .show();
                }else {
                    Intent overlaySettings = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(overlaySettings, OVERLAY_REQUEST_CODE);
                }
            }
        }

        //----------------------

        initViews();    // Initializing all the views used in Activity
        initDayButtonsArray();      // Initializing the array field Buttons[]

        // Initializing storage and data access manager
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);    // Storage
        dataAccessManager = new DataAccessManager(sharedPreferences);   // storage <-> Activity manager

        // ------------------------------------------
        // filling alarmData object (in order to use)
        // ------------------------------------------

        alarmData = dataAccessManager.buildAlarmDataObject();

        // Initial power is 75% of max
        alarmData.setVolumePower((int) ((int) maxVolumePower * 0.75));

        // ----------------------------------------------
        // Setting up views according to alarmData object
        // ----------------------------------------------
        //TODO: organise into distinct method setUpWidgets()

        // Set up clockEditText
        clockEditText.setText(alarmData.getTimeString());
        clockEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        clockEditText.setTransformationMethod(new NumericKeyBoardTransformationMethod());

        // Set up timeSwitch
        setTimeSwitch.setChecked(alarmData.isActive());

        // Set up seek bar and all volume stuff
        // TODO: transfer to distinct method setUpSeekBar()
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolumePower = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

        volumeSeekBar.setMax(maxVolumePower);

        //volumeSeekBar.setProgress(alarmData.getVolumePower());
        volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_ALARM));

        // Set up volume-increase panel
        setUpVolumeIncreasePanel();

        // Set up alarm modes panel
        setUpAlarmModePanel();

        // Set up alarm days panel
        setUpAlarmDaysPanel();

        // ---------------------------
        // some other settings needed
        // ---------------------------

        isTimeChangedByHand=false;
        enterClickAllowed = false;

        //Initial settings for clockEditText
        isTimeChangedByHand = true;
        cursorPosition = 0;
        final String separator = ":";

        // Additional setting for increase volume panel
        isMoreVolumeButtonsActive = false;

        // ------------------------------
        // Setting interfaces
        // ------------------------------

        clockEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus){
                    clockEditText.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));
                    setTimeTextView.setTextColor(getResources().getColor(R.color.colorSelectedWidgetBackground));
                    setTimeTextView.setClickable(false);

                    // Remember drawables
                    switchThumbDrawable = setTimeSwitch.getThumbDrawable();
                    switchTrackDrawable = setTimeSwitch.getTrackDrawable();


                    setTimeSwitch.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_not_active));
                    //setTimeSwitch.setTrackDrawable(getResources().getDrawable(R.drawable.switch_track_not_active));
                    setTimeSwitch.setClickable(false);
                    //clockEditText.setText("02:00");
                    cursorPosition = 0;
                }
                else{

                    setTimeTextView.setTextColor(getResources().getColor(R.color.colorSelectedTextAndIcon));
                    setTimeTextView.setClickable(true);

                    setTimeSwitch.setThumbDrawable(switchThumbDrawable);
                    //setTimeSwitch.setTrackDrawable(switchTrackDrawable);
                    setTimeSwitch.setClickable(true);

                    closeKeyboard();
                    clockEditText.clearFocus();
                    clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                }


            }
        });

        clockEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER) && enterClickAllowed){


                    Toast.makeText(AlarmActivity.this, "enter allowed", Toast.LENGTH_SHORT).show();

                    enterClickAllowed = false;

                    closeKeyboard();
                    clockEditText.clearFocus();
                    clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

                    return true;

                }

                return false;
            }
        });

        clockEditText.addTextChangedListener(new TextWatcher() {

            boolean isFirstIsOne = false;   // false if 1 and true if 2 :)
            boolean isSecondIsFourOrFive = false;
            boolean isHoursProblem = false; // false if first digit is 3-9 so it is surely 3-9 am
            boolean isCase111Out = false;
            boolean isCase22Out = false;
            boolean isCase221Out = false;
            boolean isCase21Out = false;
            boolean isCase212Out = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int pos = clockEditText.getText().toString().length() - cursorPosition - 1;
                //Toast.makeText(AlarmActivity.this, "pos:" + pos, Toast.LENGTH_SHORT).show();
                clockEditText.setSelection(5);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isTimeChangedByHand){
                    isTimeChangedByHand = false;

                    char inputSymbol = s.charAt(start);
                    String nString;

                    //TODO: Optimization (Remove repeated code)

                    if (cursorPosition == 0){

                        // Approve initial state
                        isFirstIsOne = false;
                        isSecondIsFourOrFive = false;
                        isHoursProblem = false;
                        isCase111Out = false;
                        isCase22Out = false;
                        isCase221Out = false;
                        isCase21Out = false;
                        isCase212Out = false;


                        //nString = inputSymbol + "0" + separator + "00";

                        if (inputSymbol == '1' || inputSymbol == '2'){
                            isHoursProblem = true;

                            isFirstIsOne = inputSymbol == '1';

                            Toast.makeText(AlarmActivity.this, "hours problem true", Toast.LENGTH_SHORT).show();
                        }


                        nString = " " + " " + separator + " " + inputSymbol;
                        clockEditText.setText(nString);
                    }
                    else if (cursorPosition == 1){

                        if (!isHoursProblem){   // Case 1 - first digit [3-9]

                            String inputSymbolStr = String.valueOf(inputSymbol);
                            if (inputSymbolStr.matches("[6789]")){  // Case 1.2

                                // Out 1.2
                                Toast.makeText(AlarmActivity.this, "out 1.2", Toast.LENGTH_SHORT).show();
                                nString = " " +
                                        s.toString().charAt(5) +
                                        separator + "0" + inputSymbol;
                                clockEditText.setText(nString);

                                closeKeyboard();
                                clockEditText.clearFocus();
                                clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                            }
                            else {  // When it matches to [0-5] Case 1.1

                                nString = " " + s.toString().charAt(5) + separator +
                                        inputSymbol + " ";
                                clockEditText.setText(nString);

                                isCase111Out = true;

                            }
                        }
                        else {  // Case 2 - First digit is [1-2]

                            String inputSymbolStr = String.valueOf(inputSymbol);

                            if (inputSymbolStr.equals("4") || inputSymbolStr.equals("5")){
                                isSecondIsFourOrFive = true;
                                Toast.makeText(AlarmActivity.this, "2nd : " + inputSymbolStr, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                isSecondIsFourOrFive = false;
                                Toast.makeText(AlarmActivity.this, "2nd : not 4 or 5", Toast.LENGTH_SHORT).show();
                            }

                            if (inputSymbolStr.matches("[012345]")){    // Case 2.1

                                isCase21Out = true;

                                nString = " " + String.valueOf(s.toString().charAt(5)) +
                                        separator + String.valueOf(inputSymbol) + " ";
                                clockEditText.setText(nString);


                            }
                            else {  // Case 2.2 -> |17:__| or | 2:07|

                                if (isFirstIsOne){
                                    isCase22Out = true;

                                    //Toast.makeText(AlarmActivity.this, s.toString().charAt(5), Toast.LENGTH_SHORT).show();
                                    nString = s.toString().charAt(5) + inputSymbolStr + separator +
                                            " " + " ";
                                    clockEditText.setText(nString);
                                }
                                else {  // Case 3.3 out | 2:07|

                                    nString = " " + String.valueOf(s.toString().charAt(5)) + separator + "0" + inputSymbolStr;
                                    clockEditText.setText(nString);

                                    closeKeyboard();
                                    clockEditText.clearFocus();
                                    clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                                }


                            }

                        }

//                        nString = s.toString().charAt(cursorPosition) + String.valueOf(inputSymbol) + separator + "00";
//                        Toast.makeText(AlarmActivity.this, "charAt0 - " + s.toString().charAt(0), Toast.LENGTH_SHORT).show();
//                        clockEditText.setText(nString);
                    }
                    else if (cursorPosition == 2){

                        // Out 3.2

                        if (isCase21Out && isSecondIsFourOrFive){
                            Toast.makeText(AlarmActivity.this, "case 3.2", Toast.LENGTH_SHORT).show();

                            nString = " " + s.toString().charAt(2) + separator +
                                    s.toString().charAt(4) + inputSymbol;
                            clockEditText.setText(nString);

                            closeKeyboard();
                            clockEditText.clearFocus();
                            clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                        }
                        else if (isCase111Out){  // Out 1.1.1
                            nString = " " + s.toString().charAt(2) + separator +
                                    s.toString().charAt(4) + inputSymbol;
                            clockEditText.setText(nString);

                            closeKeyboard();
                            clockEditText.clearFocus();
                            clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                        }
                        else if (isCase22Out){

                            String inputSymbolStr = String.valueOf(inputSymbol);

                            if (inputSymbolStr.matches("[012345]")){    // Case 2.2.1

                                if (isSecondIsFourOrFive){  // Case 3.2.1 out

                                // Out 3.2.1 | 2:53|
                                nString = " " + String.valueOf(s.toString().charAt(2)) + separator +
                                        String.valueOf(s.toString().charAt(4)) + inputSymbolStr;
                                clockEditText.setText(nString);

                                closeKeyboard();
                                clockEditText.clearFocus();
                                clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));

                                }
                                else {
                                    isCase221Out = true;
                                    Toast.makeText(AlarmActivity.this, "2.2 out", Toast.LENGTH_SHORT).show();
                                    nString = String.valueOf(s.toString().charAt(1)) +
                                            String.valueOf(s.toString().charAt(2)) +
                                            separator + inputSymbolStr + " ";
                                    clockEditText.setText(nString);
                                }

                            }
                            else {  // Case 2.2.2

                                // Out 2.2.2
                                nString = String.valueOf(s.toString().charAt(1)) +
                                        String.valueOf(s.toString().charAt(2)) +
                                        separator + "0" + inputSymbolStr;
                                clockEditText.setText(nString);

                                closeKeyboard();
                                clockEditText.clearFocus();
                                clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                            }

                        }
                        else if (isCase21Out){

                            String inputSymbolStr = String.valueOf(inputSymbol);

                            if (inputSymbolStr.matches("[6789]")){  // Case 2.1.1

                                // Out : 2.1.1

                                nString = " " + String.valueOf(s.toString().charAt(2)) + separator +
                                        String.valueOf(s.toString().charAt(4)) + inputSymbolStr;
                                clockEditText.setText(nString);

                                closeKeyboard();
                                clockEditText.clearFocus();
                                clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                            }
                            else {  // Case 2.1.2

                                isCase212Out = true;
                                enterClickAllowed = true;

                                nString = " " + String.valueOf(s.toString().charAt(2)) + separator +
                                        String.valueOf(s.toString().charAt(4)) + String.valueOf(inputSymbol);
                                clockEditText.setText(nString);

                                // Wait for enter clicked, otherwise -> option 2.1.2.2
                                // setOnkeyListener
                                // TODO : if user clicks enter whenever he wants

                            }

                        }

                    }
                    else if (cursorPosition == 3){

                        if (isCase221Out){

                            nString = String.valueOf(s.toString().charAt(1)) +
                                    String.valueOf(s.toString().charAt(2)) +
                                    separator +
                                    String.valueOf(s.toString().charAt(4)) +
                                    String.valueOf(inputSymbol);
                            clockEditText.setText(nString);

                            closeKeyboard();
                            clockEditText.clearFocus();
                            clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                        }
                        else if (isCase212Out){

                            if (clockEditText.hasFocus()){

                                nString = String.valueOf(s.toString().charAt(2)) +
                                        String.valueOf(s.toString().charAt(4)) + separator +
                                        String.valueOf(s.toString().charAt(5)) + inputSymbol;
                                clockEditText.setText(nString);

                                closeKeyboard();
                                clockEditText.clearFocus();
                                clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                            }

                        }

                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isTimeChangedByHand){
                    cursorPosition++;
                    //setTimeSwitch.setChecked(false);
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

        Integer hour, minute;

        String hourS = clockEditText.getText().toString().split(":")[0];

        if (hourS.charAt(0) == ' '){
            hourS = String.valueOf(hourS.charAt(1));
        }

        if (hourS.charAt(0) == '0'){
            hour = Integer.parseInt(String.valueOf(hourS.charAt(1)));
        }
        else {
            hour = Integer.parseInt(hourS);
        }

        String minuteS = clockEditText.getText().toString().split(":")[1];
        if (minuteS.charAt(0) == '0'){
            minute = Integer.parseInt(String.valueOf(minuteS.charAt(1)));
        }
        else {
            minute = Integer.parseInt(minuteS);
        }

        boolean isActive = setTimeSwitch.isChecked();
        int currentVolumePower = volumeSeekBar.getProgress();

        dataAccessManager.saveMaxVolumePower(maxVolumePower);

        // Forming alarmData object to return to storage

        alarmData.setHour(hour);
        alarmData.setMinute(minute);

        if (isActive){
            alarmData.setAsActive();
        }
        else {
            alarmData.setAsNotActive();
        }

        alarmData.setVolumePower(currentVolumePower);

        // Return new alarmData object to storage

        dataAccessManager.saveAlarmDataObject(alarmData);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (alarmData.isNoneOfDaysSet()){
            setTimeSwitch.setChecked(false);
        }

    }

    private void initViews(){

        // Set time bar views
        clockEditText = findViewById(R.id.clockEditText);
        setTimeTextView = findViewById(R.id.setTimeTextView);
        setTimeSwitch = findViewById(R.id.setTimeSwitch);

        // Volume bar views
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        increaseVolumeTextView = findViewById(R.id.increaseVolumeTextView);

        //Initialising timers volume buttons
        neverVolumeTimersButton = findViewById(R.id.neverVolumeTimersButton);
        c30sVolumeTimersButton = findViewById(R.id.c30sVolumeTimersButton);
        c15sVolumeTimersButton = findViewById(R.id.c15sVolumeTimersButton);
        c45sVolumeTimersButton = findViewById(R.id.c45sVolumeTimersButton);
        c60sVolumeTimersButton = findViewById(R.id.c60sVolumeTimersButton);

        // Alarm mode views
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

    private void initDayButtonsArray(){
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

    //**************   working with keyboard   ****************

    private void openKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInputFromWindow(clockEditText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        clockEditText.requestFocus();
    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }

    //*********   Set time text view   ***********

    public void setTimeTextViewClicked(View view) {
        openKeyboard();
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
        }
        else {

            String timeText = clockEditText.getText().toString();
            String[] numbers = timeText.split(":");

            int hour, minute;

            try{

                if (numbers[0].charAt(0) == ' '){
                    hour = Integer.parseInt(String.valueOf(numbers[0].charAt(1)));
                }
                else {
                    hour = Integer.parseInt(numbers[0]);
                }

                minute = Integer.parseInt(numbers[1]);
            }
            catch (Exception e){
                Toast.makeText(this, "parseInt exception", Toast.LENGTH_SHORT).show();
                hour = 0;
                minute = 0;
            }

            Toast.makeText(this, "" + hour + ":" + minute + " ready to set", Toast.LENGTH_SHORT).show();

            alarmData.setHour(hour);
            alarmData.setMinute(minute);
            alarmData.setAsActive();

            Calendar c = Calendar.getInstance();

            c.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
            c.set(Calendar.MINUTE, alarmData.getMinute());
            c.set(Calendar.SECOND, 0);

            setAlarm(c);
        }

    }

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

        if (alarmData.isNoneOfDaysSet()){
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                    activateAlarmPendingIntent);
        }
        else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, activateAlarmPendingIntent);
        }

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
        alarmData.setVolumeSelectedMode(0);
        setUpVolumeIncreasePanel();
    }

    public void c15sVolumeButtonClicked(View view) {
        alarmData.setVolumeSelectedMode(1);
        setUpVolumeIncreasePanel();
    }

    public void c30sVolumeButtonClicked(View view) {
        alarmData.setVolumeSelectedMode(2);
        setUpVolumeIncreasePanel();
    }

    public void c45sVolumeButtonClicked(View view) {
        alarmData.setVolumeSelectedMode(3);
        setUpVolumeIncreasePanel();
    }

    public void c60sVolumeButtonClicked(View view) {
        alarmData.setVolumeSelectedMode(4);
        setUpVolumeIncreasePanel();
    }

    //*********   Alarm mode buttons clicked   **********

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void soundPlusVibrateButtonClicked(View view) {
        alarmData.setAlarmSelectedMode(0);
        setUpAlarmModePanel();

        if (alarmData.isActive()){
            // Turn off previous pendingIntent with with old mode
            switchOnOffClick(setTimeSwitch, false);

            // Turn on new pendingIntent with needed mode
            switchOnOffClick(setTimeSwitch, true);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void soundButtonClicked(View view) {
        alarmData.setAlarmSelectedMode(1);
        setUpAlarmModePanel();

        if (alarmData.isActive()){
            // Turn off previous pendingIntent with with old mode
            switchOnOffClick(setTimeSwitch, false);

            // Turn on new pendingIntent with needed mode
            switchOnOffClick(setTimeSwitch, true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void vibrateButtonClicked(View view) {
        alarmData.setAlarmSelectedMode(2);
        setUpAlarmModePanel();

        if (alarmData.isActive()){
            // Turn off previous pendingIntent with with old mode
            switchOnOffClick(setTimeSwitch, false);

            // Turn on new pendingIntent with needed mode
            switchOnOffClick(setTimeSwitch, true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void noSoundButtonClicked(View view) {
        alarmData.setAlarmSelectedMode(3);
        setUpAlarmModePanel();

        if (alarmData.isActive()){
            // Turn off previous pendingIntent with with old mode
            switchOnOffClick(setTimeSwitch, false);

            // Turn on new pendingIntent with needed mode
            switchOnOffClick(setTimeSwitch, true);
        }
    }

    //**********   Setting up alarm days views   *************

    private void setUpAlarmDaysPanel(){

        isDayActiveArray = alarmData.getDaysActive();

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

        if (alarmData.getDay(0)){
            alarmData.setDay(false, 0);
        }
        else {
            alarmData.setDay(true, 0);
        }

        setUpAlarmDaysPanel();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void tuesdayButtonClicked(View view) {

        if (alarmData.getDay(1)){
            alarmData.setDay(false, 1);
        }
        else {
            alarmData.setDay(true, 1);
        }

        setUpAlarmDaysPanel();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void wednesdayButtonClicked(View view) {

        if (alarmData.getDay(2)){
            alarmData.setDay(false, 2);
        }
        else {
            alarmData.setDay(true, 2);
        }

        setUpAlarmDaysPanel();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void thursdayButtonClicked(View view) {

        if (alarmData.getDay(3)){
            alarmData.setDay(false, 3);
        }
        else {
            alarmData.setDay(true, 3);
        }

        setUpAlarmDaysPanel();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void fridayButtonClicked(View view) {

        if (alarmData.getDay(4)){
            alarmData.setDay(false, 4);
        }
        else {
            alarmData.setDay(true, 4);
        }

        setUpAlarmDaysPanel();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saturdayButtonClicked(View view) {

        if (alarmData.getDay(5)){
            alarmData.setDay(false, 5);
        }
        else {
            alarmData.setDay(true, 5);
        }

        setUpAlarmDaysPanel();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sundayButtonClicked(View view) {

        if (alarmData.getDay(6)){
            alarmData.setDay(false, 6);
        }
        else {
            alarmData.setDay(true, 6);
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

}