package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.DomainArrayAdapter;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Domain;

public class DomainActivity extends BaseActivity {

	public static final String TAG = DomainActivity.class.getName();

	private ListView mList;

	private DomainArrayAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_domain);

		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("Zdroj dat");

		mList = (ListView) findViewById(R.id.domain_list);

		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long i) {
				if (mAdapter == null) {
					return;
				}
				onDomainSelected(position);
			}
		});
		String currentDomain = K5Api.getDomain(this);
		mAdapter = new DomainArrayAdapter(this, getTestDomainList(), currentDomain);
		mList.setAdapter(mAdapter);
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

	private List<Domain> getTestDomainList() {
		List<Domain> list = new ArrayList<Domain>();
		list.add(new Domain("Moravská zemská knihovna", "Digitální knihovna MZK", "http", "kramerius.mzk.cz",
				R.drawable.logo_mzk));
		list.add(new Domain("Národní knihovna", "Digitální knihovna NKP", "http", "kramerius4.nkp.cz",						
				R.drawable.logo_nkp));
		list.add(new Domain("Národní digitální knihovna", "Digitální knihovna NDK", "http", "krameriusndktest.mzk.cz",						
				R.drawable.logo_ndk));		
//		list.add(new Domain("Vědecká knihovna v Olomouci", "Digitální knihovna VKP", "http", "kramerius.vkp.cz",
//				R.drawable.logo_vkol));
//		list.add(new Domain("Knihovna Akademie věd ČR", "Digitální knihovna KNAV", "http", "cdk-test.lib.cas.cz",
//				R.drawable.logo_knav));
//		list.add(new Domain("Národní technická knihovna", "Digitální knihovna NTK", "http", "kramerius.ntk.cz",
//				R.drawable.logo_ntk));
		list.add(new Domain("Moravská zemská knihovna", "Docker MZK", "http", "docker.mzk.cz", R.drawable.logo_mzk));
	//	list.add(new Domain("Moravská zemská knihovna", "Test MZK", "http", "krameriustest.mzk.cz", R.drawable.logo_mzk));
	//	list.add(new Domain("Moravská zemská knihovna", "Demo MZK", "http", "krameriusdemo.mzk.cz", R.drawable.logo_mzk));

		return list;
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

	private void onDomainSelected(int position) {
		// if (mAdapter == null || mAdapter.getCount() < position + 1) {
		// return;
		// }
		// Domain domain = mAdapter.getItem(position);
		// PreferenceManager.getDefaultSharedPreferences(this).edit().putString("domain",
		// domain.getUrl()).commit();
		// K5Connector.getInstance().restart();
		// Intent intent = new Intent(DomainActivity.this, MainActivity.class);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		// startActivity(intent);
	}

}
