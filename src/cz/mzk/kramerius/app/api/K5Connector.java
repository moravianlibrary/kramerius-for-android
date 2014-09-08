package cz.mzk.kramerius.app.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Base64;
import android.util.Pair;
import cz.mzk.kramerius.app.metadata.Author;
import cz.mzk.kramerius.app.metadata.Metadata;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.User;
import cz.mzk.kramerius.app.xml.ModsParser;

public class K5Connector {

	public static final String TAG = K5Connector.class.getName();

	public static K5Connector INSTANCE;

	private DefaultHttpClient mClient;

	private K5Connector() {

	}

	public static K5Connector getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new K5Connector();
		}
		return INSTANCE;
	}

	private DefaultHttpClient getClient() {
		if (mClient == null) {
			mClient = new DefaultHttpClient();
		}
		return mClient;
	}

	public void restart() {
		mClient = new DefaultHttpClient();
	}

	public List<Item> getNewest(Context context, boolean extended, int limit) {
		return getFeatured(context, K5Api.FEED_NEWEST, extended, limit);
	}

	public List<Item> getMostDesirable(Context context, boolean extended, int limit) {
		return getFeatured(context, K5Api.FEED_MOST_DESIRABLE, extended, limit);
	}

	// API doesn't support feed/selected. This is a temporary solution for
	// creating a list with selected documents.
	public List<Item> getSelected(Context context, boolean extended, int limit) {
		List<Item> list = new ArrayList<Item>();

		addItemToList(list, "uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22", "monograph", "Když slunéčko svítí");
		addItemToList(list, "uuid:e044d220-b0c1-4f15-95a0-0fd86747cc24", "monograph", "Dvanáct pohádek");
		addItemToList(list, "uuid:33b0c420-e25f-49cf-ab8d-c9471c927167", "monograph", "Kožuchy");
		addItemToList(list, "uuid:f1c7c08d-8f64-4b66-be28-5f209c2c7021", "periodical", "Rovnost");
		addItemToList(list, "uuid:c4d321f6-53b6-4a43-a80f-6a73138500f0", "monograph",
				"Král Myška a princ Junák : příhody statečných trpaslíků");

		addItemToList(list, "uuid:59e708b6-c462-4610-90c5-ac5ca030050a", "soundrecording", "Oh, Kay!. Clap yo' hands");

		addItemToList(list, "uuid:2fa33e93-7bb8-441c-aa5a-0f63bd565b93", "graphic",
				"Der steinere Saal bei Adamsthal [Kostelik]");

		addItemToList(list, "uuid:4873e8c7-5967-4003-8544-96f64ca55da7", "monograph", "Symbiotické zemědělství")
				.setPdf("http://kramerius.mzk.cz/search/img?pid=uuid:4873e8c7-5967-4003-8544-96f64ca55da7&stream=IMG_FULL&action=GETRAW");

		// addItemToList(list, "uuid:cf35b628-18ac-4bb6-9999-d55ff17a068b",
		// "monograph",
		// "Diagnostika předškoláka : správný vývoj řeči dítěte");

		// addItemToList(
		// list,
		// "uuid:3e6bd08f-db89-4916-8ad0-cf24985e1e83",
		// "monograph",
		// "I. souborná výstava akademického malíře Josefa Jambora: spořádaná u příležitosti 70. narozenin Mistra v místnostech osmileté střední školy v Tišnově v době od 26. října do 17.listopadu 1957");

		addItemToList(list, "uuid:bdc405b0-e5f9-11dc-bfb2-000d606f5dc6", "periodical", "Lidové noviny");

		addItemToList(list, "uuid:ae876087-435d-11dd-b505-00145e5790ea", "periodical", "Národní listy");
		addItemToList(list, "uuid:13f650ad-6447-11e0-8ad7-0050569d679d", "periodical", "Duha");
		addItemToList(list, "uuid:0d9d0820-0ba3-4a47-ba36-f271fdac4779", "map", "Europa");
		addItemToList(list, "uuid:06b2a42d-5ffa-46f7-ac38-19d3f8002ebb", "manuscript", "Snář");

		addItemToList(list, "uuid:de6a6d0e-70a0-4132-ac3a-82faf89d30bf", "map",
				"Beide Himmels-Halbkugeln in Stereographischer Polarprojection");
		addItemToList(list, "uuid:36e7c070-4bd3-4bc4-b991-3b66fe16f936", "manuscript",
				"Thajský rukopis na palmových listech");

		// addItemToList(list, "uuid:f1c7c08d-8f64-4b66-be28-5f209c2c7021",
		// "periodical", "Rovnost");
		addItemToList(list, "uuid:ba632b35-45d3-4a1e-8357-3eb866f9d00e", "soundrecording",
				"Le veau d'or Vous qui faites l'endormie");
		// addItemToList(list, "uuid:9f2fec5f-076b-424f-9b8d-d3a0aed4f0b1",
		// "soundrecording", "Nocturne Es-dur");
		// addItemToList(list, "uuid:86943124-5a4a-4679-a887-787ae0006b26",
		// "soundrecording", "Zločin a trest");
		// addItemToList(list, "uuid:206aac01-e915-4806-a828-324d3d8ee525",
		// "soundrecording",
		// "Pohřeb presidenta osvoboditele v ČS rozhlase 21.IX. 1937");

		addItemToList(list, "uuid:f40df848-b2d3-4334-a898-ed2c9aae6cb1", "periodical", "Tisk, noviny a novináři");
		addItemToList(
				list,
				"uuid:717063f8-a047-4622-9f54-bee2c5269452",
				"periodical",
				"Noviny : list vydavatelů časopisů a novinářů československých : věstník Ústř. svazu vydav. a nakl. časopisů a periodických spisů v Praze a Jednoty čsl. novinářů v Praze");

		// addItemToList(list, "uuid:d65f964d-60e2-411b-99a3-907783d419e4",
		// "monograph",
		// "Pazourek - nejstarší kulturní nerost, aneb, Kámen všech kamenů")
		// .setPdf("http://kramerius.mzk.cz/search/img?pid=uuid:d65f964d-60e2-411b-99a3-907783d419e4&stream=IMG_FULL&action=GETRAW");
		//
		// addItemToList(list, "uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8",
		// "monograph",
		// "Lidský kapitál a investice do vzdělání (16. ročník, 2013)")
		// .setPdf("http://kramerius.mzk.cz/search/img?pid=uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8&stream=IMG_FULL&action=GETRAW");

		addItemToList(list, "uuid:6fa2a1ab-3cce-4f9b-a6ca-4cfd04cf60a8", "monograph", "Philosophy of Balance")
				.setPdf("http://kramerius.mzk.cz/search/img?pid=uuid:6fa2a1ab-3cce-4f9b-a6ca-4cfd04cf60a8&stream=IMG_FULL&action=GETRAW");

		// addItemToList(list, "uuid:bdc405b0-e5f9-11dc-bfb2-000d606f5dc6",
		// "periodical",
		// "Lidové noviny");

		if (limit > -1 && list.size() > limit) {
			list = list.subList(0, limit);
		}

		if (extended) {
			for (Item o : list) {
				Metadata metadata = getModsMetadata(context, o.getRootPid());
				if (metadata != null && !metadata.getAuthors().isEmpty()) {
					Author author = metadata.getAuthors().get(0);
					if (author != null && author.getName() != null) {
						o.setAuthor(author.getName());
					}
				}
			}
		}

		return list;

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

	private List<Item> getFeatured(Context context, int feed, boolean extended, int limit) {
		List<Item> list = new ArrayList<Item>();
		try {
			String requst = K5Api.getFeedPath(context, feed);
			HttpGet request = new HttpGet(requst);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONObject json = (JSONObject) new JSONTokener(jsonString).nextValue();
			JSONArray data = json.getJSONArray("data");
			for (int i = 0; i < data.length(); i++) {
				Item item = new Item();
				JSONObject jsonItem = data.getJSONObject(i);
				item.setPid(jsonItem.optString("pid"));
				item.setModel(jsonItem.optString("model"));
				item.setIssn(jsonItem.optString("issn"));
				item.setDate(jsonItem.optString("date"));
				item.setTitle(jsonItem.optString("title"));
				item.setRootTitle(jsonItem.optString("root_title"));
				item.setRootPid(jsonItem.optString("root_pid"));				
				item.setPolicyPrivate("private".equals(jsonItem.optString("policy")));
				list.add(item);
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

		if (limit > -1 && list.size() > limit) {
			list = list.subList(0, limit);
		}
		if (extended) {
			for (Item o : list) {
				Metadata metadata = getModsMetadata(context, o.getRootPid());
				if (metadata != null && !metadata.getAuthors().isEmpty()) {
					Author author = metadata.getAuthors().get(0);
					if (author != null && author.getName() != null) {
						o.setAuthor(author.getName());
					}
				}
			}
		}
		return list;
	}
	
	
	public List<Pair<String, String>> getHierarychy(Context context, String pid) {
		try {
			String requst = K5Api.getItemPath(context, pid);
			HttpGet request = new HttpGet(requst);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONObject jsonItem = (JSONObject) new JSONTokener(jsonString).nextValue();
			List<Pair<String, String>> hierarchy = new ArrayList<Pair<String,String>>();
			
			JSONArray a = jsonItem.optJSONArray("context");
			if(a == null) {
				hierarchy.add(new Pair<String, String>(jsonItem.optString("pid"), jsonItem.optString("model")));				
			} else {
				if(a.length() == 1 && a.get(0) instanceof JSONArray) {
					a = (JSONArray) a.get(0);
				}
				for(int i = 0; i < a.length(); i++) {
					JSONObject c = a.getJSONObject(i);
					if(c != null) {
						hierarchy.add(new Pair<String, String>(c.optString("pid"), c.optString("model")));
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
		}
		return null;
	}	
	
		
	

	public Item getItem(Context context, String pid) {
		try {
			String requst = K5Api.getItemPath(context, pid);
			HttpGet request = new HttpGet(requst);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONObject jsonItem = (JSONObject) new JSONTokener(jsonString).nextValue();
			Item item = new Item();
			item.setPid(jsonItem.optString("pid"));
			item.setModel(jsonItem.optString("model"));
			item.setIssn(jsonItem.optString("issn"));
			item.setDate(jsonItem.optString("date"));
			item.setTitle(jsonItem.optString("title"));
			item.setRootTitle(jsonItem.optString("root_title"));
			JSONObject pdf = jsonItem.optJSONObject("pdf");
			if (pdf != null) {
				String pdfUrl = pdf.optString("url");
				if (pdfUrl != null) {
					item.setPdf(pdfUrl);
				}
			}
			item.setRootPid(jsonItem.optString("root_pid"));
			item.setPolicyPrivate("private".equals(jsonItem.optString("policy")));

			JSONObject details = jsonItem.optJSONObject("details");
			if (details != null) {
				item.setYear(details.optString("year"));
				item.setVolumeNumber(details.optString("volumeNumber"));
				item.setIssueNumber(details.optString("issueNumber"));
				item.setPeriodicalItemDate(details.optString("date"));
				item.setPartNumber(details.optString("partNumber"));
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
		}
		return null;
	}

	public List<Item> getChildren(Context context, String pid) {
		List<Item> list = new ArrayList<Item>();
		try {
			String requst = K5Api.getChildrenPath(context, pid);
			HttpGet request = new HttpGet(requst);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONArray data = (JSONArray) new JSONTokener(jsonString).nextValue();
			for (int i = 0; i < data.length(); i++) {
				Item item = new Item();
				JSONObject jsonItem = data.getJSONObject(i);
				item.setPid(jsonItem.optString("pid"));
				item.setModel(jsonItem.optString("model"));
				item.setIssn(jsonItem.optString("issn"));
				item.setDate(jsonItem.optString("date"));
				item.setTitle(jsonItem.optString("title"));
				item.setRootTitle(jsonItem.optString("root_title"));
				item.setRootPid(jsonItem.optString("root_pid"));
				item.setPolicyPrivate("private".equals(jsonItem.optString("policy")));
				JSONObject details = jsonItem.optJSONObject("details");
				if (details != null) {
					item.setYear(details.optString("year"));
					item.setVolumeNumber(details.optString("volumeNumber"));
					item.setIssueNumber(details.optString("issueNumber"));
					item.setPeriodicalItemDate(details.optString("date"));
					item.setPartNumber(details.optString("partNumber"));
				}
				list.add(item);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Item> getVirtualCollctions(Context context) {
		List<Item> list = new ArrayList<Item>();
		try {
			String requst = K5Api.getVirtualCollectionsPath(context);
			HttpGet request = new HttpGet(requst);
			HttpResponse response = getClient().execute(request);
			String jsonString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			JSONArray data = (JSONArray) new JSONTokener(jsonString).nextValue();
			for (int i = 0; i < data.length(); i++) {
				Item item = new Item();
				JSONObject jsonItem = data.getJSONObject(i);
				item.setPid(jsonItem.optString("pid"));
				item.setTitle(jsonItem.getJSONObject("descs").optString("cs"));
				list.add(item);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	public User getUserInfo(Context context) {
		String userName = K5Api.getUser(context);
		String password = K5Api.getPassword(context);
		return getUserInfo(context, userName, password);
	}

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
		}
		return null;
	}

	public Map<String, Boolean> getUserRights(Context context) {
		String userName = K5Api.getUser(context);
		String password = K5Api.getPassword(context);
		return getUserRights(context, userName, password);
	}

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

	public Metadata getModsMetadata(Context context, String pid) {
		String url = K5Api.getModsStreamPath(context, pid);
		Metadata metadata = ModsParser.getMetadata(url);
		if(metadata != null) {
			metadata.setPid(pid);
		}
		return metadata;
	}

}
