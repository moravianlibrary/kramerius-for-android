package cz.mzk.kramerius.app.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import cz.mzk.kramerius.app.metadata.Metadata;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.User;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.util.LangUtils;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.VersionUtils;
import cz.mzk.kramerius.app.xml.AltoParser;
import cz.mzk.kramerius.app.xml.ModsParser;

/**
 * Deprecated implementation suitable only for Android versions before 9. And minSdkVersion of this app is 14.
 * 
 * @see http://android-developers.blogspot.cz/2011/09/androids-http-clients.html
 * 
 */
@Deprecated
public class K5ConnectorImplAndroidHttpClient implements K5Connector {

	private static final String LOG_TAG = K5ConnectorImplAndroidHttpClient.class.getName();

	public static final int CONNECTION_TIMEOUT = 10;
	public static final int SOCKET_TIMEOUT = 10;

	private DefaultHttpClient mClient;

	private DefaultHttpClient getClient() {
		if (mClient == null) {
			mClient = createClient();
		}
		return mClient;
	}

	private DefaultHttpClient createClient() {
		DefaultHttpClient client = new DefaultHttpClient();
		final HttpParams httpParameters = client.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT * 1000);
		HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT * 1000);
		return client;
	}

	@Override
	public void restart() {
		mClient = createClient();
	}

	private Item addItemToList(List<Item> list, String pid, String model, String title) {
		Item item = new Item();
		item.setModel(model);
		item.setPid(pid);
		item.setRootPid(pid);
		item.setTitle(title);
		item.setRootTitle(title);
		list.add(item);
		return item;
	}

	@Override
	public List<Item> getFeatured(Context context, int feed, int limit, String policy, String model) {
		try {
			List<Item> list = new ArrayList<Item>();
			String requst = null;
			requst = K5Api.getFeedPath(context, feed, limit, policy, model);
			if (VersionUtils.Debuggable()) {
				Log.d(LOG_TAG, "request:" + requst + ", " + feed);
			}
			HttpGet request = new HttpGet(requst);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONObject json = (JSONObject) new JSONTokener(jsonString).nextValue();
			JSONArray data = json.getJSONArray(K5Constants.DATA);
			for (int i = 0; i < data.length(); i++) {
				Item item = new Item();
				JSONObject jsonItem = data.getJSONObject(i);
				String m = jsonItem.optString(K5Constants.MODEL);
				if (ModelUtil.PAGE.equals(m) || m.isEmpty()) {
					continue;
				}
				// if(ModelUtil.PAGE.equals(m)) {
				// item.setPid(jsonItem.optString(K5Constants.ROOT_PID));
				// item.setModel(ModelUtil.MONOGRAPH);
				// item.setTitle(jsonItem.optString(K5Constants.ROOT_TITLE));
				// } else {
				// item.setPid(jsonItem.optString(K5Constants.PID));
				// item.setModel(jsonItem.optString(K5Constants.MODEL));
				// item.setTitle(jsonItem.optString(K5Constants.TITLE));
				// }
				item.setPid(jsonItem.optString(K5Constants.PID));
				item.setModel(jsonItem.optString(K5Constants.MODEL));
				item.setTitle(jsonItem.optString(K5Constants.TITLE));

				item.setIssn(jsonItem.optString(K5Constants.ISSN));
				item.setDate(jsonItem.optString(K5Constants.DATE));

				if (K5Constants.MIME_TYPE_PDF.equals(jsonItem.optString(K5Constants.MIME_TYPE))) {
					item.setPdf(K5Api.getPdfPath(context, item.getPid()));
				}

				JSONArray authors = jsonItem.optJSONArray(K5Constants.AUTHOR);
				if (authors != null && authors.length() > 0) {
					item.setAuthor(authors.optString(0));
				}
				item.setRootTitle(jsonItem.optString(K5Constants.ROOT_TITLE));
				item.setRootPid(jsonItem.optString(K5Constants.ROOT_PID));
				item.setPolicyPrivate(K5Constants.POLICY_PRIVATE.equals(jsonItem.optString(K5Constants.POLICY)));
				list.add(item);
			}
			return list;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<Pair<String, String>> getHierarychy(Context context, String pid) {
		try {
			String requst = K5Api.getItemPath(context, pid);
			HttpGet request = new HttpGet(requst);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONObject jsonItem = (JSONObject) new JSONTokener(jsonString).nextValue();
			List<Pair<String, String>> hierarchy = new ArrayList<Pair<String, String>>();

			JSONArray a = jsonItem.optJSONArray(K5Constants.CONTEXT);
			if (a == null) {
				hierarchy.add(new Pair<String, String>(jsonItem.optString(K5Constants.PID), jsonItem
						.optString(K5Constants.MODEL)));
			} else {
				if (a.length() == 1 && a.get(0) instanceof JSONArray) {
					a = (JSONArray) a.get(0);
				}
				for (int i = 0; i < a.length(); i++) {
					JSONObject c = a.getJSONObject(i);
					if (c != null) {
						hierarchy.add(new Pair<String, String>(c.optString(K5Constants.PID), c
								.optString(K5Constants.MODEL)));
					}
				}
			}
			return hierarchy;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Item getItem(Context context, String pid, String domain) {
		try {
			String requst = K5Api.getItemPath(context, pid);
			if (VersionUtils.Debuggable()) {
				Log.d(LOG_TAG, "getItem - Request:" + requst);
			}
			requst.replace(K5Api.getDomain(context), domain);
			HttpGet request = new HttpGet(requst);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			if (VersionUtils.Debuggable()) {
				Log.d(LOG_TAG, "getItem - Response:" + jsonString);
			}
			JSONObject jsonItem = (JSONObject) new JSONTokener(jsonString).nextValue();
			Item item = new Item();
			item.setPid(jsonItem.optString(K5Constants.PID));
			item.setModel(jsonItem.optString(K5Constants.MODEL));
			item.setIssn(jsonItem.optString(K5Constants.ISSN));
			item.setDate(jsonItem.optString(K5Constants.DATE));
			item.setTitle(jsonItem.optString(K5Constants.TITLE));
			item.setRootTitle(jsonItem.optString(K5Constants.ROOT_TITLE));
			JSONObject pdf = jsonItem.optJSONObject(K5Constants.PDF);
			if (pdf != null) {
				String pdfUrl = pdf.optString(K5Constants.PDF_URL);
				if (pdfUrl != null) {
					item.setPdf(pdfUrl);
				}
			}
			item.setRootPid(jsonItem.optString(K5Constants.ROOT_PID));
			item.setPolicyPrivate(K5Constants.POLICY_PRIVATE.equals(jsonItem.optString(K5Constants.POLICY)));

			JSONObject details = jsonItem.optJSONObject(K5Constants.DETAILS);
			if (details != null) {
				item.setYear(details.optString(K5Constants.DETAILS_YEAR));
				item.setVolumeNumber(details.optString(K5Constants.DETAILS_VOLUME_NUMBER));
				item.setIssueNumber(details.optString(K5Constants.DETAILS_ISSUE_NUMBER));
				item.setPeriodicalItemDate(details.optString(K5Constants.DETAILS_DATE));
				item.setPartNumber(details.optString(K5Constants.DETAILS_PART_NUMBER));
			}

			return item;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Item getItem(Context context, String pid) {
		return getItem(context, pid, K5Api.getDomain(context));
	}

	@Override
	public List<Item> getChildren(Context context, String pid, String modelFilter) {
		try {
			List<Item> list = new ArrayList<Item>();
			String requst = K5Api.getChildrenPath(context, pid);
			HttpGet request = new HttpGet(requst);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONArray data = (JSONArray) new JSONTokener(jsonString).nextValue();
			for (int i = 0; i < data.length(); i++) {
				Item item = new Item();
				JSONObject jsonItem = data.getJSONObject(i);
				String model = jsonItem.optString(K5Constants.MODEL);
				if (modelFilter != null && !modelFilter.equals(model)) {
					continue;
				}
				item.setPid(jsonItem.optString(K5Constants.PID));
				item.setModel(model);
				item.setIssn(jsonItem.optString(K5Constants.ISSN));
				item.setDate(jsonItem.optString(K5Constants.DATE));
				item.setTitle(jsonItem.optString(K5Constants.TITLE));
				item.setRootTitle(jsonItem.optString(K5Constants.ROOT_TITLE));
				item.setRootPid(jsonItem.optString(K5Constants.ROOT_PID));
				item.setPolicyPrivate(K5Constants.POLICY_PRIVATE.equals(jsonItem.optString(K5Constants.POLICY)));
				JSONObject details = jsonItem.optJSONObject(K5Constants.DETAILS);
				if (details != null) {
					item.setYear(details.optString(K5Constants.DETAILS_YEAR));
					item.setVolumeNumber(details.optString(K5Constants.DETAILS_VOLUME_NUMBER));
					item.setIssueNumber(details.optString(K5Constants.DETAILS_ISSUE_NUMBER));
					item.setPeriodicalItemDate(details.optString(K5Constants.DETAILS_DATE));
					item.setPartNumber(details.optString(K5Constants.DETAILS_PART_NUMBER));
				}
				list.add(item);
			}
			return list;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<Item> getChildren(Context context, String pid) {
		return getChildren(context, pid, null);
	}

	@Override
	public List<Item> getVirtualCollections(Context context) {
		try {
			List<Item> list = new ArrayList<Item>();
			HttpGet request = new HttpGet(K5Api.getVirtualCollectionsPath(context));
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONArray data = (JSONArray) new JSONTokener(jsonString).nextValue();
			for (int i = 0; i < data.length(); i++) {
				Item item = new Item();
				JSONObject jsonItem = data.getJSONObject(i);
				item.setPid(jsonItem.optString(K5Constants.PID));
				item.setTitle(jsonItem.getJSONObject(K5Constants.VC_DESCS).optString(LangUtils.getLanguage()));
				list.add(item);
			}
			return list;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public User getUserInfo(Context context) {
		String userName = K5Api.getUser(context);
		String password = K5Api.getPassword(context);
		return getUserInfo(context, userName, password);
	}

	@Override
	public User getUserInfo(Context context, String name, String password) {
		final String basicAuth = "Basic " + Base64.encodeToString((name + ":" + password).getBytes(), Base64.NO_WRAP);
		try {
			String requestPath = K5Api.getUserPath(context);
			HttpGet request = new HttpGet(requestPath);
			request.setHeader("Authorization", basicAuth);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONObject json = (JSONObject) new JSONTokener(jsonString).nextValue();
			User user = new User();
			user.setPassword(password);
			user.setFirstName(json.optString("firstname"));
			user.setSurname(json.optString("surname"));
			user.setLogin(json.optString("lname"));
			if (user.getLogin().equals("not_logged")) {
				return null;
			}

			JSONArray roles = json.getJSONArray("roles");
			for (int i = 0; i < roles.length(); i++) {
				user.addRole(roles.getJSONObject(i).getString("name"));
			}
			return user;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Pair<List<Item>, Integer> getSearchResult(Context context, String query, int start, int rows) {
		try {
			String requestPath = K5Api.getSearchPath(context, query, start, rows);
			HttpGet request = new HttpGet(requestPath);
			if (VersionUtils.Debuggable()) {
				Log.d(LOG_TAG, "query - search:" + requestPath);
			}
			request.setHeader("accept", "application/json");
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			if (VersionUtils.Debuggable()) {
				Log.d(LOG_TAG, "search result:" + jsonString);
			}
			JSONObject json = (JSONObject) new JSONTokener(jsonString).nextValue();
			JSONObject responseJson = json.optJSONObject("response");
			if (responseJson == null) {
				return null;
			}
			int numFound = responseJson.optInt("numFound");
			JSONArray itemArray = responseJson.optJSONArray("docs");
			if (itemArray == null) {
				return null;
			}
			List<Item> items = new ArrayList<Item>();
			for (int i = 0; i < itemArray.length(); i++) {
				JSONObject itemJson = itemArray.getJSONObject(i);
				Item item = new Item();
				item.setPid(itemJson.optString("PID"));
				item.setTitle(itemJson.optString("dc.title"));
				item.setRootTitle(itemJson.optString("root_title"));
				item.setRootPid(itemJson.optString("root_pid"));
				item.setRootPid(itemJson.optString("root_pid"));

				if ("application/pdf".equals(itemJson.optString("img_full_mime"))) {
					item.setPdf(K5Api.getPdfPath(context, item.getPid()));
				}

				JSONArray authors = itemJson.optJSONArray("dc.creator");
				if (authors != null && authors.length() > 0) {
					item.setAuthor(authors.optString(0));
				}
				item.setPolicyPrivate("private".equals(itemJson.optString("dostupnost")));
				item.setModel(itemJson.optString(SearchQuery.MODEL));
				// JSONArray models = itemJson.optJSONArray(SearchQuery.MODEL);
				// if (models != null && models.length() > 0) {
				// item.setModel(models.optString(0));
				// }

				items.add(item);
			}

			return new Pair<List<Item>, Integer>(items, numFound);

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getDoctypeCount(Context context, String type) {
		try {
			String requestPath = K5Api.getDoctypeCountPath(context, type);
			HttpGet request = new HttpGet(requestPath);
			if (VersionUtils.Debuggable()) {
				Log.d(LOG_TAG, "query:" + requestPath);
			}
			request.setHeader("accept", "application/json");
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONObject json = (JSONObject) new JSONTokener(jsonString).nextValue();
			JSONObject responseJson = json.optJSONObject("response");
			if (responseJson == null) {
				return 0;
			}
			return responseJson.optInt("numFound");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public Map<String, Boolean> getUserRights(Context context) {
		String userName = K5Api.getUser(context);
		String password = K5Api.getPassword(context);
		return getUserRights(context, userName, password);
	}

	@Override
	public Map<String, Boolean> getUserRights(Context context, String name, String password) {
		final String basicAuth = "Basic " + Base64.encodeToString((name + ":" + password).getBytes(), Base64.NO_WRAP);
		Map<String, Boolean> rights = new HashMap<String, Boolean>();
		try {
			String requestPath = K5Api.getUserRightsPath(context);
			HttpGet request = new HttpGet(requestPath);
			request.setHeader("Authorization", basicAuth);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONObject json = (JSONObject) new JSONTokener(jsonString).nextValue();

			JSONArray names = json.names();
			for (int i = 0; i < names.length(); i++) {
				String key = names.getString(i);
				boolean value = json.getBoolean(key);
				rights.put(key, value);
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rights;
	}

	// public Bitmap getFullImage(Context context, String pid) {
	// String userName = K5Api.getUser(context);
	// String password = K5Api.getPassword(context);
	// final String basicAuth = "Basic "
	// + Base64.encodeToString((userName + ":" + password).getBytes(),
	// Base64.NO_WRAP);
	// try {
	// String requst = K5Api.getPreviewPath(context, pid);
	// HttpGet request = new HttpGet(requst);
	// HttpParams params = new BasicHttpParams();
	// HttpConnectionParams.setConnectionTimeout(params, 60000);
	// request.setParams(params);
	// request.setHeader("Authorization", basicAuth);
	// HttpResponse response = getClient().execute(request);
	//
	// Header[] h = response.getAllHeaders();
	// for (int i = 0; i < h.length; i++) {
	// Log.d(TAG, "header:" + h[i].getName() + ", " + h[i].getValue());
	// }
	//
	// byte[] image = EntityUtils.toByteArray(response.getEntity());
	//
	// return BitmapFactory.decodeByteArray(image, 0, image.length);
	//
	// } catch (IllegalStateException e) {
	// e.printStackTrace();
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	@Override
	public Metadata getModsMetadata(Context context, String pid) {
		String url = K5Api.getModsStreamPath(context, pid);
		Metadata metadata = ModsParser.getMetadata(url);
		if (metadata != null) {
			metadata.setPid(pid);
		}
		return metadata;
	}

	@Override
	public Set<AltoParser.TextBox> getTextBoxes(Context context, String pagePid, String searchQuery) {
		String url = K5Api.getAltoStreamPath(context, pagePid);
		String[] searchTokens = searchQuery.split(" ");
		return AltoParser.getTextBlocks(url, searchTokens);
	}

}
