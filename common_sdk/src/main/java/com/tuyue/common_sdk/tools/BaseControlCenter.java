package com.tuyue.common_sdk.tools;

import android.annotation.SuppressLint;
import android.content.Context;


public class BaseControlCenter {
    /**
     * 禁止检查，因为这是一个Application的Context
     */
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    private static boolean isDebug;


    /**
     * 初始化
     *
     * @param application 上下文
     * @param isDebug     是否是debug模式
     */
    public static void init(Context application, boolean isDebug) {
        BaseControlCenter.mContext = application.getApplicationContext();
        BaseControlCenter.isDebug = isDebug;

    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static Context getContext() {
        return mContext;
    }
}
