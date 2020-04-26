package com.custom.arouter_api;

import android.content.Context;
import android.os.Bundle;

public class BundleManager {

    private Bundle bundle = new Bundle();

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public BundleManager withString(String key, String value) {
        bundle.putString(key, value);
        return this;
    }

    public BundleManager withInt(String key, int value) {
        bundle.putInt(key, value);
        return this;
    }

    public BundleManager withBoolean(String key, Boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    public void navigation(Context context) {
        RouterManager.getInstance().navigation(context, this);
    }

}
