package solo.snapit.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import solo.snapit.R;
import solo.snapit.widget.SemiLayout;

/**
 * Created by solo on 15/3/8.
 */
public class NoteShowActivity extends Activity {

    private View mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_show);
        mScrollView = findViewById(R.id.scroll_view);
        ((SemiLayout) findViewById(R.id.root)).setScrollView(mScrollView);
    }

}
