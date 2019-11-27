package com.kkaka.arouter_api;

import com.kkaka.arouter_annotation.model.RouterBean;

import java.util.Map;

/**
 * @author Laizexin on 2019/11/21
 * @description Group对应的详细Path加载数据接口
 */
public interface ARouterLoadPath {
    Map<String, RouterBean> loadPath();
}
