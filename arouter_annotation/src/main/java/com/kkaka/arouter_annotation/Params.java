package com.kkaka.arouter_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Laizexin on 2019/11/25
 * @description key不填则以变量名为key
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Params {
    String key() default "";
}
