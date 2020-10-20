package com.example.alarmus_demo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alarmus_demo.R;

public class MainActivity extends AppCompatActivity {

    EditText clockEditText;
    boolean isChangedByHand;
    int cursorPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_constr);

        clockEditText = findViewById(R.id.clockEditText);
        isChangedByHand = true;
        cursorPosition = 0;
        final String separator = " : ";

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
                //Toast.makeText(MainActivity.this, "cursorPos - " + cursorPosition, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isChangedByHand){
                    isChangedByHand = false;

                    Toast.makeText(MainActivity.this, "Text - " + s, Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "cPos - " + cursorPosition, Toast.LENGTH_SHORT).show();

                    char inputSymbol = s.charAt(start);
                    String nString;
                    if (cursorPosition == 0){
                        nString = inputSymbol + "8" + separator + "88";
                        clockEditText.setText(nString);
                    }
                    else if (cursorPosition == 1){
                        nString = s.toString().charAt(cursorPosition) + String.valueOf(inputSymbol) + separator + "88";
                        Toast.makeText(MainActivity.this, "charAt0 - " + s.toString().charAt(0), Toast.LENGTH_SHORT).show();
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
//                        clockEditText.setFocusableInTouchMode(false);
//                        clockEditText.setFocusable(false);
                        clockEditText.setTextColor(getResources().getColor(R.color.colorRowTextAndIcon));
                    }

                    //clockEditText.setText(nString);
                    //Toast.makeText(MainActivity.this, "onChange()", Toast.LENGTH_SHORT).show();


                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (isChangedByHand){
                    cursorPosition++;
                }

                isChangedByHand = true;
                //Toast.makeText(MainActivity.this, "onAfterChanged()", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void musicMenuButtonClicked(View view) {
        Intent musicListActivityIntent = new Intent(MainActivity.this, MusicListActivity.class);
        startActivity(musicListActivityIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void infoMenuButtonClicked(View view) {
        Intent infoActivityIntent = new Intent(MainActivity.this, InfoActivity.class);
        startActivity(infoActivityIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }
}