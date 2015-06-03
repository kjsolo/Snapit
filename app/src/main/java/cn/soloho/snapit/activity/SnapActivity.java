package cn.soloho.snapit.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.commonsware.cwac.camera.CameraFragment;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.soloho.snapit.R;

/**
 * Created by solo on 15/2/19.
 */
public class SnapActivity extends Activity {

    private static final String CAMERA_FRAGMENT = "camera";

    @InjectView(R.id.snap) View mSnapView;
    @InjectView(R.id.flash1) View mFlash1View;
    @InjectView(R.id.flash2) View mFlash2View;
    @InjectView(R.id.camera_layout) View mCameraLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snap);
        ButterKnife.inject(this);

        if (null == savedInstanceState) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.cameraFragment, new MyCameraFragment(), CAMERA_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
    }

    @OnClick(R.id.snap)
    public void onSnapClick(View view) {
        ((CameraFragment) getFragmentManager().findFragmentByTag(CAMERA_FRAGMENT))
                .takePicture(true, false);
    }

    private void animFlash() {
        int y = mCameraLayout.getHeight() / 2;

        ValueAnimator animator = ValueAnimator.ofInt(0, y, 0);
        animator.setDuration(400);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams lp1 = mFlash1View.getLayoutParams();
                lp1.height = (int) animation.getAnimatedValue();
                mFlash1View.setLayoutParams(lp1);

                ViewGroup.LayoutParams lp2 = mFlash2View.getLayoutParams();
                lp2.height = (int) animation.getAnimatedValue();
                mFlash2View.setLayoutParams(lp2);
            }
        });
        animator.start();
    }

    public static class MyCameraFragment extends CameraFragment {

        private static final int REQUEST_EDIT = 1;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHost(new MyCameraHost(getActivity()));
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case REQUEST_EDIT:{
                    if (resultCode == RESULT_OK) {
                        getActivity().setResult(RESULT_OK);
                        getActivity().finish();
                    } else if (resultCode == EditNoteActivity.RESULT_ABANDON) {
                        getActivity().finish();
                    }
                    break;
                }
            }
        }

        public class MyCameraHost extends SimpleCameraHost {

            public MyCameraHost(Context context) {
                super(context);
            }

            @Override
            protected String getPhotoFilename() {
                return "Photo_temp.jpg";
            }

            @Override
            protected File getPhotoDirectory() {
                return getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            }

            @Override
            protected boolean scanSavedImage() {
                return false;
            }

            @Override
            public RecordingHint getRecordingHint() {
                return RecordingHint.STILL_ONLY;
            }

            @Override
            public boolean useSingleShotMode() {
                return true;
            }

            @Override
            public void saveImage(PictureTransaction xact, byte[] image) {
                //super.saveImage(xact, image);
            }

            @Override
            public void saveImage(PictureTransaction xact, Bitmap bitmap) {
                File file = getPhotoPath();

                if (file.exists()) {
                    file.delete();
                } else {
                    File p = file.getParentFile();
                    if (null != p) {
                        p.mkdirs();
                    }
                }

                //Bitmap newBitmap = TopCropUtils.crop(bitmap, bitmap.getWidth(), bitmap.getWidth(), TopCropUtils.OPTIONS_RECYCLE_INPUT);

                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();

                    startActivityForResult(new Intent(getActivity(), NoteShowActivity.class)
                            .putExtra(EditNoteActivity.EXTRA_URI, Uri.fromFile(file)), REQUEST_EDIT,
                            ActivityOptions.makeCustomAnimation(getActivity(),
                                    R.anim.activity_open_enter,
                                    R.anim.activity_open_exit).toBundle());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public Camera.ShutterCallback getShutterCallback() {
                return new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                        ((SnapActivity) getActivity()).animFlash();
                    }
                };
            }

            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                //super.onAutoFocus(success, camera);
            }

            @Override
            public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                return parameters;
            }

        }

    }
}
