package cz.mzk.kramerius.app.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.util.ModelUtil;

public class VirtualCollectionActivity extends BaseActivity implements OnItemSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_virtual_collection);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		String pid = getIntent().getStringExtra(EXTRA_PID);
		if (pid == null) {
			finish();
			return;
		}
		String title = getIntent().getStringExtra(EXTRA_TITLE);

		SearchQuery query = new SearchQuery().virtualCollection(pid).allModels();

		SearchResultFragment fragment = SearchResultFragment.newInstance(query.build());
		fragment.setOnItemSelectedListener(this);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.vc_container, fragment).commit();

		
		getActionBar().setTitle(title);
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
