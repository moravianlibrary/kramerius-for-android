package cz.mzk.kramerius.app.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.search.Query;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.util.Logger;

public class SearchFiltersFragment extends BaseFragment {

//    private static final String EXTRA_QUERY = "extra_query";

    private static final String LOG_TAG = SearchFiltersFragment.class.getSimpleName();

    //    private String mSearchQuery;
    private FilterListener mCallback;

    private boolean mLoading;

    private LayoutInflater mInflater;
    private ViewGroup mAccessibilityWrapper;

    private Query mQuery;


    public SearchFiltersFragment(Query query) {
        mQuery = query;
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
//        mSearchQuery = getArguments().getString(EXTRA_QUERY, "*:*");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_search_filters, container, false);
        mAccessibilityWrapper = (ViewGroup) view.findViewById(R.id.accessibility_wrapper);
        refresh();

        return view;
    }



    private void refresh() {
        mAccessibilityWrapper.removeAllViews();
        new GetFiltersTask(getActivity().getApplicationContext(), SearchQuery.POLICY).execute();
    }


    class GetFiltersTask extends AsyncTask<Void, Void, Map<String, Integer>> {

        private Context tContext;
        private String tFacet;

        public GetFiltersTask(Context context, String facet) {
            tContext = context;
            tFacet = facet;
        }

        @Override
        protected void onPreExecute() {
            startLoaderAnimation();
        }

        @Override
        protected Map<String, Integer> doInBackground(Void... params) {
            return K5ConnectorFactory.getConnector().getSearchFacetResults(tContext, mQuery, tFacet);
        }

        @Override
        protected void onPostExecute(Map<String, Integer> result) {
            stopLoaderAnimation();
            if (getActivity() == null) {
                return;
            }
            if(SearchQuery.POLICY.equals(tFacet)) {
                handleAccessibility(result);
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



    private void handleAccessibility(Map<String, Integer> map) {
        if(map == null) {
            return;
        }

        int pu = 0;
        int pr = 0;
        if(map.containsKey("public")) {
            pu = map.get("public");
        }
        if(map.containsKey("private")) {
            pr = map.get("private");
        }
        int all = pu + pr;

        addFilter(mAccessibilityWrapper, "Pouze veřejné", SearchQuery.POLICY, "public", pu);
        addFilter(mAccessibilityWrapper, "Pouze neveřejné", SearchQuery.POLICY, "private", pr);
        addFilter(mAccessibilityWrapper, "Vše", SearchQuery.POLICY, "all", all);

    }


    private void addFilter(ViewGroup container, String title, String code, String value, int count) {
        View v = mInflater.inflate(R.layout.view_search_filter_item, container, false);
        TextView countView = (TextView) v.findViewById(R.id.count);
        countView.setText(String.valueOf(count));
        TextView titleView = (TextView) v.findViewById(R.id.title);
        titleView.setText(title);
        ((TextView) v.findViewById(R.id.title)).setText(String.valueOf(title));
        if(mQuery.isActive(code, value)) {
            titleView.setTextColor(Color.BLUE);
            countView.setTextColor(Color.BLUE);
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











}
