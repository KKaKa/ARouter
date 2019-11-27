package com.kkaka.arouter_api;

import android.app.Activity;
import android.util.LruCache;

/**
 * @author Laizexin on 2019/11/26
 * @description
 */
public class ParamsManager {

    private static ParamsManager instance;
    private LruCache<String,ParamsLoad> cache;
    private static final String FILE_SUFFIX_NAME = "$$Params";

    public static ParamsManager getInstance(){
        if(instance == null){
            synchronized (ParamsManager.class){
                if(instance == null){
                    instance = new ParamsManager();
                }
            }
        }
        return instance;
    }

    private ParamsManager() {
        cache = new LruCache<>(100);
    }

    public void loadParams(Activity activity){
        String className = activity.getClass().getName();
        ParamsLoad paramsLoad = cache.get(className);
        if(null == paramsLoad){
            try {
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                paramsLoad = (ParamsLoad) aClass.newInstance();
                cache.put(className,paramsLoad);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        paramsLoad.loadParams(activity);
    }


}
