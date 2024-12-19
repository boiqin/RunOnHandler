package com.boiqin.runonhandler;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.boiqin.annotations.RunOnHandler;

public class DemoActivity extends AppCompatActivity {
    private static final String TAG = "DemoActivity";
    private final Handler mainHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        test();
    }

    @RunOnHandler(handlerName = "mainHandler")
    public void test() {
        Log.e(TAG, "current thread: " + Thread.currentThread().getName());
    }
}
