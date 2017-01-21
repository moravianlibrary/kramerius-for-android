package cz.mzk.kramerius.app.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.card.VirtualCollectionCard;
import cz.mzk.kramerius.app.data.KrameriusContract;
import cz.mzk.kramerius.app.model.Hit;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.search.Query;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.LangUtils;
import cz.mzk.kramerius.app.util.Logger;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;

public class SearchFiltersFragment extends BaseFragment {


    private static final String LOG_TAG = SearchFiltersFragment.class.getSimpleName();

    private FilterListener mCallback;

    private LayoutInflater mInflater;
    private ViewGroup mAccessibilityWrapper;
    private ViewGroup mAuthorsWrapper;
    private ViewGroup mKeywordsWrapper;
    private ViewGroup mDoctypesWrapper;
    private ViewGroup mCollectionsWrapper;
    private ViewGroup mLanguagesWrapper;


    private ViewGroup mFiltersWrapper;


    private Query mQuery;


    private Map<String, String> mLanguageMap;
    private Map<String, String> mCollectionsMap;

    private List<Hit> mPendingCollectionFilter;


    private Context mContext;


    public void setQuery(Query query) {
        mQuery = query;
    }

    public SearchFiltersFragment(Context context) {
        mContext = context;
        initLanguageMap(context);
    }


    public interface FilterListener {
        void onFilterChanged();
    }


    public void setCallback(FilterListener callback) {
        mCallback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_search_filters, container, false);
        mAccessibilityWrapper = (ViewGroup) view.findViewById(R.id.accessibility_wrapper);
        mAuthorsWrapper = (ViewGroup) view.findViewById(R.id.authors_wrapper);
        mKeywordsWrapper = (ViewGroup) view.findViewById(R.id.keywords_wrapper);
        mDoctypesWrapper = (ViewGroup) view.findViewById(R.id.doctypes_wrapper);
        mCollectionsWrapper = (ViewGroup) view.findViewById(R.id.collections_wrapper);
        mLanguagesWrapper = (ViewGroup) view.findViewById(R.id.languages_wrapper);
        mFiltersWrapper = (ViewGroup) view.findViewById(R.id.filters_wrapper);

        refresh();

        return view;
    }



    private void refresh() {
        mAccessibilityWrapper.removeAllViews();
        mAuthorsWrapper.removeAllViews();
        mKeywordsWrapper.removeAllViews();
        mDoctypesWrapper.removeAllViews();
        mCollectionsWrapper.removeAllViews();
        mLanguagesWrapper.removeAllViews();
        mFiltersWrapper.removeAllViews();
        initFilters();
        new GetFiltersTask(getActivity().getApplicationContext(), SearchQuery.POLICY).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new GetFiltersTask(getActivity().getApplicationContext(), SearchQuery.MODEL).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new GetFiltersTask(getActivity().getApplicationContext(), SearchQuery.AUTHOR_FACET).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new GetFiltersTask(getActivity().getApplicationContext(), SearchQuery.COLLECTION).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new GetFiltersTask(getActivity().getApplicationContext(), SearchQuery.KEYWORDS).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new GetFiltersTask(getActivity().getApplicationContext(), SearchQuery.LANGUAGE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    class GetFiltersTask extends AsyncTask<Void, Void, List<Hit>> {

        private Context tContext;
        private String tFacet;

        public GetFiltersTask(Context context, String facet) {
            tContext = context;
            tFacet = facet;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<Hit> doInBackground(Void... params) {
            return K5ConnectorFactory.getConnector().getSearchFacetResults(tContext, mQuery, tFacet);
        }

        @Override
        protected void onPostExecute(List<Hit> result) {
            stopLoaderAnimation();
            if (getActivity() == null) {
                return;
            }
            if(SearchQuery.POLICY.equals(tFacet)) {
                handleAccessibility(result);
            } else if(SearchQuery.AUTHOR_FACET.equals(tFacet)) {
                handleAuthors(result);
            } else if(SearchQuery.KEYWORDS.equals(tFacet)) {
                handleKeywords(result);
            } else if(SearchQuery.MODEL.equals(tFacet)) {
                handleDoctypes(result);
            } else if(SearchQuery.COLLECTION.equals(tFacet)) {
                handleCollections(result);
            } else if(SearchQuery.LANGUAGE.equals(tFacet)) {
                handleLanguages(result);
            }
//            //if (mFirst) {
//            //	stopLoaderAnimation();
//            //}
//            mLoading = false;
//            if (result == null || result.first == null) {
//                if (mFirst) {
//
//                    showWarningMessage(R.string.warn_data_loading_failed, R.string.gen_again,
//                            new onWarningButtonClickedListener() {
//
//                                @Override
//                                public void onWarningButtonClicked() {
//                                    new SearchResultsFragment.GetResultTask(getActivity().getApplicationContext()).execute(mQuery);
//                                }
//                            });
//                }
//                return;
//            }
//            if (result.second == 0) {
//                showWarningMessage(getString(R.string.warn_empty_result), null, null);
//                return;
//            }
//            mNumFound = result.second;
//            populateGrid(result.first);
//            mFirst = false;
//            int shownNum = mStart + mRows;
//            if (shownNum > mNumFound) {
//                shownNum = mNumFound;
//            }
//
//            getSupportActionBar().setSubtitle(shownNum + " z " + mNumFound);
        }
    }



    private void handleAccessibility(List<Hit> list) {
        if(list == null) {
            return;
        }
        int pu = 0;
        int pr = 0;
        for(Hit h: list) {
            if("public".equals(h.value)) {
                pu = h.count;
            } else if("private".equals(h.value)) {
                pr = h.count;
            }
        }
        int all = pu + pr;
        addFilter(mAccessibilityWrapper, "Pouze veřejné", SearchQuery.POLICY, "public", pu);
        addFilter(mAccessibilityWrapper, "Pouze neveřejné", SearchQuery.POLICY, "private", pr);
        addFilter(mAccessibilityWrapper, "Vše", SearchQuery.POLICY, "all", all);

    }



    private void handleAuthors(List<Hit> list) {
        if(list == null) {
            return;
        }
        for(Hit h : list) {
            addFilter(mAuthorsWrapper, h.value,SearchQuery.AUTHOR_FACET, h.value, h.count);
        }
    }

    private void handleKeywords(List<Hit> list) {
        if(list == null) {
            return;
        }
        for(Hit h : list) {
            addFilter(mKeywordsWrapper, h.value,SearchQuery.KEYWORDS, h.value, h.count);
        }
    }

    private void handleCollections(List<Hit> list) {
        if(list == null) {
            return;
        }
        if(mCollectionsMap == null) {
            mPendingCollectionFilter = list;
            new GetVirtualCollectionsTask(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            for (Hit h : list) {
                addFilter(mCollectionsWrapper, mCollectionsMap.get(h.value), SearchQuery.COLLECTION, h.value, h.count);
            }
        }
    }

    private void handleLanguages(List<Hit> list) {
        if(list == null) {
            return;
        }
        for(Hit h : list) {
            addFilter(mLanguagesWrapper, getLanguageName(h.value),SearchQuery.LANGUAGE, h.value, h.count);

        }
    }

    private void handleDoctypes(List<Hit> list) {
        if(list == null) {
            return;
        }
        for(Hit h : list) {
            addFilter(mDoctypesWrapper, getModelName(h.value), SearchQuery.MODEL, h.value, h.count);
        }
    }

    private void addFilter(ViewGroup container, String title, String code, String value, int count) {
        View v = mInflater.inflate(R.layout.view_search_filter_item, container, false);
        TextView countView = (TextView) v.findViewById(R.id.count);
        countView.setText(String.valueOf(count));
        TextView titleView = (TextView) v.findViewById(R.id.title);
        titleView.setText(title);
        ((TextView) v.findViewById(R.id.title)).setText(String.valueOf(title));
        if(mQuery.isActive(code, value)) {
            titleView.setTextColor(0xff0073b2);
            countView.setTextColor(0xff0073b2);
        }

        v.setTag(new Tag(code, value));
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFilterSelected(v);
            }
        });
        container.addView(v);
    }


    private void onFilterSelected(View v) {
        Tag tag = (Tag) v.getTag();
        Logger.debug(LOG_TAG, "onFilterSelected, " + tag.code + ":" + tag.value);
        if(mQuery.change(tag.code, tag.value) && mCallback != null) {
            mCallback.onFilterChanged();
            refresh();
        }
    }



    public class Tag {
        public String code;
        public String value;

        public Tag(String code, String value) {
            this.code = code;
            this.value = value;
        }
    }



    private String getModelName(String value) {
        String a = "models_" + value;
        String packageName = getActivity().getPackageName();
        int resId = getResources().getIdentifier(a, "string", packageName);
        if(resId == 0) {
            Logger.debug(LOG_TAG, "getStringResourceByName (" + value + ")");
            return "";
        }
        return getString(resId);
    }





    private void initFilters() {
        if(mQuery.hasQuery()) {
            addUsedFilter(mQuery.getQuery(), "q", mQuery.getQuery(), R.drawable.ic_search_grey);
        }
        for(String doctype : mQuery.getDoctypes()) {
            addUsedFilter(getModelName(doctype), SearchQuery.MODEL, doctype, R.drawable.ic_attach_grey);
        }
        for(String author : mQuery.getAuthors()) {
            addUsedFilter(author, SearchQuery.AUTHOR_FACET, author, R.drawable.ic_user_grey);
        }
        for(String collection : mQuery.getCollections()) {
            addUsedFilter(collection, SearchQuery.COLLECTION, collection, R.drawable.ic_group_grey);
        }
        for(String keyword : mQuery.getKeywords()) {
            addUsedFilter(keyword, SearchQuery.KEYWORDS, keyword, R.drawable.ic_label_grey);
        }
        for(String language : mQuery.getLanguages()) {
            addUsedFilter(getLanguageName(language), SearchQuery.LANGUAGE, language, R.drawable.ic_star_grey);
        }
    }


    private void addUsedFilter(String title, String code, String value, int iconRes) {
        View v = mInflater.inflate(R.layout.view_search_used_filter_item, mFiltersWrapper, false);
        ImageView iconView = (ImageView) v.findViewById(R.id.icon);
        iconView.setImageResource(iconRes);
        TextView titleView = (TextView) v.findViewById(R.id.title);
        titleView.setText(title);
        ((TextView) v.findViewById(R.id.title)).setText(String.valueOf(title));
        v.setTag(new Tag(code, value));
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFilterSelected(v);
            }
        });
        mFiltersWrapper.addView(v);
    }



    private void initLanguageMap(Context context) {
        mLanguageMap = new HashMap<>();
        Cursor c = context.getContentResolver().query(KrameriusContract.LanguageEntry.CONTENT_URI,
                new String[]{KrameriusContract.LanguageEntry.COLUMN_CODE, KrameriusContract.LanguageEntry.COLUMN_NAME},
                KrameriusContract.LanguageEntry.COLUMN_LANG + "=?",
                new String[]{LangUtils.getLanguage()}, null);
        while (c.moveToNext()) {
            mLanguageMap.put(c.getString(0), c.getString(1));
        }
        c.close();
    }


    private String getLanguageName(String code) {
        if(mLanguageMap.containsKey(code)) {
            return mLanguageMap.get(code);
        } else {
            return code;
        }
    }


    private void fillCollectionMap(List<Item> list) {
        if(list == null) {
            return;
        }
        mCollectionsMap = new HashMap<>();
        for(Item item : list) {
            mCollectionsMap.put(item.getPid(), item.getTitle());
        }
    }













    class GetVirtualCollectionsTask extends AsyncTask<Void, Void, List<Item>> {

        private Context tContext;

        public GetVirtualCollectionsTask(Context context) {
            tContext = context;
        }

        @Override
        protected void onPreExecute() {
            startLoaderAnimation();
        }

        @Override
        protected List<Item> doInBackground(Void... params) {
            return K5ConnectorFactory.getConnector().getVirtualCollections(tContext);
        }

        @Override
        protected void onPostExecute(List<Item> result) {
            fillCollectionMap(result);
            handleCollections(mPendingCollectionFilter);
        }

    }







}
