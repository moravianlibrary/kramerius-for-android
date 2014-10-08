package cz.mzk.kramerius.app.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import cz.mzk.kramerius.app.BaseActivity;

public class SettingsActivity extends BaseActivity {

	private static final int CONTENT_VIEW_ID = 10101010;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setIcon(android.R.color.transparent);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);		
		FrameLayout frame = new FrameLayout(this);
		frame.setBackgroundColor(Color.WHITE);
		frame.setId(CONTENT_VIEW_ID);
		setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		if (savedInstanceState == null) {
			Fragment newFragment = new SettingsFragment();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(CONTENT_VIEW_ID, newFragment).commit();
		}
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