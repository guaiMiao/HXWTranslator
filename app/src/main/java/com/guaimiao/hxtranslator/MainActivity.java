package com.guaimiao.hxtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String[] copyModes = new String[]{"自动复制：焱暒妏","自动复制：繁體中文","自动复制：关"};
    private static MainActivity instance;
    public static MainActivity getInstance(){return instance;}
    private Intent floatingWindowIntent;
    public int copyMode = 0;
    public static int password = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        Button b = findViewById(R.id.start_floating);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FloatingTranslatorService.isStarted) {
                    return;
                }
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, "当前无权限，请授权", Toast.LENGTH_SHORT);
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
                } else {
                    floatingWindowIntent = new Intent(MainActivity.this, FloatingTranslatorService.class);
                    startService(floatingWindowIntent);
                }
            }
        });

        Button b1 = findViewById(R.id.auto_copy);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyMode += 1;
                if(copyMode == 3){
                    copyMode = 0;
                }
                b1.setText(copyModes[copyMode]);
            }
        });
        Button b2 = findViewById(R.id.close_floating);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
                try{
                    FloatingTranslatorService.isStarted = false;
                    windowManager.removeView(FloatingTranslatorService.getInstance().linearLayout);
                    stopService(floatingWindowIntent);
                }catch (Exception e){
                    Toast.makeText(MainActivity.this,"请先开启悬浮窗",Toast.LENGTH_SHORT);
                }
            }
        });

        EditText e = findViewById(R.id.encoding);
        e.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                password = Integer.parseInt(v.getText().toString());
                return false;
            }
        });

        Button b3 = findViewById(R.id.random_encode);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e.setText(""+new Random().nextInt(10000000)+1);
                password = Integer.parseInt(e.getText().toString());
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, FloatingTranslatorService.class));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}