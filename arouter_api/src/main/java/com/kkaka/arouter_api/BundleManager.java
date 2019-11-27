package com.kkaka.arouter_api;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Laizexin on 2019/11/26
 * @description
 */
public class BundleManager {
    private Bundle bundle = new Bundle();

    public Bundle getBundle() {
        return this.bundle;
    }


    public BundleManager withInt(@NonNull String key, @Nullable int value) {
        bundle.putInt(key, value);
        return this;
    }

    public BundleManager withBoolean(@NonNull String key, @Nullable boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withFloat(@NonNull String key, @Nullable float value) {
        bundle.putFloat(key, value);
        return this;
    }

    public BundleManager withDouble(@NonNull String key, @Nullable double value) {
        bundle.putDouble(key, value);
        return this;
    }

    public BundleManager withLong(@NonNull String key, @Nullable long value) {
        bundle.putLong(key, value);
        return this;
    }

    public BundleManager withChar(@NonNull String key, @Nullable char value) {
        bundle.putChar(key, value);
        return this;
    }

    public BundleManager withString(@NonNull String key, @Nullable String value) {
        bundle.putString(key, value);
        return this;
    }

    public BundleManager withBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    // 可以自己增加 ...

    // 直接完成跳转
    public Object navigation(Context context) {
        return RouterManager.getInstance().navigation(context, this);
    }
}
