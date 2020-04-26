package com.custom.arouter_api;

import java.util.Map;

public interface ARouterGroup {

    /**
     * 通过Apt技术动态存储注解类的消息
     * key : group分组
     * value：需要添加到对应分组的注册的类信息
     *
     * @return
     */
    Map<String, Class<? extends ARouterPath>> getGroupMap();
}
