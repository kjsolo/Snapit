package solo.snapit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by solo on 14/12/22.
 */
public class NoteDivider extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    private int mDividerHeight;
    private Drawable mDivider;

    public NoteDivider(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    public NoteDivider(Drawable divider) {
        this(divider, 0);
    }

    public NoteDivider(Drawable divider, int height) {
        mDivider = divider;
        mDividerHeight = height;
        if (mDivider == null) {
            mDivider = new ColorDrawable(Color.TRANSPARENT);
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawVertical(c, parent);
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = Math.max(parent.getChildCount() - 1, 0);
        for (int i = 1; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom;
            if (mDividerHeight > 0) {
                bottom = mDividerHeight;
            } else {
                bottom = top + mDivider.getIntrinsicHeight();
            }
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    public int getDividerHeight() {
        return mDividerHeight;
    }

    public Drawable getDivider() {
        return mDivider;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, mDividerHeight > 0 ? mDividerHeight : mDivider.getIntrinsicHeight());
    }
}
