package cz.mzk.kramerius.app.ui;

import java.util.Collections;
import java.util.List;

import com.google.analytics.tracking.android.EasyTracker;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.ItemByTitleComparator;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.PeriodicalArrayAdapter;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.ParentChildrenPair;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.TextUtil;

public class PeriodicalActivity extends BaseActivity implements OnItemSelectedListener {

	private View mLoader;
	private Animation mLoaderAnimation;
	private PeriodicalFragment mPeriodicalFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_periodical);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setIcon(android.R.color.transparent);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("K5 - Digitální knihovna");

		mPeriodicalFragment = (PeriodicalFragment) getFragmentManager().findFragmentById(R.id.periodical_fragment);
		mPeriodicalFragment.setOnItemSelectedListener(this);
		String pid = getIntent().getExtras().getString(EXTRA_PID);
		mLoader = findViewById(R.id.loader);
		mLoaderAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation);
		mLoaderAnimation.setRepeatCount(Animation.INFINITE);

		new GetPeriodicalVolumesTask(this).execute(pid);
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

	class GetPeriodicalVolumesTask extends AsyncTask<String, Void, ParentChildrenPair> {

		private Context tContext;

		public GetPeriodicalVolumesTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			mLoader.setVisibility(View.VISIBLE);
			mLoader.startAnimation(mLoaderAnimation);
		}

		@Override
		protected ParentChildrenPair doInBackground(String... params) {
			Item item = K5Connector.getInstance().getItem(tContext, params[0]);
			if(item == null) {
				return null;
			}
			return new ParentChildrenPair(item, K5Connector.getInstance().getChildren(tContext, item.getPid()));
		}

		@Override
		protected void onPostExecute(ParentChildrenPair result) {
			mLoader.clearAnimation();
			mLoader.setVisibility(View.GONE);
			if (tContext == null || result == null || result.getParent() == null) {
				return;
			}
			getActionBar().setTitle(TextUtil.shortenforActionBar(result.getParent().getTitle()));						
			List<Item> items = result.getChildren();
			if (items != null) {
				//Collections.sort(items, new ItemByTitleComparator());
				int type = PeriodicalArrayAdapter.TYPE_ITEM;
				if (ModelUtil.PERIODICAL.equals(result.getParent().getModel())) {
					type = PeriodicalArrayAdapter.TYPE_VOLUME;
				} else if (ModelUtil.PERIODICAL_VOLUME.equals(result.getParent().getModel())) {
					type = PeriodicalArrayAdapter.TYPE_ITEM;
				}
				mPeriodicalFragment.setItems(items, type);
			}
		}
	}

	@Override
	public void onItemSelected(Item item) {
		Intent intent = null;
		if(ModelUtil.PERIODICAL_VOLUME.equals(item.getModel())) {
			 intent = new Intent(PeriodicalActivity.this, PeriodicalActivity.class);
		} else 		if(ModelUtil.PERIODICAL_ITEM.equals(item.getModel())) {
			 intent = new Intent(PeriodicalActivity.this, PageActivity.class);
		} else {
			return;
		}
		intent.putExtra(EXTRA_PID, item.getPid());
		startActivity(intent);

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