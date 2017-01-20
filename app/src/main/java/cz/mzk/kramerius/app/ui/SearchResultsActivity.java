package cz.mzk.kramerius.app.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.analytics.tracking.android.EasyTracker;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.search.Query;
import cz.mzk.kramerius.app.util.ModelUtil;

public class SearchResultsActivity extends BaseActivity implements View.OnClickListener, OnItemSelectedListener, SearchFiltersFragment.FilterListener {

    public static final String EXTRA_QUERY = "extra_query";


    private SearchResultsFragment mSearchResultsFragment;
    private SearchFiltersFragment mSearchFiltersFragment;

    private boolean mFiltersMode;
    private Button mSwitchBtn;
    private Query mQuery;

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
        if (query == null) {
            finish();
            return;
        }

        mQuery = new Query(query);

        mSwitchBtn = (Button) findViewById(R.id.switch_btn);
        mSwitchBtn.setOnClickListener(this);



        if(savedInstanceState == null) {
            mSearchResultsFragment = new SearchResultsFragment();
            mSearchResultsFragment.setQuery(mQuery);
            mSearchResultsFragment.setOnItemSelectedListener(this);

            mSearchFiltersFragment = new SearchFiltersFragment();
            mSearchFiltersFragment.setQuery(mQuery);
            mSearchFiltersFragment.setCallback(this);


            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.search_result_container, mSearchResultsFragment, "tag_search_results");
            ft.add(R.id.search_result_container, mSearchFiltersFragment, "tag_search_filters").commit();
        } else {
            mSearchResultsFragment = (SearchResultsFragment) getFragmentManager().findFragmentByTag("tag_search_results");
            mSearchResultsFragment.setQuery(mQuery);
            mSearchResultsFragment.setOnItemSelectedListener(this);

            mSearchFiltersFragment = (SearchFiltersFragment) getFragmentManager().findFragmentByTag("tag_search_filters");
            mSearchFiltersFragment.setQuery(mQuery);
            mSearchFiltersFragment.setCallback(this);
        }
        setMode(false);
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
}
