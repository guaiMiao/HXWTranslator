package com.guaimiao.hxtranslator;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class FloatingTranslatorService extends Service {
    public static FloatingTranslatorService getInstance(){return instance;}
    private static FloatingTranslatorService instance;
    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    public LinearLayout linearLayout;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        isStarted = true;
        windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.width = 600;
        layoutParams.height = 500;
        layoutParams.x = 300;
        layoutParams.y = 300;

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.floating_translator,null);
        EditText e = linearLayout.findViewById(R.id.schs);
        EditText e1 = linearLayout.findViewById(R.id.fchs);
        EditText e2 = linearLayout.findViewById(R.id.hxchs);
        final boolean[] hasChange = {false};
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(hasChange[0]) return;
                String schs = s.toString();
                String fchs = Translator.schs2fchs(schs);
                String hxchs = Translator.schs2hxchs(schs);
                hasChange[0] = true;
                e1.setText(fchs);
                e2.setText(hxchs);
                hasChange[0] = false;
                if(MainActivity.getInstance().copyMode == 0){
                    ClipboardManager manager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    manager.setPrimaryClip(ClipData.newPlainText("text",hxchs));
                }
                if(MainActivity.getInstance().copyMode == 1){
                    ClipboardManager manager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    manager.setPrimaryClip(ClipData.newPlainText("text",fchs));
                }
            }
        });
        e1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(hasChange[0]) return;
                String fchs = s.toString();
                String schs = Translator.fchs2schs(fchs);
                String hxchs = Translator.fchs2hxchs(fchs);
                hasChange[0] = true;
                e.setText(schs);
                e2.setText(hxchs);
                hasChange[0] = false;
                if(MainActivity.getInstance().copyMode == 0){
                    ClipboardManager manager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    manager.setPrimaryClip(ClipData.newPlainText("text",hxchs));
                }
            }
        });
        e2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(hasChange[0]) return;
                String hxchs = s.toString();
                String schs = Translator.hxchs2schs(hxchs);
                String fchs = Translator.hxchs2fchs(hxchs);
                hasChange[0] = true;
                e.setText(schs);
                e1.setText(fchs);
                hasChange[0] = false;
                if(MainActivity.getInstance().copyMode == 1){
                    ClipboardManager manager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    manager.setPrimaryClip(ClipData.newPlainText("text",fchs));
                }
            }
        });
        linearLayout.setOnTouchListener(new FloatingOnTouchListener());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();

        return super.onStartCommand(intent, flags, startId);
    }

    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(MainActivity.getInstance())) {
            windowManager.addView(linearLayout, layoutParams);
//            e.setOnTouchListener(new FloatingOnTouchListener());
//            e1.setOnTouchListener(new FloatingOnTouchListener());
//            e2.setOnTouchListener(new FloatingOnTouchListener());
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}