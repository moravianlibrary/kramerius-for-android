package cz.mzk.kramerius.app.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.util.Pair;
import cz.mzk.kramerius.app.metadata.Metadata;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.User;
import cz.mzk.kramerius.app.search.TextBox;

public interface K5Connector {

	public abstract void restart();

	public abstract List<Item> getFeatured(Context context, int feed, int limit, String policy, String model);

	public abstract List<Pair<String, String>> getHierarychy(Context context, String pid);

	public abstract Item getItem(Context context, String pid, String domain);

	public abstract Item getItem(Context context, String pid);

	public abstract List<Item> getChildren(Context context, String pid, String modelFilter);

	public abstract List<Item> getChildren(Context context, String pid);

	public abstract List<Item> getVirtualCollections(Context context);

	public abstract User getUserInfo(Context context);

	public abstract User getUserInfo(Context context, String name, String password);

	public abstract Pair<List<Item>, Integer> getSearchResult(Context context, String query, int start, int rows);

	public abstract int getDoctypeCount(Context context, String type);

	public abstract Map<String, Boolean> getUserRights(Context context);

	public abstract Map<String, Boolean> getUserRights(Context context, String name, String password);

	public abstract Metadata getModsMetadata(Context context, String pid);

	public abstract Set<TextBox> getTextBoxes(Context context, String pagePid, String searchQuery);

}