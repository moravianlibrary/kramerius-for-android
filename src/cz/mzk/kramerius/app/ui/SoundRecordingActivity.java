package cz.mzk.kramerius.app.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.internal.Card.OnLongCardClickListener;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.card.PeriodicalCard;
import cz.mzk.kramerius.app.card.SoundUnitCard;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.ParentChildrenPair;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.ShareUtils;
import cz.mzk.kramerius.app.util.TextUtil;

public class SoundRecordingActivity extends BaseActivity implements OnPopupMenuSelectedListener {

	public static final String TAG = SoundRecordingActivity.class.getName();

	private CardGridView mCardGrid;
	private CardGridArrayAdapter mAdapter;

	private DisplayImageOptions mOptions;

	private Animation mLoaderAnimation;
	private View mLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_recording);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		
		mCardGrid = (CardGridView) findViewById(R.id.card_grid);
		String pid = getIntent().getStringExtra(EXTRA_PID);
		mLoader = findViewById(R.id.loader);
		mLoaderAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation);
		mLoaderAnimation.setRepeatCount(Animation.INFINITE);
		mOptions = CardUtils.initUniversalImageLoaderLibrary(this);
		new GetSoundUnitsTask(this).execute(pid);
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

	class GetSoundUnitsTask extends AsyncTask<String, Void, ParentChildrenPair> {

		private Context tContext;

		public GetSoundUnitsTask(Context context) {
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
			if (item == null) {
				return null;
			}
			return new ParentChildrenPair(item, K5Connector.getInstance().getChildren(tContext, item.getPid()));
		}

		@Override
		protected void onPostExecute(ParentChildrenPair result) {
			mLoader.clearAnimation();
			mLoader.setVisibility(View.GONE);
			if (tContext == null || result.getParent() == null) {
				return;
			}
			Item parent = result.getParent();
			List<Item> children = result.getChildren();
			getSupportActionBar().setTitle(TextUtil.shortenforActionBar(parent.getTitle()));
			if (children == null) {
				return;
			}
			populateGrid(children);
		}
	}

	private void populateGrid(List<Item> items) {
		ArrayList<Card> cards = new ArrayList<Card>();
		for (Item item : items) {
			SoundUnitCard card = new SoundUnitCard(this, item, mOptions);
			card.setOnPopupMenuSelectedListener(this);
			card.setOnClickListener(new OnCardClickListener() {
				@Override
				public void onClick(Card card, View view) {
					openSoundUnit(((SoundUnitCard) card).getItem());
				}
			});
			card.setOnLongClickListener(new OnLongCardClickListener() {				
				@Override
				public boolean onLongClick(Card card, View view) {
					onPopupDetailsSelected((((SoundUnitCard) card).getItem()));
					return false;
				}
			});
			cards.add(card);
		}
		mAdapter = new CardGridArrayAdapter(this, cards);
		CardUtils.setAnimationAdapter(mAdapter, mCardGrid);

	}

	private void openSoundUnit(Item item) {
		if (ModelUtil.SOUND_UNIT.equals(item.getModel())) {
			Intent intent = new Intent(SoundRecordingActivity.this, SoundUnitActivity.class);
			intent.putExtra(EXTRA_PID, item.getPid());
			startActivity(intent);
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

	public void onOpenDetail(String pid) {
		Intent intent = new Intent(SoundRecordingActivity.this, MetadataActivity.class);
		intent.putExtra(MetadataActivity.EXTRA_PID, pid);
		startActivity(intent);
	}

	@Override
	public void onPopupOpenSelected(Item item) {
		openSoundUnit(item);
	}

	@Override
	public void onPopupDetailsSelected(Item item) {
		onOpenDetail(item.getPid());
	}

	@Override
	public void onPopupShareSelected(Item item) {
		ShareUtils.openShareIntent(this, item.getPid());
	}

}
