package com.tuyue.common_sdk.tools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.WindowManager;



public class DensityUtil {

    public static int dp2Px(float dpValue) {
        Context context = BaseControlCenter.getContext();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2Px(float dpValue) {
        Context context = BaseControlCenter.getContext();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int dp2px(Context context, float dpValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }
    public static int sp2px(Context context, float dpValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpValue, context.getResources().getDisplayMetrics());
    }



    public static float px2Dp(int px) {
        Context context = BaseControlCenter.getContext();
        final float scale = context.getResources().getDisplayMetrics().density;
        return (px / scale + 0.5f);
    }

    public static float px2Sp(int px) {
        Context context = BaseControlCenter.getContext();
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (px / scale + 0.5f);
    }

    public static int getWidth() {
        Context context = BaseControlCenter.getContext();
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getHeight() {
        Context context = BaseControlCenter.getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Point point = new Point();
            wm.getDefaultDisplay().getRealSize(point);
            return point.y;
        }
        return 0;
    }

    /**
     * 获取状态栏高度
     */
    public static int getStateBarHeight() {
        return getStateBarHeight(BaseControlCenter.getContext());
    }

    public static int getStateBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelOffset(resourceId);
    }
}
