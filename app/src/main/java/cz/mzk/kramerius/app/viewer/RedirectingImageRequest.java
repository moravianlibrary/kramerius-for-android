package cz.mzk.kramerius.app.viewer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.widget.ImageView.ScaleType;

import com.android.volley.NetworkResponse;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import cz.mzk.kramerius.app.util.VersionUtils;

public class RedirectingImageRequest extends ImageRequest {

	private static final String TAG = RedirectingImageRequest.class.getSimpleName();

	/**
	 * RGB_565 zatim nema smysl resit. Napr na N10 se stahne stranka ve velikosti 1011x1500=1516500 px. Coz pro bitmapu v pameti
	 * je asi 3MB v ARGB_8888 a 6MB v RGB_565. Jenze zobrazena (a v pameti) je vzdy jen jedna stranka a pro Volley pouzivame jen
	 * diskovou cache.
	 */
	private static final Config BMP_CONFIG = Config.ARGB_8888;

	// unfortunatelly these are not accessible so I must keep copies
	private final Listener<Bitmap> listener;
	private final ErrorListener errorListener;

	public RedirectingImageRequest(String url, Listener<Bitmap> listener, ErrorListener errorListener) {
		super(url, listener, 0, 0, ScaleType.CENTER, BMP_CONFIG, errorListener);
		this.listener = listener;
		this.errorListener = errorListener;
	}

	@Override
	public void deliverError(VolleyError error) {
		NetworkResponse response = error.networkResponse;
		if (response != null) {
			final int status = response.statusCode;
			// Handle 30x
			switch (status) {
			case 300:
			case 301:
			case 302:
			case 303:
			case 305:
			case 307:
				String location = error.networkResponse.headers.get("Location");
				if (VersionUtils.Debuggable()) {
					Log.d(TAG, "Redirecting, Location: " + location);
				}
				VolleyRequestManager.addToRequestQueue(new RedirectingImageRequest(location, listener, errorListener));
				break;
			default:
				super.deliverError(error);
			}
		} else {
			super.deliverError(error);
		}
	}

}
