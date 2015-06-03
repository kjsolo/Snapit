package cn.soloho.snapit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by solo on 15/2/19.
 */
public class HeaderPlaceHolder extends View {
    public HeaderPlaceHolder(Context context) {
        super(context);
    }

    public HeaderPlaceHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderPlaceHolder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int newWidth = getMeasuredWidth();
        int newHeight = (int) (newWidth * 0.5625f);

        setMeasuredDimension(newWidth, newHeight);
    }
}
