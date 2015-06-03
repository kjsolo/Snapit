package cn.soloho.snapit.tools;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by solo on 14/11/5.
 */
public class DisplayUtils {

    public static int dp2px(Context context, int dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    public static int sp2px(Context context, int sp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics()) + 0.5f);
    }

}
