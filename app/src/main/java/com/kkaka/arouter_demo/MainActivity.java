package com.kkaka.arouter_demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kkaka.arouter_annotation.ARouter;
import com.kkaka.arouter_api.RouterManager;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.btn_jump);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterManager.getInstance()
                        .setDebug(true)
                        .setNeedFinish(true)
                        .setPath("/order/Order_MainActivity")
                        .withString("name","kkaka")
                        .withInt("age",22)
                        .navigation(MainActivity.this);
            }
        });
    }
}
