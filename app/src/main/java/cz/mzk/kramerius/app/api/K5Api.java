package cz.mzk.kramerius.app.api;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import android.preference.PreferenceManager;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Domain;
import cz.mzk.kramerius.app.model.Track.AudioFormat;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.service.MediaPlayerService;
import cz.mzk.kramerius.app.util.DomainUtil;

public class K5Api {

	public static final int STATUS_UNKNOWN = 0;
	public static final int STATUS_PDF_FAILED = 1;
	public static final int STATUS_PDF_OK = 2;
	public static final int STATUS_PDF_FORBIDDEN = 3;

	public static final int FEED_NO_LIMIT = -1;

	public static final int FEED_NEWEST = 0;
	public static final int FEED_MOST_DESIRABLE = 1;
	public static final int FEED_CUSTOM = 2;

	private static final String PATH_API = "api";
	private static final String PATH_SEARCH = "search";
	private static final String PATH_HANDLE = "handle";
	private static final String PATH_API_VERSION = "v5.0";

	private static final String PATH_FEED = "feed";
	private static final String PATH_MOST_DESIRABLE = "mostdesirable";
	private static final String PATH_NEWEST = "newest";
	private static final String PATH_CUSTOM = "custom";
	private static final String PATH_USER = "user";
	private static final String PATH_RIGHTS = "rights";
	private static final String PATH_VC = "vc";
	private static final String PATH_ITEM = "item";
	private static final String PATH_CHILDREN = "children";
	private static final String PATH_STREAM = "streams";
	private static final String PATH_MODS = "BIBLIO_MODS";
	// private static final String PATH_MP3 = "MP3";
	private static final String PATH_IMG = "img";
	private static final String PATH_THUMB = "thumb";

	private static final String PARAM_LIMIT = "limit";
	private static final String PARAM_MODEL = "type";
	private static final String PARAM_POLICY = "policy";
	private static final String PARAM_PID = "pid";
	private static final String PARAM_STREAM = "stream";
	private static final String PARAM_ACTION = "action";

	private static final String STREAM_IMG_FULL = "IMG_FULL";

	private static final String ACTION_RAW = "GETRAW";

	public static String getDomain(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(
				context.getString(R.string.pref_domain_key), context.getString(R.string.pref_domain_default));
	}

	public static void setDomain(Context context, String domain, String protocol) {
	    killAudioPlayerIfDomainChanged(context, domain);
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putString(context.getString(R.string.pref_domain_key), domain)
				.putString(context.getString(R.string.pref_protocol_key), protocol).commit();
		K5ConnectorFactory.getConnector().restart();
	}
	
	private static void killAudioPlayerIfDomainChanged(Context context, String domain) {
        if (!domain.equals(getDomain(context))) {
            PendingIntent intent = MediaPlayerService.buildStopServicePendingIntent(context);
            try {
                intent.send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        }
    }

	public static void setDomain(Context context, Domain domain) {
		if (DomainUtil.getCurrentDomain(context).getUrl().equals(domain.getUrl())) {
			return;
		}
		setDomain(context, domain.getDomain(), domain.getProtocol());
	}

	public static String getProtocol(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(
				context.getString(R.string.pref_protocol_key), context.getString(R.string.pref_protocol_default));
	}

	public static Uri getBaseUri(Context context) {
		// if(getDomain(context).equals("kramerius.zcm.cz")) {
		// return Uri.parse(getProtocol(context) + "://" + getDomain(context));
		// } else if(getDomain(context).contains(":") || getDomain(context).contains("/")) {
		// return Uri.parse(getProtocol(context) + "://" + getDomain(context) + "/" + PATH_SEARCH);
		// }
		return new Uri.Builder().scheme(getProtocol(context)).authority(getDomain(context)).appendPath(PATH_SEARCH)
				.build();
	}

	public static Uri getApiUri(Context context) {
		return getBaseUri(context).buildUpon().appendPath(PATH_API).appendPath(PATH_API_VERSION).build();
	}

	public static String getUser(Context context) {
		String userPass = PreferenceManager.getDefaultSharedPreferences(context).getString(
				getDomain(context) + "_user_pass", "");
		if (!userPass.contains(";")) {
			return null;
		}
		return userPass.substring(0, userPass.indexOf(";"));
	}

	public static String getPassword(Context context) {
		String userPass = PreferenceManager.getDefaultSharedPreferences(context).getString(
				getDomain(context) + "_user_pass", "");
		if (!userPass.contains(";")) {
			return null;
		}
		return userPass.substring(userPass.indexOf(";") + 1, userPass.length());
	}

	public static void storeUser(Context context, String userName, String password) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putString(getDomain(context) + "_user_pass", userName + ";" + password).commit();
	}

	public static void logOut(Context context) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().remove(getDomain(context) + "_user_pass")
				.commit();
		K5ConnectorFactory.getConnector().restart();
	}

	public static boolean isLoggedIn(Context context) {
		String userPass = PreferenceManager.getDefaultSharedPreferences(context).getString(
				getDomain(context) + "_user_pass", "");
		return userPass.contains(";");
	}

	public static String getThumbnailPath(Context context, String pid) {
		return getItemUri(context, pid).buildUpon().appendPath(PATH_THUMB).build().toString();
		// return
		// getBaseUri(context).buildUpon().appendPath("img").appendQueryParameter("uuid",
		// uuid)
		// .appendQueryParameter(PARAM_STREAM, "IMG_THUMB").build().toString();
	}

	public static String getFullImagePath(Context context, String uuid) {
		return getBaseUri(context).buildUpon().appendPath("img").appendQueryParameter("uuid", uuid)
				.appendQueryParameter(PARAM_STREAM, "IMG_FULL").build().toString();
	}

	public static Uri getItemUri(Context context, String pid) {
		return getApiUri(context).buildUpon().appendPath(PATH_ITEM).appendPath(pid).build();
	}

	public static Uri getStreamUri(Context context, String pid) {
		return getItemUri(context, pid).buildUpon().appendPath(PATH_STREAM).build();
	}

	public static Uri getVirtualCollectionsUri(Context context) {
		return getApiUri(context).buildUpon().appendPath(PATH_VC).build();
	}

	public static String getVirtualCollectionsPath(Context context) {
		return getVirtualCollectionsUri(context).toString();
	}

	public static String getFeedPath(Context context, int feed, int limit, String policy, String model) {
		String feedType = "";
		switch (feed) {
		case FEED_NEWEST:
			feedType = PATH_NEWEST;
			break;
		case FEED_MOST_DESIRABLE:
			feedType = PATH_MOST_DESIRABLE;
			break;
		case FEED_CUSTOM:
			feedType = PATH_CUSTOM;
			break;
		}

		Builder builder = getApiUri(context).buildUpon().appendPath(PATH_FEED).appendPath(feedType);
		if (limit != FEED_NO_LIMIT) {
			builder = builder.appendQueryParameter(PARAM_LIMIT, String.valueOf(limit));
		}
		if (policy != null) {
			builder = builder.appendQueryParameter(PARAM_POLICY, policy);
		}
		if (model != null) {
			builder = builder.appendQueryParameter(PARAM_MODEL, model);
		}
		return builder.build().toString();

	}

	public static String getItemPath(Context context, String pid) {
		return getItemUri(context, pid).toString();
	}

	public static String getChildrenPath(Context context, String pid) {
		return getItemUri(context, pid).buildUpon().appendPath(PATH_CHILDREN).build().toString();
	}

	public static String getUserPath(Context context) {
		return getApiUri(context).buildUpon().appendPath(PATH_USER).build().toString();
	}

	public static String getUserRightsPath(Context context) {
		return getApiUri(context).buildUpon().appendPath(PATH_RIGHTS).build().toString();
	}

	public static String getModsStreamPath(Context context, String pid) {
		return getStreamUri(context, pid).buildUpon().appendPath(PATH_MODS).build().toString();
	}

	public static String getAudioStreamPath(Context context, String pid, AudioFormat format) {
		// tyhle verze neumi range
		// return "http://kramerius.mzk.cz/search/api/v5.0/item/" + pid + "/streams/OGG";
		// return "http://kramerius.mzk.cz/search/api/v5.0/item/" + pid + "/streams/MP3";

		// u OGG vraci MediaPlayer.getDuration() vzdy 0
		// return "http://kramerius.mzk.cz/search/audioProxy/" + pid + "/OGG";
		// tohle je asi nejlepsi
		// return "http://kramerius.mzk.cz/search/audioProxy/" + pid + "/MP3";

		// return "http://kramerius.mzk.cz/search/audioProxy/" + pid + "/" + format;
		return getBaseUri(context).toString() + "/audioProxy/" + pid + "/" + format.toString();

		// TODO: replace with this code after fix of this issue is tested:
		// https://github.com/ceskaexpedice/kramerius/issues/109
		// return getStreamUri(context, pid).buildUpon().appendPath(format.toString()).build().toString();
	}

	public static String getSearchPath(Context context, String query, int start, int rows) {
		return getApiUri(context).buildUpon().appendPath(PATH_SEARCH).appendQueryParameter("q", query)
				.appendQueryParameter("start", String.valueOf(start))
				.appendQueryParameter("rows", String.valueOf(rows)).build().toString();
	}

	public static String getPersistentUrl(Context context, String pid) {
		return getBaseUri(context).buildUpon().appendPath(PATH_HANDLE).appendPath(pid).toString();
	}

	public static String getDoctypeCountPath(Context context, String type) {
		return getApiUri(context).buildUpon().appendPath(PATH_SEARCH)
				.appendQueryParameter("q", SearchQuery.MODEL + ":" + type).appendQueryParameter("rows", "0").build()
				.toString();
	}

	public static String getPdfPath(Context context, String pid) {
		return getBaseUri(context).buildUpon().appendPath(PATH_IMG).appendQueryParameter(PARAM_PID, pid)
				.appendQueryParameter(PARAM_STREAM, STREAM_IMG_FULL).appendQueryParameter(PARAM_ACTION, ACTION_RAW)
				.build().toString();
	}

}
