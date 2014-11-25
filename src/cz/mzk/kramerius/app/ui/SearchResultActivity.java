package cz.mzk.kramerius.app.ui;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.ModelUtil;

public class SearchResultActivity extends BaseActivity implements OnItemSelectedListener {

	public static final String EXTRA_QUERY = "extra_query";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);

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

		SearchResultFragment fragment = SearchResultFragment.newInstance(query);
		fragment.setOnItemSelectedListener(this);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.search_result_container, fragment).commit();

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
