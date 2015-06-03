package solo.snapit.widget;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by solo on 15/6/2.
 */
public class SToast {

    public static void show(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT);
    }

}
