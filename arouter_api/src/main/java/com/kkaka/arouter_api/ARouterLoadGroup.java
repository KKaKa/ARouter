package com.kkaka.arouter_api;

import java.util.Map;

/**
 * @author Laizexin on 2019/11/21
 * @description group加载数据接口
 */
public interface ARouterLoadGroup {
    Map<String,Class<? extends ARouterLoadPath>> loadGroup();
}
