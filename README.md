# ARouter
组件化中使用APT实现模块间Activity跳转和参数传递


### 使用注解@ARouter建立路由表
```
@ARouter(path = "/app/MainActivity")
@ARouter(path = "/order/Order_MainActivity")
```

### 使用注解@Params接收参数
```
@Params(key = "name")
String name;
@Params
int age;
```

### 不同模块间Activity跳转
```
RouterManager.getInstance()
        .setDebug(true)
        .setNeedFinish(true)
        .setPath("/order/Order_MainActivity")
        .navigation(MainActivity.this);
```

### 不同模块间Activity携带参数跳转
```
RouterManager.getInstance()
        .setDebug(true)
        .setNeedFinish(true)
        .setPath("/order/Order_MainActivity")
        .withString("name","kkaka")
        .withInt("age",22)
        .navigation(MainActivity.this);
```

### 目标Activity接收参数
```
@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Params(key = "name")
    String name;
    @Params
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//...
        ParamsManager.getInstance().loadParams(this);
        textView.setText(String.format(Locale.CHINA,"接收到的参数：[%s : %s] [%s : %d]","name",name,"age",age));
    }
}
```

