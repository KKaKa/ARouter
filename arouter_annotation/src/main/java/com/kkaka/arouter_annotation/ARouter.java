package com.kkaka.arouter_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Laizexin on 2019/11/21
 * @description 路由注解
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ARouter {

    //路径 eg: "/app/MainActivity"
    String path();

    //组名 eg: "app" 不填则在path中截取
    String group() default "";
}
