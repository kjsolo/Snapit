package cn.soloho.snapit.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.soloho.snapit.Bamboo;
import cn.soloho.snapit.R;
import cn.soloho.snapit.fragment.NoteListFragment;
import cn.soloho.snapit.model.Note;
import cn.soloho.snapit.tools.BitmapUtils;
import cn.soloho.snapit.tools.FileUtils;
import cn.soloho.snapit.widget.SToast;

/**
 * Created by solo on 15/2/18.
 */
public class MainActivity extends Activity {

    private static final int REQUEST_SNAP = 1;
    private static final int REQUEST_CAMERA_PICTURE = 2;

    private static final String FRAGMENT_TAG_NOTE_LIST = "note_list";

    @InjectView(R.id.fab)
    FloatingActionButton mFab;
    private Uri mTempPictureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, NoteListFragment.newInstance(), FRAGMENT_TAG_NOTE_LIST)
                    .commit();
        } else {
            mTempPictureUri = savedInstanceState.getParcelable("mTempPictureUri");
        }

    }

    @OnClick({R.id.fab})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab: {

                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    try {
                        // 获取缓存图片路径，告诉外部照相机需要保存到这个路径下
                        mTempPictureUri = Uri.fromFile(createTempPictureFile());

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPictureUri);
                        startActivityForResult(intent, REQUEST_CAMERA_PICTURE);

                    } catch (Exception e) {
                        e.printStackTrace();
                        SToast.show(this, "无法创建缓存图片");
                    }
                } else {
                    SToast.show(this, "外部储存器未准备好");
                }

//                startActivityForResult(new Intent(this, SnapActivity.class), REQUEST_SNAP,
//                        ActivityOptions.makeCustomAnimation(this,
//                                R.anim.activity_open_enter,
//                                R.anim.activity_open_exit).toBundle());
                break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("mTempPictureUri", mTempPictureUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SNAP: {
                if (resultCode == RESULT_OK) {
                    //((NoteListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_NOTE_LIST))
                }
                break;
            }
            case REQUEST_CAMERA_PICTURE: {
                if (resultCode == Activity.RESULT_OK) {

                    // 检查挂载
                    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

                        if (mTempPictureUri != null && new File(mTempPictureUri.getPath()).exists()) {

                            try {
                                // 创建需要保存的文件路径
                                String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                                File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                        Bamboo.Picture.NOTE_PICTURES);
                                File file = new File(dir, "Photo_" + ts + ".jpg");

                                // 获取图片拍照时的角度，便于修正
                                int degrees = BitmapUtils.getOrientation(this, mTempPictureUri);

                                // 图片的宽度
                                int width = getResources().getDisplayMetrics().widthPixels;

                                // 保存图片
                                BitmapUtils.compress(this,
                                        mTempPictureUri,    // 缓存图片路径
                                        file.getPath(),     // 图片保存的路径
                                        width,              // 图片宽度
                                        0,                  // 图片高度
                                        degrees);           // 图片旋转角度

                                // 创建.nomedia文件，可避免被扫描
                                FileUtils.createNoMediaFile(dir);

                                Note note = new Note();
                                note.setImageUri(Uri.fromFile(file).toString());
                                note.setRemark("Maybe I have a dream");
                                getContentResolver().insert(Note.CONTENT_URI, note.toContentValues());
                            } catch (Exception e) {
                                SToast.show(this, "无法保存图片");
                            }

                        } else {
                            SToast.show(this, "缓存图片文件不存在");
                        }

                    } else {
                        SToast.show(this, "外部储存器未准备好");
                    }
                }

                break;
            }
        }
    }

    /**
     * 获取暂存照片的文件
     *
     * @return 如果无法访问就会抛出IllegalStateException，如果找不到，就会抛出RuntimeException
     * @throws RuntimeException
     */
    private File createTempPictureFile() {
        File cacheDir = getExternalCacheDir();
        if (cacheDir == null) {
            // Returns null if external storage is not currently mounted
            // so it could not ensure the path exists
            throw new RuntimeException("External storage is not currently mounted");
        }

        File tempDir = new File(cacheDir, "TempPicture");

        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new RuntimeException("Directory " + tempDir.getName() + " create failure");
        }

        return new File(tempDir, "pick_picture");
    }


}
