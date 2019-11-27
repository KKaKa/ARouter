package com.kkaka.moduler.order;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kkaka.arouter_annotation.ARouter;
import com.kkaka.arouter_annotation.Params;
import com.kkaka.arouter_api.ParamsManager;
import com.kkaka.arouter_api.RouterManager;

import java.util.Locale;

@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Params(key = "name")
    String name;
    @Params
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity__main);
        Button button = findViewById(R.id.btn_jump);
        TextView textView = findViewById(R.id.tv);

        ParamsManager.getInstance().loadParams(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterManager.getInstance()
                        .setNeedFinish(true)
                        .setPath("/app/MainActivity")
                        .navigation(Order_MainActivity.this);
            }
        });

        textView.setText(String.format(Locale.CHINA,"接收到的参数：[%s : %s] [%s : %d]","name",name,"age",age));
    }
}
