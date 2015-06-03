package solo.snapit.tools;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {

    public static int getOrientation(Context context, Uri photoUri) {
        int orientation = 0;
        if ("file".equals(photoUri.getScheme())) {
            try {
                ExifInterface exif = new ExifInterface(photoUri.getPath());
                int orientationAttr = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);

                switch (orientationAttr) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        orientation = 270;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        orientation = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        orientation = 90;
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("content".equals(photoUri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(photoUri,
                    new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                orientation = cursor.getInt(0);
                cursor.close();
            }
        }
        return orientation;
    }


    public static byte[] bitmap2byteArray(String pathName) {
		Bitmap bm = BitmapFactory.decodeFile(pathName);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		return baos.toByteArray();
	}

	public static boolean compress(Context context, Uri uri, String path, int reqWidth, int reqHeight, int degrees) throws IOException {
		Bitmap b = decodeSampleFromUri(context, uri, reqWidth, reqHeight);
		if (b != null) {
			return save(context, b, new File(path), degrees);
		}
		return false;
	}

	public static boolean save(Context context, Bitmap bmp, File file, int degrees) throws IOException {
        bmp = rotateImage(bmp, degrees);

        File p = file.getParentFile();
        if (p != null && !p.exists() && !p.mkdirs()) {
            throw new RuntimeException("Directory " + p.getName() + " create failure");
        }
		FileOutputStream out = new FileOutputStream(file);
		// 100 means no compression
		boolean isCompress = bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
		out.flush();
		out.close();
		
		return isCompress;
	}

    public static Bitmap rotateImage(Bitmap bmp, int degrees) {
        if (degrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        }
        return bmp;
    }

	public static Bitmap decodeSampleFromUri(Context context, Uri uri, int reqWidth, int reqHeight) throws IOException {
		if (reqWidth == 0 && reqHeight == 0) {
			return null;
		}
		
		InputStream input = context.getContentResolver().openInputStream(uri);
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(input, null, options);
		input.close();
		
		computeSize(options, reqWidth, reqHeight);
	    
	    input = context.getContentResolver().openInputStream(uri);
	    options.inJustDecodeBounds = false;
	    Bitmap sampleBitmap = BitmapFactory.decodeStream(input, null, options);
	    input.close();
	    return sampleBitmap;
	}
	
	private static void computeSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		int newWidth = reqWidth;
		int newHeight = reqHeight;
		if (reqWidth != reqHeight) {
			final int bitmapHeight = options.outHeight;
		    final int bitmapWidth = options.outWidth;
			
			if (reqHeight == 0) {
				float scaleWidht = ((float) reqWidth / bitmapWidth);
				newHeight = (int) (bitmapHeight * scaleWidht);
			}
			
			if (reqWidth == 0) {
				float scaleHeight = ((float) reqHeight / bitmapHeight);
				newWidth = (int) (bitmapWidth * scaleHeight);
			}
		}
		options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight);
	}
	
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}
	
}
