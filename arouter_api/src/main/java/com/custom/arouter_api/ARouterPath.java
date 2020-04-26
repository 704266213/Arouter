package com.custom.arouter_api;

import com.custom.arouter_annotation.bean.RouterBean;

import java.util.Map;

/**
 * @author Administrator
 */
public interface ARouterPath {

    /**
     * 通过Apt技术动态存储注解类的消息
     * key : group分组
     * value: apt生成的类的全类名，如：com.custom.ARouter$$Group$$User
     *
     * @return
     */

    Map<String, RouterBean> getPathMap();
}
