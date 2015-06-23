package cz.mzk.kramerius.app.util;

import java.io.ByteArrayOutputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtil {

	private static final String TAG = BitmapUtil.class.getName();

	// public static Bitmap getBitmapFromURL(String src) {
	// Log.d(TAG, "bitmap:" + src);
	// try {
	// URL url = new URL(src);
	// HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	// connection.setDoInput(true);
	// connection.connect();
	// InputStream input = connection.getInputStream();
	// Bitmap myBitmap = BitmapFactory.decodeStream(input);
	// return myBitmap;
	// } catch (IOException e) {
	// e.printStackTrace();
	// return null;
	// }
	// }
	//
	//
	// public static Bitmap getBitmapFromURL(String src, String name, String password) {
	// Log.d(TAG, "bitmap-auth:" + src);
	// try {
	// URL url = new URL(src);
	// HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	// final String basicAuth = "Basic " + Base64.encodeToString((name + ":" + password).getBytes(), Base64.NO_WRAP);
	// connection.setRequestProperty("Authorization", basicAuth);
	// connection.setDoInput(true);
	// connection.connect();
	// InputStream input = connection.getInputStream();
	// Bitmap myBitmap = BitmapFactory.decodeStream(input);
	// return myBitmap;
	// } catch (IOException e) {
	// e.printStackTrace();
	// return null;
	// }
	// }

	public static Bitmap decodeAndScaleBitmap(Resources res, int resId, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public static Bitmap scaleBitmap(Bitmap original, int reqWidth, int reqHeight) {
		// original bitmap to byte[]
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		original.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

}
