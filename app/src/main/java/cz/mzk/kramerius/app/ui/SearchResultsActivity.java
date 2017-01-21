package cz.mzk.kramerius.app.ui;

import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.SearchSuggestionAdapter;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.data.KrameriusContract;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.search.Query;
import cz.mzk.kramerius.app.util.Logger;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.PrefUtils;
import cz.mzk.kramerius.app.view.MaterialSearchView;

public class SearchResultsActivity extends BaseActivity implements View.OnClickListener, OnItemSelectedListener, SearchFiltersFragment.FilterListener {

    public static final String EXTRA_QUERY = "extra_query";
    public static final String EXTRA_COLLECTION = "extra_collection";

    private static final String LOG_TAG = SearchResultsActivity.class.getSimpleName();


    private SearchResultsFragment mSearchResultsFragment;
    private SearchFiltersFragment mSearchFiltersFragment;

    private boolean mFiltersMode;
    private TextView mSwitchBtn;
    private Query mQuery;

    private MaterialSearchView mSearchView;
    private int mSuggestionsIndex = 0;
    private SearchSuggestionAdapter mSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.search_result_title);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String query = getIntent().getStringExtra(EXTRA_QUERY);
        String collection = getIntent().getStringExtra(EXTRA_COLLECTION);
//        if (query == null) {
//            finish();
//            return;
//        }

        mQuery = new Query(query, collection, PrefUtils.isPublicOnly(this));

        if(savedInstanceState == null) {
            mSearchResultsFragment = new SearchResultsFragment();
            mSearchResultsFragment.setQuery(mQuery);
            mSearchResultsFragment.setOnItemSelectedListener(this);

            mSearchFiltersFragment = new SearchFiltersFragment(this);
            mSearchFiltersFragment.setQuery(mQuery);
            mSearchFiltersFragment.setCallback(this);


            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (getDevice() == TABLET && isLandscape()) {
                ft.replace(R.id.search_result_container, mSearchResultsFragment, "tag_search_results");
                ft.replace(R.id.search_filters_container, mSearchFiltersFragment, "tag_search_filters").commit();
            } else {
                mSwitchBtn = (TextView) findViewById(R.id.switch_btn);
                mSwitchBtn.setOnClickListener(this);
                ft.add(R.id.search_result_container, mSearchResultsFragment, "tag_search_results");
                ft.add(R.id.search_result_container, mSearchFiltersFragment, "tag_search_filters").commit();
            }
        } else {
            mSearchResultsFragment = (SearchResultsFragment) getFragmentManager().findFragmentByTag("tag_search_results");
            mSearchResultsFragment.setQuery(mQuery);
            mSearchResultsFragment.setOnItemSelectedListener(this);

            mSearchFiltersFragment = (SearchFiltersFragment) getFragmentManager().findFragmentByTag("tag_search_filters");
            mSearchFiltersFragment.setQuery(mQuery);
            mSearchFiltersFragment.setCallback(this);
        }
        setMode(false);




        mSearchView = (MaterialSearchView) findViewById(R.id.search_view);
        mSearchView.setVoiceSearch(true);
        mSearchView.showVoice(true);
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Logger.debug(LOG_TAG, "SEARCH: onQueryTextSubmit");
                onSearchQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Logger.debug(LOG_TAG, "SEARCH: onQueryTextChange");
                mSuggestionsIndex++;
                //new GetSuggestionsTask(MainActivity.this, newText, mSuggestionsIndex).execute();
                new SearchResultsActivity.GetSuggestionsTask(SearchResultsActivity.this, newText, mSuggestionsIndex).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return false;
            }
        });

        mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                Logger.debug(LOG_TAG, "SEARCH: onSearchViewShown");
            }

            @Override
            public void onSearchViewClosed() {
                Logger.debug(LOG_TAG, "SEARCH: onSearchViewClosed");
            }
        });
        mSearchView.setSubmitOnClick(true);
        mSearchAdapter = new SearchSuggestionAdapter(this);
        mSearchView.setAdapter(mSearchAdapter);
        mSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Logger.debug(LOG_TAG, "setOnItemClickListener");
                mSearchView.setQuery((String) mSearchAdapter.getItem(position), true);
            }
        });




    }



    @Override
    public void onItemSelected(Item item) {
        ModelUtil.startActivityByModel(this, item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public void onFilterChanged() {
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.remove(mSearchResultsFragment).commit();
    //    mSearchResultsFragment = new SearchResultsFragment(mQuery);
     //   mSearchResultsFragment.setOnItemSelectedListener(this);
        mSearchResultsFragment.refresh();
//        FragmentTransaction ft2 = getFragmentManager().beginTransaction();
//        ft2.add(R.id.search_result_container, mSearchFiltersFragment).commit();
        setMode(false);
    }



    private void switchMode() {
        mFiltersMode = !mFiltersMode;
        setMode(mFiltersMode);
    }


    private void setMode(boolean inFilterMode) {
        if(getDevice() == TABLET && isLandscape()) {
            return;
        }
        mFiltersMode = inFilterMode;
        if(inFilterMode) {

            mSwitchBtn.setText("Zpět na výsledky hledání");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            //ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
            ft.hide(mSearchResultsFragment);
            ft.show(mSearchFiltersFragment).commit();
        } else {
            mSwitchBtn.setText("Upřesnit hledání");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.hide(mSearchFiltersFragment);
            ft.show(mSearchResultsFragment).commit();
            //ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
            //ft.replace(R.id.search_result_container, mSearchResultsFragment).commit();
        }
    }

    @Override
    public void onClick(View v) {
        if(v == mSwitchBtn) {
            switchMode();
        }
    }









    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(item);

        return true;
    }


    class GetSuggestionsTask extends AsyncTask<String, Void, List<String>> {

        private Context tContext;
        private String tQuery;
        private int tIndex;

        public GetSuggestionsTask(Context context, String query, int index) {
            tContext = context;
            tQuery = query;
            if (tQuery != null) {
                tQuery = tQuery.trim();
            }
            tIndex = index;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<String> doInBackground(String... params) {
            if (tQuery == null || tQuery.length() < 1) {
                return new ArrayList<String>();
            }
            return K5ConnectorFactory.getConnector().getSuggestions(tContext, tQuery, 30);
        }

        @Override
        protected void onPostExecute(List<String> suggestions) {
            // mSuggestionsLoading = false;
            if (tContext == null || mSearchView == null || suggestions == null || tIndex < mSuggestionsIndex) {
                return;
            }
            Logger.debug(LOG_TAG, "setting suggestions: " + suggestions);
            List<String> s = new ArrayList<>();
            Cursor c = getContentResolver().query(KrameriusContract.SearchEntry.CONTENT_URI,
                    new String[]{ KrameriusContract.SearchEntry.COLUMN_QUERY },
                    KrameriusContract.SearchEntry.COLUMN_QUERY + " LIKE ? ",
                    new String[]{ tQuery + "%" },
                    KrameriusContract.SearchEntry.COLUMN_TIMESTAMP + " DESC");
            while(c.moveToNext()) {
                s.add(c.getString(0));
            }
            c.close();
            mSearchAdapter.refresh(tQuery, s, suggestions);
            if (mSearchAdapter.getCount() > 0) {
                mSearchView.showSuggestions();
            } else {
                mSearchView.dismissSuggestions();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    mSearchView.dismissSuggestions();
                    mSearchView.setQuery(searchWrd, true);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    public void onSearchQuery(String query) {
        if (query == null) {// || query.length() == 0) {
            return;
        }
        if (query.length() > 0) {
            ContentValues cv = new ContentValues();
            cv.put(KrameriusContract.SearchEntry.COLUMN_QUERY, query);
            cv.put(KrameriusContract.SearchEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());
            getContentResolver().insert(KrameriusContract.SearchEntry.CONTENT_URI, cv);
        }
        Intent intent = new Intent(SearchResultsActivity.this, SearchResultsActivity.class);
        intent.putExtra(SearchResultsActivity.EXTRA_QUERY, query);
        startActivity(intent);
        finish();
    }

}
