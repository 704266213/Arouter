package com.custom.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.custom.arouter_annotation.ARouter;
import com.custom.arouter_annotation.Parameter;
import com.custom.arouter_api.ParameterManager;
import com.custom.arouter_api.RouterManager;

@ARouter(path = "/order/OrderMainActivity")
public class OrderMainActivity extends AppCompatActivity {


    @Parameter(name = "name")
    String name;
    @Parameter(name = "age")
    int age;
    @Parameter(name = "sex")
    boolean sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_main);

        ParameterManager.getInstance().loadParameter(this);

        Log.e("XLog", "--------------->  name : " + name);
        Log.e("XLog", "--------------->  age : " + age);
        Log.e("XLog", "--------------->  sex : " + sex);

    }

    public void goToMainActivity(View view) {
        RouterManager.getInstance()
                .build("/app/MainActivity")
                .navigation(this);
    }
}
