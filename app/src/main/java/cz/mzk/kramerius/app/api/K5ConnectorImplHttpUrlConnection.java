package cz.mzk.kramerius.app.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.mzk.kramerius.app.KrameriusApplication;
import cz.mzk.kramerius.app.data.KrameriusContract;
import cz.mzk.kramerius.app.metadata.Metadata;
import cz.mzk.kramerius.app.model.Domain;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.User;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.search.TextBox;
import cz.mzk.kramerius.app.util.LangUtils;
import cz.mzk.kramerius.app.util.Logger;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.xml.AltoParser;
import cz.mzk.kramerius.app.xml.ModsParser;

public class K5ConnectorImplHttpUrlConnection implements K5Connector {

    private static final String LOG_TAG = K5ConnectorImplHttpUrlConnection.class.getName();

    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int DATA_READ_TIMEOUT = 10000;
    public static final int MAX_REDIRECTIONS = 5;

    // private K5Connector legacyConnector = new K5ConnectorImplDefaultHttpClient();

    @Override
    public void restart() {
        // nothing here
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

    private String downloadText(String requestUrl, Map<String, String> headers) {
        return downloadText(requestUrl, headers, MAX_REDIRECTIONS);
    }

    private String downloadText(String requestUrl) {
        return downloadText(requestUrl, null, MAX_REDIRECTIONS);
    }

    private String downloadText(String requestUrl, Map<String, String> headers, int remainingRedirections) {
        if (remainingRedirections == 0) {
            Log.e(LOG_TAG, "too many redirections for: " + requestUrl);
            return null;
        }
        HttpURLConnection urlConnection = null;
        try {
            // request
            URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(DATA_READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            if (headers != null) {
                for (String header : headers.keySet()) {
                    urlConnection.setRequestProperty(header, headers.get(header));
                }
            }
            // response
            int responseCode = urlConnection.getResponseCode();
            String location = urlConnection.getHeaderField("Location");
            switch (responseCode) {
                case 200:
                    return stringFromUrlConnection(urlConnection);
                case 300:
                case 301:
                case 302:
                case 303:
                case 305:
                case 307:
                    if (location == null || location.isEmpty()) {
                        Log.e(LOG_TAG, "redirection with missing 'Location' header: \"" + requestUrl + '\"');
                        return null;
                    }
                    urlConnection.disconnect();
                    return downloadText(location, headers, remainingRedirections - 1);
                default:
                    Log.e(LOG_TAG, "Unexpected response code " + responseCode + ": \"" + requestUrl + '\"');
                    return null;
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "invalid url: \"" + requestUrl + '\"', e);
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "i/o error reading: \"" + requestUrl + '\"', e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static String stringFromUrlConnection(HttpURLConnection urlConnection) throws IOException {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] buffer = new byte[8 * 1024];
            out = new ByteArrayOutputStream();
            int readBytes = 0;
            while ((readBytes = in.read(buffer)) != -1) {
                out.write(buffer, 0, readBytes);
            }
            return out.toString();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    public List<Item> getFeatured(Context context, int feed, int limit, String policy, String model) {
        // return legacyConnector.getFeatured(context, feed, limit, policy, model);
        try {
            List<Item> list = new ArrayList<Item>();
            String url = K5Api.getFeedPath(context, feed, limit, policy, model);
            Logger.debug(LOG_TAG, "request: " + url + ", " + feed);
            String jsonString = getResponse(context, url, true);
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
            String url = K5Api.getItemPath(context, pid);
            String jsonString = getResponse(context, url, true);
            JSONObject jsonItem = (JSONObject) new JSONTokener(jsonString).nextValue();
            List<Pair<String, String>> hierarchy = new ArrayList<Pair<String, String>>();
            JSONArray a = jsonItem.optJSONArray(K5Constants.CONTEXT);
            if (a == null) {
                hierarchy.add(new Pair<String, String>(jsonItem.optString(K5Constants.PID), jsonItem
                        .optString(K5Constants.MODEL)));
            } else {
                if (a.length() > 0 && a.get(0) instanceof JSONArray) {
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
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Item getItem(Context context, String pid, String domain) {
        // return legacyConnector.getItem(context, pid, domain);
        try {
            String url = K5Api.getItemPath(context, pid);
            Logger.debug(LOG_TAG, "getItem - Request:" + url);
            url.replace(K5Api.getDomain(context), domain);
            String jsonString = getResponse(context, url, true);
            Logger.debug(LOG_TAG, "getItem - Response:" + jsonString);
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
        // return legacyConnector.getChildren(context, pid, modelFilter);
        try {
            String url = K5Api.getChildrenPath(context, pid);
            String jsonString = getResponse(context, url);
            JSONArray data = (JSONArray) new JSONTokener(jsonString).nextValue();
            List<Item> list = new ArrayList<Item>();
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
            String url = K5Api.getVirtualCollectionsPath(context);
            String jsonString = getResponse(context, url, true);
            JSONArray data = (JSONArray) new JSONTokener(jsonString).nextValue();
            List<Item> list = new ArrayList<Item>();
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
        // return legacyConnector.getUserInfo(context, name, password);
        try {
            String url = K5Api.getUserPath(context);
            final String basicAuth = "Basic "
                    + Base64.encodeToString((name + ":" + password).getBytes(), Base64.NO_WRAP);
            Map<String, String> headers = new HashMap<String, String>() {
                {
                    put("Authorization", basicAuth);
                }
            };

            String jsonString = downloadText(url, headers);
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
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Pair<List<Item>, Integer> getSearchResult(Context context, String query, int start, int rows) {
        // return legacyConnector.getSearchResult(context, query, start, rows);
        try {
            String url = K5Api.getSearchPath(context, query, start, rows);
            Logger.debug(LOG_TAG, "query - search: " + url);
            Map<String, String> headers = new HashMap<String, String>() {
                {
                    put("accept", "application/json");
                }
            };

            String jsonString = downloadText(url, headers);
            Logger.debug(LOG_TAG, "search result:" + jsonString);
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
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getDoctypeCount(Context context, String type) {
        // return legacyConnector.getDoctypeCount(context, type);
        try {
            String url = K5Api.getDoctypeCountPath(context, type);
            Logger.debug(LOG_TAG, "query:" + url);
            Map<String, String> headers = new HashMap<String, String>() {
                {
                    put("accept", "application/json");
                }
            };

            String jsonString = downloadText(url, headers);
            JSONObject json = (JSONObject) new JSONTokener(jsonString).nextValue();
            JSONObject responseJson = json.optJSONObject("response");
            if (responseJson == null) {
                return 0;
            }
            return responseJson.optInt("numFound");
        } catch (IllegalStateException e) {
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
        // return legacyConnector.getUserRights(context, name, password);
        Map<String, Boolean> rights = new HashMap<String, Boolean>();
        try {
            String url = K5Api.getUserRightsPath(context);
            final String basicAuth = "Basic "
                    + Base64.encodeToString((name + ":" + password).getBytes(), Base64.NO_WRAP);
            Map<String, String> headers = new HashMap<String, String>() {
                {
                    put("Authorization", basicAuth);
                }
            };

            String jsonString = downloadText(url, headers);
            JSONObject json = (JSONObject) new JSONTokener(jsonString).nextValue();

            JSONArray names = json.names();

            for (int i = 0; i < names.length(); i++) {
                String key = names.getString(i);
                boolean value = json.getBoolean(key);
                rights.put(key, value);
            }

        } catch (IllegalStateException e) {
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
    public Set<TextBox> getTextBoxes(Context context, String pagePid, String searchQuery) {
        String url = K5Api.getAltoStreamPath(context, pagePid);
        String[] searchTokens = searchQuery.split(" ");
        return AltoParser.getTextBlocks(url, searchTokens);
    }



    @Override
    public boolean reloadTestLibraries(Context context) {
        context.getContentResolver().delete(KrameriusContract.LibraryEntry.CONTENT_URI, KrameriusContract.LibraryEntry.COLUMN_LOCKED + "=?", new String[]{String.valueOf(1)});
        try {
            String url = "https://registr-krameriu.herokuapp.com/libraries.json?android=1";
            String jsonString = getResponse(context, url, false);
            Logger.debug(LOG_TAG, "getLibraries - Response:" + jsonString);
            JSONArray jsonArray = (JSONArray) new JSONTokener(jsonString).nextValue();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonItem = jsonArray.getJSONObject(i);
                String name = jsonItem.optString("name");
                String code = jsonItem.optString("code");
                String libraryUrl = jsonItem.optString("url");
                String protocol = libraryUrl.substring(0, libraryUrl.indexOf("://"));
                String domain = libraryUrl.substring(libraryUrl.indexOf("://") + 3, libraryUrl.length());
                Logger.debug(LOG_TAG, "getLibraries - name:" + name);
                Logger.debug(LOG_TAG, "getLibraries - code:" + code);
                Logger.debug(LOG_TAG, "getLibraries - libraryUrl:" + libraryUrl);
                Logger.debug(LOG_TAG, "getLibraries - protocol:" + protocol);
                Logger.debug(LOG_TAG, "getLibraries - domain:" + domain);

                ContentValues cv = new ContentValues();
                cv.put(KrameriusContract.LibraryEntry.COLUMN_NAME, name);
                cv.put(KrameriusContract.LibraryEntry.COLUMN_PROTOCOL, protocol);
                cv.put(KrameriusContract.LibraryEntry.COLUMN_DOMAIN, domain);
                cv.put(KrameriusContract.LibraryEntry.COLUMN_CODE, code);
                cv.put(KrameriusContract.LibraryEntry.COLUMN_LOCKED, 1);
                context.getContentResolver().insert(KrameriusContract.LibraryEntry.CONTENT_URI, cv);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }



    private String getResponse(Context context, String url) {
        return getResponse(context, url, false);
    }

    private String getResponse(Context context, String url, boolean tryCache) {
        Logger.debug(LOG_TAG, "REQUEST: " + url);
        if(!tryCache) {
            return downloadText(url);
        }
        String response = cacheLookup(context, url);
        if(response == null) {
            response = downloadText(url);
            addToCache(context, url, response);
        }
        return response;
    }


    private String cacheLookup(Context context, String url) {
        Logger.debug(LOG_TAG, "Cache lookup: " + url);
        String response = null;
        Cursor c = context.getContentResolver().query(KrameriusContract.CacheEntry.CONTENT_URI,
                new String[]{KrameriusContract.CacheEntry.COLUMN_RESPONSE},
                KrameriusContract.CacheEntry.COLUMN_URL + "=?",
                new String[]{url}, null);
        if(c.moveToFirst()) {
            response = c.getString(0);
        }
        c.close();
        return response;
    }

    private void addToCache(Context context, String url, String response) {
        Logger.debug(LOG_TAG, "Adding to cache: " + url + "\n" + response);
        if(url == null || response == null) {
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put(KrameriusContract.CacheEntry.COLUMN_URL, url);
        cv.put(KrameriusContract.CacheEntry.COLUMN_RESPONSE, response);
        context.getContentResolver().insert(KrameriusContract.CacheEntry.CONTENT_URI, cv);
    }

}
