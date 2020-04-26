package com.custom.arouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.custom.arouter_annotation.ARouter;
import com.custom.arouter_api.RouterManager;


@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToMineActivity(View view) {
        RouterManager.getInstance()
                .build("/user/UserMainActivity")
                .navigation(this);
    }
}
