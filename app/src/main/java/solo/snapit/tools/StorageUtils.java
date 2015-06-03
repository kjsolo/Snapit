package solo.snapit.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 保存文件
 *
 * @author hosolo
 */
public class StorageUtils {

    private File mMainFile;

    /**
     * {@link Environment#getExternalStoragePublicDirectory(String type)}
     */
    public StorageUtils(String mainName) {
        mMainFile = Environment.getExternalStoragePublicDirectory(mainName);
    }

    public StorageUtils(File mainFile) {
        mMainFile = mainFile;
    }

    public File getMainFile() {
        return mMainFile;
    }

    public File create(String name) {
        return new File(mMainFile, name);
    }

    public File createDirIfNotExists(String name) {
        File file = new File(getMainFile(), name);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 检查外部储存是否可读可写
     *
     * @return
     */
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 保存图片
     *
     * @param context
     * @param dirName  文件夹名字
     * @param fileName 文件名
     * @param bmp
     * @param degrees  旋转度数
     * @param noMedia  是否同时创建.nomedia文件
     * @return
     * @throws IOException
     */
    public Uri saveBitmap(Context context, String dirName, String fileName,
                          Bitmap bmp, int degrees, boolean noMedia) throws IOException {
        if (bmp == null) {
            return null;
        }
        if (!StorageUtils.isExternalStorageWritable()) {
            return null;
        }

        File dir;
        if (dirName == null) {
            dir = getMainFile();
        } else {
            dir = createDirIfNotExists(dirName);
        }

        if (noMedia) {
            FileUtils.createNoMediaFile(dir);
        }

        // 先保存原图
        File file = new File(dir, fileName);
        BitmapUtils.save(context, bmp, file, 0);

        Uri uri = Uri.fromFile(file);
        boolean ok = BitmapUtils.compress(context, uri, file.getPath(), 600, 0, degrees);
        if (ok) {
            uri = Uri.fromFile(file);
            return uri;
        }
        return null;
    }

    public Uri saveBitmap(Context context, String dirName, Bitmap bmp, int degrees) throws IOException {
        String name = createFileName("jpg");
        return saveBitmap(context, dirName, name, bmp, degrees, true);
    }

    public Uri saveBitmap(Context context, String dirName, Uri uri) {
        if (uri == null) {
            return null;
        }
        if (!StorageUtils.isExternalStorageWritable()) {
            return null;
        }

        int degrees = BitmapUtils.getOrientation(context, uri);

        try {
            File dir = createDirIfNotExists(dirName);
            FileUtils.createNoMediaFile(dir);
            String name = createFileName("jpg");
            File file = new File(dir, name);

            boolean ok = BitmapUtils.compress(context, uri, file.getPath(), 600, 0, degrees);
            if (ok) {
                uri = Uri.fromFile(file);
                return uri;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Uri copyFile(Context context, String dirName, Uri uri, String suffix) {
        if (uri == null) {
            return null;
        }
        if (!StorageUtils.isExternalStorageWritable()) {
            return null;
        }
        File dir = createDirIfNotExists(dirName);
        FileUtils.createNoMediaFile(dir);
        String name = createFileName(suffix);
        try {
            File sourceFile = new File(uri.getPath());
            File destFile = new File(dir, name);
            FileUtils.copyFile(sourceFile, destFile);

            uri = Uri.fromFile(destFile);
            return uri;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String createFileName(String suffix) {
        final SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.getDefault());
        final String name = formater.format(new Date()) + "." + suffix;
        return name;
    }

    public void clearFiles(Context context, String dirName) {
        FileUtils.deleteAllFilesOfDir(new File(getMainFile(), dirName), false);
    }

}
