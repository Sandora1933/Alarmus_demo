package com.example.alarmus_demo.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.alarmus_demo.R;

public class SnoozeActivity extends AppCompatActivity {

    CardView timeSnoozeCardView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snooze);

        init();
        timeSnoozeCardView.setBackgroundResource(R.drawable.card_time_snooze);

    }

    private void init(){
        timeSnoozeCardView = findViewById(R.id.timeSnoozeCardView);
    }

}
