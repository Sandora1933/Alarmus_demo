package com.example.alarmus_demo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alarmus_demo.R;

public class StartAlarmActivity extends AppCompatActivity {

    RelativeLayout percentRelativeLayout;
    FrameLayout frameLayout;
    TextView textView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_start);

        percentRelativeLayout = findViewById(R.id.percentRelativeLayout);
        frameLayout = findViewById(R.id.frameLayout);
        textView = findViewById(R.id.percentTextView);

        percentRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    double num = (event.getY() / v.getHeight()) * 100;
                    num = 100 - num;
                    textView.setText((int) num + "%");

                    ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
                    params.height = v.getHeight() - (int) event.getY();
                    frameLayout.setLayoutParams(params);

                }

                return true;
            }

        });

        //TODO: Exchange onTouchListener with OnDragListener
//        frameLayout.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View v, DragEvent event) {
//                switch (event.getAction()){
//                    case DragEvent.ACTION_DRAG_STARTED:
//                        v.setBackgroundColor(Color.RED);
//                        Toast.makeText(StartAlarmActivity.this, "action!", Toast.LENGTH_SHORT).show();
//                        break;
//                    default:
//                        Toast.makeText(StartAlarmActivity.this, "action-default!", Toast.LENGTH_SHORT).show();
//                        break;
//                }
//
//                return true;
//            }
//        });

    }

    public void moreButtonClicked(View view) {
        Intent snoozeActivityIntent = new Intent(StartAlarmActivity.this, SnoozeActivity.class);
        startActivity(snoozeActivityIntent);
    }

    public void stopButtonClicked(View view) {
        Toast.makeText(this, "stop clicked", Toast.LENGTH_SHORT).show();
    }
}
