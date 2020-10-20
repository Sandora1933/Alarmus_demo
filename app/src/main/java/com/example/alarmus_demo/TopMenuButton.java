package com.example.alarmus_demo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Button;

public class TopMenuButton extends androidx.appcompat.widget.AppCompatButton {

    public TopMenuButton(Context context) {
        super(context);
    }

    public TopMenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopMenuButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }
}
