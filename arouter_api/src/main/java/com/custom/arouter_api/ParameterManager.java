package com.custom.arouter_api;

import android.util.LruCache;

/**
 * @author Administrator
 */
public class ParameterManager {


    private static class ParameterManagerInstance {
        private static final ParameterManager parameterManager = new ParameterManager();
    }

    private ParameterManager() {
        cache = new LruCache<>(100);
    }

    public static ParameterManager getInstance() {
        return ParameterManagerInstance.parameterManager;
    }

    static final String FILE_SUFFIX_NAME = "$$Parameter";
    private LruCache<String, IParameter> cache;

    public void loadParameter(Object object) {
        String parameterClassName = object.getClass().getName() + FILE_SUFFIX_NAME;

        IParameter iParameter = cache.get(parameterClassName);

        if (iParameter == null) {
            Class parameterClass;
            try {
                parameterClass = Class.forName(parameterClassName);
                iParameter = (IParameter) parameterClass.newInstance();
                cache.put(parameterClassName, iParameter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        iParameter.loadParameter(object);
    }

}
