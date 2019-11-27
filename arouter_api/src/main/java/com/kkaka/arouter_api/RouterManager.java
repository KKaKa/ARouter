package com.kkaka.arouter_api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.kkaka.arouter_annotation.model.RouterBean;

/**
 * @author Laizexin on 2019/11/25
 * @description
 */
public class RouterManager {
    private boolean isDebug = false;
    private static RouterManager instance;
    private String path;
    private String group;
    private LruCache<String,ARouterLoadGroup> groupLruCache;
    private LruCache<String,ARouterLoadPath> pathLruCache;
    private static final String FILE_GROUP_NAME = "ARouter$$Group$$";
    private boolean needFinish;

    public RouterManager() {
        this.groupLruCache = new LruCache<>(100);
        this.pathLruCache = new LruCache<>(100);
    }

    public static RouterManager getInstance(){
        if(instance == null){
            synchronized (RouterManager.class){
                if(instance == null){
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    public BundleManager setPath(String path){
        if(instance == null){
            throw new NullPointerException("getInstance() first");
        }

        if(TextUtils.isEmpty(path)){
            throw new IllegalArgumentException("RouterManager -> path is null");
        }

        if(!path.startsWith("/")){
            throw new IllegalArgumentException("path must start with '/'");
        }

        String finalGroupName = path.substring(1,path.lastIndexOf("/"));
        if (isDebug){
            Log.i("ARouter","ARouter.RouterManager.finalGroupName -> "+ finalGroupName);
        }

        if(TextUtils.isEmpty(finalGroupName)){
            throw new IllegalArgumentException("RouterManager -> finalGroupName is null");
        }
        this.path = path;
        this.group = finalGroupName;
        return new BundleManager();
    }

    public RouterManager setDebug(boolean isDebug){
        this.isDebug = isDebug;
        return this;
    }

    public RouterManager setNeedFinish(boolean needFinish) {
        this.needFinish = needFinish;
        return this;
    }

    public boolean navigation(Context context,BundleManager manager){
        //查找例如 ARouter$$Group$$order
        String groupClassName = context.getPackageName() + ".apt." +FILE_GROUP_NAME + group;
        if(isDebug){
            Log.i("ARouter","ARouter.RouterManager.navigation.groupClassName -> "+ groupClassName);
        }
        try {
            ARouterLoadGroup aRouterLoadGroup = groupLruCache.get(group);
            if(aRouterLoadGroup == null){
                Class<?> aClass = Class.forName(groupClassName);
                aRouterLoadGroup = (ARouterLoadGroup) aClass.newInstance();
                groupLruCache.put(group,aRouterLoadGroup);
            }

            if(aRouterLoadGroup.loadGroup().isEmpty()){
                throw new NullPointerException("ARouter table error,ARouterLoadGroup is empty");
            }

            ARouterLoadPath aRouterLoadPath = pathLruCache.get(path);
            if(aRouterLoadPath == null){
                Class<? extends ARouterLoadPath> aClass = aRouterLoadGroup.loadGroup().get(group);
                if(aClass == null){
                    throw new NullPointerException("ARouter table error,ARouterLoadGroup can not find Class with group" + group);
                }
                aRouterLoadPath = aClass.newInstance();
                pathLruCache.put(path,aRouterLoadPath);
            }

            if(aRouterLoadPath.loadPath().isEmpty()){
                throw new NullPointerException("ARouter table error,ARouterLoadPath is empty");
            }

            RouterBean routerBean = aRouterLoadPath.loadPath().get(path);

            if(routerBean != null){
                if(routerBean.getType() == RouterBean.Type.ACTIVITY){
                    Intent intent = new Intent(context,routerBean.getClazz());
                    intent.putExtras(manager.getBundle());
                    context.startActivity(intent);
                    if(needFinish && context instanceof Activity){
                        ((Activity) context).finish();
                    }
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
