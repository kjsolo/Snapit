package cn.soloho.snapit.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Editable;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toolbar;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.software.shell.fab.ActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.soloho.snapit.model.Note;
import cn.soloho.snapit.tools.DisplayUtils;
import cn.soloho.snapit.tools.ResourcesUtils;
import cn.soloho.snapit.R;

/**
 * Created by solo on 15/2/27.
 */
public class EditNoteActivity extends Activity {

    public static final int RESULT_ABANDON = 2;

    public static final String EXTRA_URI = "mUri";

    @InjectView(R.id.image) ImageView mImageView;
    @InjectView(R.id.mask) View mMaskView;
    @InjectView(R.id.edit) EditText mEditView;
    @InjectView(R.id.note) TextView mNoteView;
    @InjectView(R.id.fab) ActionButton mFabView;
    @InjectView(R.id.note_fab) ActionButton mNoteFabView;
    @InjectView(R.id.action_note) TableRow mActionNoteView;
    @InjectView(R.id.action_save) TableRow mActionSaveView;
    @InjectView(R.id.save_text) TextView mSaveTextView;
    @InjectView(R.id.note_text) TextView mNoteTextView;
    @InjectView(R.id.fab_image) ImageView mFabImageView;
    @InjectView(R.id.fab_image2) ImageView mFabImage2View;
    @InjectView(R.id.tips) TextView mTipsView;
    @InjectView(R.id.toolbar) Toolbar mToolbar;
    @InjectView(R.id.text_bg) View mTextBgView;
    @InjectView(R.id.root) ViewGroup mRootView;
    @InjectView(R.id.edit_text_layout) ViewGroup mEditTextLayout;

    private ObjectAnimator mTipsAnim;
    private LocationClient mLocationClient;

    private Uri mUri;
    private boolean mInsert;
    private int mVisibleHeight;
    private boolean isAnim;
    private boolean mIsEdited;
    private boolean mIsKeyboardShow;
    private double mLatitude;
    private double mLongitude;
    private String mAddrStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);
        ButterKnife.inject(this);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        //option.setScanSpan(1000);当不设此项，或者所设的整数值小于1000（ms）时，采用一次定位模式。每调用一次requestLocation( )，定位SDK会发起一次定位。请求定位与监听结果一一对应。
        option.setOpenGps(true);
        option.setAddrType("all");
        mLocationClient.setLocOption(option);

        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getKeyboardHeight();
            }
        });

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NavUtils.navigateUpFromSameTask(EditNoteActivity.this);
                onBackPressed();
            }
        });

        mUri = getIntent().getParcelableExtra(EXTRA_URI);

        if (mUri == null) {
            throw new RuntimeException("Uri is empty");
        }

        reset();

        if ("file".equals(mUri.getScheme())) {
            mInsert = true;
            ImageLoader.getInstance().displayImage(mUri.toString(), mImageView);
        } else if ("content".equals(mUri.getScheme())) {
            mInsert = false;
        } else {
            throw new RuntimeException("Unknow uri: " + mUri.toString());
        }

        if (savedInstanceState != null) {
            mIsEdited = savedInstanceState.getBoolean("mIsEdited");
            mLatitude = savedInstanceState.getDouble("mLatitude");
            mLongitude = savedInstanceState.getDouble("mLongitude");
            mAddrStr = savedInstanceState.getString("mAddrStr");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mIsEdited", mIsEdited);
        outState.putDouble("mLatitude", mLatitude);
        outState.putDouble("mLongitude", mLongitude);
        outState.putString("mAddrStr", mAddrStr);
    }

    @Override
    public void onBackPressed() {
        if (mMaskView.getVisibility() == View.VISIBLE) {
            shrink();
        } else if (mIsEdited) {
            new WarningDialog().show(getFragmentManager(), null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
    }

    @OnTextChanged(value = R.id.edit, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAfterTextChanged(Editable s) {
        mNoteView.setText(s.toString());
        mIsEdited = true;
    }

    @OnClick({ R.id.fab, R.id.mask, R.id.action_note, R.id.note_fab })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab: {
                if (mMaskView.getVisibility() != View.VISIBLE) {
                    extend();
                } else {
                    if (!isAnim) {
                        try {
                            Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
                            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Photo_" + ts + ".jpg");
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();

                            Note note = new Note();
                            note.setImageUri(Uri.fromFile(file).toString());
                            note.setRemark(mNoteView.getText().toString());
                            note.setLat(mLatitude);
                            note.setLng(mLongitude);
                            note.setLocation(mAddrStr);
                            //note.setRemind();

                            getContentResolver().insert(Note.CONTENT_URI, note.toContentValues());
                            setResult(RESULT_OK);
                            finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            }
            case R.id.mask: {
                if (mIsKeyboardShow) {
                    // 隐藏键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditView.getWindowToken(), 0, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            shrink();
                        }
                    });
                } else {
                    shrink();
                }
                break;
            }
            case R.id.note_fab:
            case R.id.action_note: {
                openEdit();
                break;
            }
        }
    }

    private void reset() {
        mMaskView.setVisibility(View.INVISIBLE);
        mEditView.setVisibility(View.GONE);
        mActionNoteView.setVisibility(View.INVISIBLE);
        mSaveTextView.setVisibility(View.INVISIBLE);
        mFabImageView.setAlpha(1f);
        mFabImage2View.setAlpha(0f);
        mFabImageView.setRotation(0);
        mFabImage2View.setRotation(0);
        mTipsView.setVisibility(View.INVISIBLE);
        mTextBgView.setVisibility(View.INVISIBLE);
    }

    public void extend() {
        if (isAnim) {
            return;
        }
        isAnim = true;
        int duration = 200;
        int y = DisplayUtils.dp2px(this, 10);

        ObjectAnimator fabivAnim = ObjectAnimator.ofPropertyValuesHolder(mFabImageView,
                PropertyValuesHolder.ofFloat(View.ROTATION, 0, 45),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0));

        ObjectAnimator fabiv2Anim = ObjectAnimator.ofPropertyValuesHolder(mFabImage2View,
                PropertyValuesHolder.ofFloat(View.ROTATION, -45, 0),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1));

        ObjectAnimator stvAnim = ObjectAnimator.ofPropertyValuesHolder(mSaveTextView,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, y, 0),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1));

        ObjectAnimator ntvAnim = ObjectAnimator.ofPropertyValuesHolder(mNoteTextView,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, y, 0),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1));

        ObjectAnimator nfvAnim = ObjectAnimator.ofPropertyValuesHolder(mNoteFabView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0, 1));

        ObjectAnimator mAnim = ObjectAnimator.ofPropertyValuesHolder(mMaskView,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1));

        AnimatorSet nSet = new AnimatorSet();
        nSet.playTogether(ntvAnim, nfvAnim);
        nSet.setDuration(duration);
        nSet.setStartDelay(duration / 2);
        nSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mActionNoteView.setVisibility(View.VISIBLE);
                mNoteTextView.setVisibility(View.VISIBLE);
                mNoteFabView.setVisibility(View.VISIBLE);
                mNoteFabView.setPivotX(mNoteFabView.getWidth() / 2);
                mNoteFabView.setPivotY(mNoteFabView.getHeight());
                mNoteFabView.setScaleX(0);
                mNoteFabView.setScaleY(0);
                mNoteTextView.setAlpha(0f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnim = false;
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fabivAnim, fabiv2Anim, stvAnim, nSet, mAnim);
        set.setDuration(duration);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mSaveTextView.setVisibility(View.VISIBLE);
                mMaskView.setVisibility(View.VISIBLE);
            }
        });
        set.start();
    }

    private void shrink() {
        if (isAnim) {
            return;
        }
        isAnim = true;
        int duration = 200;
        int y = DisplayUtils.dp2px(this, 10);

        ObjectAnimator fabivAnim = ObjectAnimator.ofPropertyValuesHolder(mFabImageView,
                PropertyValuesHolder.ofFloat(View.ROTATION, 45, 0),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1));

        ObjectAnimator fabiv2Anim = ObjectAnimator.ofPropertyValuesHolder(mFabImage2View,
                PropertyValuesHolder.ofFloat(View.ROTATION, 0, -45),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0));

        ObjectAnimator stvAnim = ObjectAnimator.ofPropertyValuesHolder(mSaveTextView,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0, y),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0));

        ObjectAnimator ntvAnim = ObjectAnimator.ofPropertyValuesHolder(mNoteTextView,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0, y),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0));

        ObjectAnimator nfvAnim = ObjectAnimator.ofPropertyValuesHolder(mNoteFabView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0));

        ObjectAnimator mAnim = ObjectAnimator.ofPropertyValuesHolder(mMaskView,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0));

        AnimatorSet dSet = new AnimatorSet();
        if (mSaveTextView.getVisibility() == View.VISIBLE) {
            dSet.playTogether(fabivAnim, fabiv2Anim, stvAnim, mAnim);
        } else {
            dSet.playTogether(fabivAnim, fabiv2Anim, mAnim);
        }
        dSet.setDuration(duration);
        dSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSaveTextView.setVisibility(View.INVISIBLE);
                mMaskView.setVisibility(View.GONE);
                isAnim = false;
            }
        });

        if (mActionNoteView.getVisibility() != View.VISIBLE) {

            if (mTextBgView.getVisibility() == View.VISIBLE) {
                dSet.setStartDelay(400);
                int actionSaveTop = ((ViewGroup) mActionSaveView.getParent()).getTop() + mActionSaveView.getTop()
                        + mFabView.getTop() + mFabView.getHeight() / 2;
                int textBgTop = ((ViewGroup) mTextBgView.getParent()).getTop() + mTextBgView.getTop();
                int diffTop;
                if (actionSaveTop > textBgTop) {
                    diffTop = actionSaveTop - textBgTop;
                } else {
                    diffTop = 0;
                }

                int actionSaveLeft = ((ViewGroup) mActionSaveView.getParent()).getLeft() + mActionSaveView.getLeft()
                        + ((ViewGroup) mFabView.getParent().getParent()).getLeft() + mFabView.getLeft() + mFabView.getWidth() / 2;

                int cx = actionSaveLeft;
                int cy = diffTop;
                int startRadius = Math.max(mTextBgView.getWidth(), mTextBgView.getHeight());
                Animator tbAnim = ViewAnimationUtils.createCircularReveal(mTextBgView, cx, cy, startRadius, 0);
                tbAnim.setDuration(600);

//                ObjectAnimator nvAnim = ObjectAnimator.ofPropertyValuesHolder(mNoteView,
//                        PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1)).setDuration(duration);
//
//                ObjectAnimator evAnim = ObjectAnimator.ofPropertyValuesHolder(mEditView,
//                        PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0)).setDuration(duration);

                final boolean hasText = mEditView.getText().length() > 0;
                int colorFrom = Color.parseColor("#ccffffff");
                int colorTo = Color.parseColor("#212121");
                ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo).setDuration(600);
                colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        mNoteView.setTextColor((int) animator.getAnimatedValue());
                    }

                });

                AnimatorSet set = new AnimatorSet();
                set.playTogether(colorAnim, tbAnim);
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mNoteView.setVisibility(View.VISIBLE);
                        if (hasText) {
                            mEditView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mTextBgView.setVisibility(View.INVISIBLE);
                        if (!hasText) {
                            mEditView.setVisibility(View.GONE);
                        }
                    }
                });
                set.start();
            }
            dSet.start();
        } else {
            dSet.setStartDelay(duration / 2);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(ntvAnim, nfvAnim, dSet);
            set.setDuration(duration);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mNoteFabView.setPivotX(mNoteFabView.getWidth() / 2);
                    mNoteFabView.setPivotY(mNoteFabView.getHeight());
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mActionNoteView.setVisibility(View.INVISIBLE);
                    mNoteTextView.setVisibility(View.INVISIBLE);
                    mNoteFabView.setVisibility(View.INVISIBLE);
                }
            });
            set.start();
        }
    }

    private void openEdit() {
        if (isAnim) {
            return;
        }
        isAnim = true;
        int actionNoteTop = ((ViewGroup) mActionNoteView.getParent()).getTop() + mActionNoteView.getTop()
                + mNoteFabView.getTop() + mNoteFabView.getHeight() / 2;
        int textBgTop = ((ViewGroup) mTextBgView.getParent()).getTop() + mTextBgView.getTop();
        int diffTop;
        if (actionNoteTop > textBgTop) {
            diffTop = actionNoteTop - textBgTop;
        } else {
            diffTop = 0;
        }

        int actionNoteLeft = ((ViewGroup) mActionNoteView.getParent()).getLeft() + mActionNoteView.getLeft()
                + ((ViewGroup) mNoteFabView.getParent()).getLeft() + mNoteFabView.getLeft() + mNoteFabView.getWidth() / 2;

        int cx = actionNoteLeft;
        int cy = diffTop;
        int finalRadius = Math.max(mTextBgView.getWidth(), mTextBgView.getHeight());
        Animator tbAnim = ViewAnimationUtils.createCircularReveal(mTextBgView, cx, cy, 0, finalRadius);
        tbAnim.setDuration(600);

        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        int y = DisplayUtils.dp2px(this, 10);
        ObjectAnimator ntvAnim = ObjectAnimator.ofPropertyValuesHolder(mNoteTextView,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0, y),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0)).setDuration(duration);

        ObjectAnimator nfvAnim = ObjectAnimator.ofPropertyValuesHolder(mNoteFabView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0)).setDuration(duration);

//        ObjectAnimator nvAnim = ObjectAnimator.ofPropertyValuesHolder(mNoteView,
//                PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0)).setDuration(duration);
//        nvAnim.setStartDelay(300);
//
//        ObjectAnimator evAnim = ObjectAnimator.ofPropertyValuesHolder(mEditView,
//                PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1)).setDuration(duration);
//        evAnim.setStartDelay(300);

        int colorFrom = Color.parseColor("#212121");
        int colorTo = Color.parseColor("#ccffffff");
        ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo).setDuration(600);
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mEditView.setTextColor((int) animator.getAnimatedValue());
            }

        });

        ObjectAnimator stvAnim = ObjectAnimator.ofPropertyValuesHolder(mSaveTextView,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0, y),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0)).setDuration(duration);
        stvAnim.setStartDelay(duration / 2);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(ntvAnim, nfvAnim, tbAnim, colorAnim, stvAnim);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mTextBgView.setVisibility(View.VISIBLE);
                mNoteFabView.setPivotX(mNoteFabView.getWidth() / 2);
                mNoteFabView.setPivotY(mNoteFabView.getHeight());
                mNoteView.setVisibility(View.GONE);
                mEditView.setVisibility(View.VISIBLE);
                if (mEditView.getSelectionEnd() == 0 && mEditView.getText().length() > 0) {
                    mEditView.setSelection(mEditView.getText().length());
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mActionNoteView.setVisibility(View.INVISIBLE);
                mNoteTextView.setVisibility(View.INVISIBLE);
                mNoteFabView.setVisibility(View.INVISIBLE);
                mSaveTextView.setVisibility(View.INVISIBLE);
                isAnim = false;
            }
        });
        set.start();

        if (mTipsAnim == null || !mTipsAnim.isRunning()) {
            //300 + 3000 + 300
            int size = (int) (getResources().getDimension(ResourcesUtils.getResIdOfAttr(this, android.R.attr.actionBarSize)) + 0.5f);
            mTipsAnim = ObjectAnimator.ofPropertyValuesHolder(mTipsView,
                    PropertyValuesHolder.ofKeyframe(View.TRANSLATION_Y,
                            Keyframe.ofFloat(0, -size),
                            Keyframe.ofFloat(0.0833f, 0),
                            Keyframe.ofFloat(0.9167f, 0),
                            Keyframe.ofFloat(1, -size))).setDuration(3600);
            mTipsAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mTipsView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mTipsView.setVisibility(View.INVISIBLE);
                }
            });
            mTipsAnim.start();
        }
    }

    private void getKeyboardHeight() {
        Rect r = new Rect();
        mRootView.getWindowVisibleDisplayFrame(r);

        int visibleHeight = r.height();

        if (mVisibleHeight == 0) {
            mVisibleHeight = visibleHeight;
            return;
        }

        if (mVisibleHeight == visibleHeight) {
            return;
        }

        mVisibleHeight = visibleHeight;

        if (mVisibleHeight < mEditTextLayout.getTop()) {
            mEditTextLayout.setY(mVisibleHeight - mEditTextLayout.getMeasuredHeight());
            mIsKeyboardShow = true;
        } else {
            mEditTextLayout.setY(mEditTextLayout.getTop());
            mIsKeyboardShow = false;
        }
    }

    public static class WarningDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("内容已编辑，是否要放弃编辑并离开？")
                    .setPositiveButton("是的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().setResult(RESULT_ABANDON);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("不是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // nope
                        }
                    }).create();
        }
    }

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            mAddrStr = location.getAddrStr();
        }

    }

}
