package cz.mzk.kramerius.app.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.PrefUtils;

public class VirtualCollectionActivity extends BaseActivity implements OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_collection);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String pid = getIntent().getStringExtra(EXTRA_PID);
        if (pid == null) {
            finish();
            return;
        }
        String title = getIntent().getStringExtra(EXTRA_TITLE);

        SearchQuery query = new SearchQuery().virtualCollection(pid);
        String policy = PrefUtils.getPolicy(this);
        if (policy != null) {
            query.add(SearchQuery.POLICY, policy);
        }

        SearchResultFragment fragment = SearchResultFragment.newInstance(query.build());
        fragment.setOnItemSelectedListener(this);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.vc_container, fragment).commit();

        getSupportActionBar().setTitle(title);
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

}
