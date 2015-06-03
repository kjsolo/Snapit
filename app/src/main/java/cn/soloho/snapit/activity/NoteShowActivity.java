package cn.soloho.snapit.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import cn.soloho.snapit.widget.SemiLayout;
import cn.soloho.snapit.R;

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
