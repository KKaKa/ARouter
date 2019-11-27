package com.kkaka.arouter_compiler;

/**
 * @author Laizexin on 2019/11/22
 * @description
 */
public class Constants {

    public static final String AROUTER_ANNOTATION_TYPE = "com.kkaka.arouter_annotation.ARouter";
    public static final String PARAMS_ANNOTATION_TYPE = "com.kkaka.arouter_annotation.Params";

    public static final String MODULE_NAME = "moduleName";
    public static final String APT_PACKAGE = "aptPackage";

    public static final String ACTIVITY = "android.app.Activity";
    public static final String STRING = "java.lang.String";

    public static final String INTERFACE_AROUTER_PATH = "com.kkaka.arouter_api.ARouterLoadPath";
    public static final String INTERFACE_AROUTER_GROUP = "com.kkaka.arouter_api.ARouterLoadGroup";
    public static final String INTERFACE_PARAMS = "com.kkaka.arouter_api.ParamsLoad";

    //方法名
    public static final String PATH_MTTHOD_NAME = "loadPath";
    public static final String GROUP_MTTHOD_NAME = "loadGroup";
    public static final String PARAMS_MTTHOD_NAME = "loadParams";
    //变量名
    public static final String PATH_PARAMETER_NAME = "pathMap";
    public static final String GROUP_PARAMETER_NAME = "groupMap";
    //参数名
    public static final String PARAMS_NAME = "object";

    // APT生成的路由组Group类文件名
    public static final String GROUP_FILE_NAME = "ARouter$$Group$$";
    // APT生成的路由组Group对应的详细Path类文件名
    public static final String PATH_FILE_NAME = "ARouter$$Path$$";
    //APT生成的参数对应的类文件名
    public static final String PARAMS_FILE_NAME = "$$Params";


}
