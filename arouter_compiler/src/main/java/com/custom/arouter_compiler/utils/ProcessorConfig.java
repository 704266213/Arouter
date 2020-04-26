package com.custom.arouter_compiler.utils;

public interface ProcessorConfig {

    /**
     * @ARouter注解 的 包名 + 类名
     */
    String AROUTER_PACKAGE = "com.custom.arouter_annotation.ARouter";

    /**
     * 接收参数的TAG标记
     * 目的是接收 每个module名称
     */
    String OPTIONS = "moduleName";

    /**
     * 接收 包名（APT 存放的包名）
     */
    String APT_PACKAGE = "packageNameForAPT";

    /**
     * Activity全类名
     */
    String ACTIVITY_PACKAGE = "android.app.Activity";

    /**
     * ARouter api 包名
     */
    String AROUTER_API_PACKAGE = "com.custom.arouter_api";

    /**
     * ARouter api 的 ARouterGroup 高层标准
     * 生成的Group类的前缀
     */
    String AROUTER_API_GROUP = AROUTER_API_PACKAGE + ".ARouterGroup";

    /**
     * ARouter api 的 ARouterPath 高层标准
     * 生成的Path类的前缀
     */
    String AROUTER_API_PATH = AROUTER_API_PACKAGE + ".ARouterPath";


    /**
     * 路由组，中的 Path 里面的 方法名
     */
    String PATH_METHOD_NAME = "getPathMap";

    /**
     * 路由组，中的 Group 里面的 方法名
     */
    String GROUP_METHOD_NAME = "getGroupMap";

    /**
     * 路由组，中的 Path 里面 的
     */
    String PATH_MAP = "pathMap";

    /**
     * 路由组，中的 Group 里面 的 变量名
     */
    String GROUP_MAP = "groupMap";

    /**
     * 路由组，PATH 最终要生成的 文件名
     */
    String PATH_FILE_NAME = "ARouter$$Path$$";

    /**
     * 路由组，GROUP 最终要生成的 文件名
     */
    String GROUP_FILE_NAME = "ARouter$$Group$$";


    /**
     * @ARouter注解 的 包名 + 类名
     */
    String ANNOTATION_PARAMETER  = "com.custom.arouter_annotation.Parameter";

    /**
     * ARouter api 的 ParameterGet 方法参数的名字
     */
    String PARAMETER_NAME = "targetParameter";

    /**
     * ARouter api 的 ParameterGet 高层标准
     */
    String AROUTER_AIP_PARAMETER = AROUTER_API_PACKAGE + ".IParameter";

    String PARAMETER_METHOD_NAME = "loadParameter";

    /**
     * String全类名
     */
    String STRING = "java.lang.String";

    String PARAMETER_FILE_NAME = "$$Parameter";
}


