package cz.mzk.kramerius.app.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
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
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		String query = getIntent().getStringExtra(EXTRA_QUERY);
		if (query == null) {
			finish();
			return;
		}
		SearchResultFragment fragment = SearchResultFragment.newInstance(query);
		fragment.setOnItemSelectedListener(this);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.search_result_container, fragment).commit();

		getActionBar().setTitle(R.string.search_result_title);
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

}
