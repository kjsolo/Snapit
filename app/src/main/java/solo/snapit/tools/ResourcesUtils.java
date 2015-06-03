package solo.snapit.tools;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by solo on 15/2/28.
 */
public class ResourcesUtils {

    public static int getResIdOfAttr(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.resourceId;
    }

}
