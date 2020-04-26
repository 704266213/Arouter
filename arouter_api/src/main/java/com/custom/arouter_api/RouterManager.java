package com.custom.arouter_api;

import android.content.Context;
import android.content.Intent;
import android.util.LruCache;

import com.custom.arouter_annotation.bean.RouterBean;

/**
 * @author Administrator
 */
public class RouterManager {

    private static volatile RouterManager routerManager;
    /**
     * 性能  LRU缓存
     */
    private LruCache<String, ARouterGroup> groupLruCache;
    private LruCache<String, ARouterPath> pathLruCache;

    private RouterManager() {
        groupLruCache = new LruCache<>(100);
        pathLruCache = new LruCache<>(100);
    }

    public static RouterManager getInstance() {
        if (routerManager == null) {
            synchronized (RouterManager.class) {
                if (routerManager == null) {
                    routerManager = new RouterManager();
                }
            }
        }
        return routerManager;
    }


    private String group;
    private String path;

    public BundleManager build(String path) {
        this.path = path;
        this.group = Utils.parsePath(path);
        return new BundleManager();
    }


    private final String GROUP_FILE_NAME = "ARouter$$Group$$";

    public void navigation(Context context, BundleManager bundleManager) {
        String groupClassName = context.getApplicationContext().getPackageName() + "." + GROUP_FILE_NAME + Utils.captureName(group);

        ARouterGroup routerGroup = groupLruCache.get(group);
        if (routerGroup == null) {
            Class groupClass = null;
            try {
                groupClass = Class.forName(groupClassName);
                routerGroup = (ARouterGroup) groupClass.newInstance();
                groupLruCache.put(group, routerGroup);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ARouterPath routerPath = pathLruCache.get(path);
        if (routerPath == null) {
            Class<? extends ARouterPath> pathClass = routerGroup.getGroupMap().get(group.toLowerCase());
            try {
                routerPath = pathClass.newInstance();
                pathLruCache.put(path, routerPath);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        if (routerPath != null) {
            if (routerPath.getPathMap().isEmpty()) {
                throw new RuntimeException("路由表Path对象为空");
            }

            // 我们已经进入 PATH 函数 ，开始拿 Class 进行跳转
            RouterBean routerBean = routerPath.getPathMap().get(path);
            if (routerBean != null) {
                switch (routerBean.getTypeEnum()) {
                    case ACTIVITY:
                        Intent intent = new Intent(context, routerBean.getTargetClass());
                        // 携带参数
                        intent.putExtras(bundleManager.getBundle());
                        context.startActivity(intent);
                        break;
                }
            }
        }
    }

}
