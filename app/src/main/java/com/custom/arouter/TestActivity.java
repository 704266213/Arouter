package com.custom.arouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.custom.arouter_annotation.ARouter;

@ARouter(path = "/app/TestActivity")
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
