package com.custom.user;

import androidx.appcompat.app.AppCompatActivity;

import com.custom.arouter_annotation.ARouter;
import com.custom.arouter_api.RouterManager;

import android.os.Bundle;
import android.view.View;

@ARouter(path = "/user/UserMainActivity")
public class UserMainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_main);
    }

    public void goToOrderActivity(View view) {
        RouterManager.getInstance()
                .build("/order/OrderMainActivity")
                .withString("name", "alan")
                .withInt("age", 18)
                .withBoolean("sex", true)
                .navigation(this);
    }
}
