package com.custom.arouter_api;

import android.text.TextUtils;

/**
 * @author Administrator
 */
public class Utils {


    /**
     * 将字符串的首字母转大写
     *
     * @param str 需要转换的字符串
     * @return
     */
    public static String captureName(String str) {
        // 进行字母的ascii编码前移，效率要高于截取字符串进行转换的操作
        char[] cs = str.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    public static String parsePath(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("path的格式不正确，正确写法：如 /order/MainActivity");
        }

        // 只写了一个 /
        if (path.lastIndexOf("/") == 0) {
            throw new IllegalArgumentException("path的格式不正确，正确写法：如 /order/MainActivity");
        }

        // 截取组名  /order/MainActivity  order
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (TextUtils.isEmpty(finalGroup)) {
            throw new IllegalArgumentException("path的格式不正确，正确写法：如 /order/MainActivity");
        }
        return finalGroup;
    }
}
