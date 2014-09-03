package cz.mzk.kramerius.app.util;


public class BitmapUtil {

	private static final String TAG = BitmapUtil.class.getName();
	
//	public static Bitmap getBitmapFromURL(String src) {
//		Log.d(TAG, "bitmap:" + src);
//		try {
//			URL url = new URL(src);
//			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//			connection.setDoInput(true);
//			connection.connect();
//			InputStream input = connection.getInputStream();
//			Bitmap myBitmap = BitmapFactory.decodeStream(input);
//			return myBitmap;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	
//	public static Bitmap getBitmapFromURL(String src, String name, String password) {
//		Log.d(TAG, "bitmap-auth:" + src);
//		try {
//			URL url = new URL(src);
//			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//			final String basicAuth = "Basic " + Base64.encodeToString((name + ":" + password).getBytes(), Base64.NO_WRAP);
//			connection.setRequestProperty("Authorization", basicAuth);
//			connection.setDoInput(true);
//			connection.connect();
//			InputStream input = connection.getInputStream();
//			Bitmap myBitmap = BitmapFactory.decodeStream(input);
//			return myBitmap;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

}
