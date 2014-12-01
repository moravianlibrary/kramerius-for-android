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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.card.SoundUnitCard;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.ParentChildrenPair;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.ShareUtils;
import cz.mzk.kramerius.app.util.TextUtil;

public class SoundRecordingFragment extends BaseFragment implements OnPopupMenuSelectedListener {

	public static final String TAG = SoundRecordingFragment.class.getName();

	private OnItemSelectedListener mCallback;

	private CardGridView mCardGridView;
	private CardGridArrayAdapter mAdapter;

	private DisplayImageOptions mOptions;

	private String mPid;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_card_grid, container, false);
		mCardGridView = (CardGridView) view.findViewById(R.id.card_grid);
		inflateLoader(container, inflater);
		new GetSoundUnitsTask(getActivity().getApplicationContext()).execute(mPid);
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPid = getArguments().getString(BaseActivity.EXTRA_PID, "");
		setHasOptionsMenu(true);
	}

	public static SoundRecordingFragment newInstance(String pid) {
		SoundRecordingFragment f = new SoundRecordingFragment();
		Bundle args = new Bundle();
		args.putString(BaseActivity.EXTRA_PID, pid);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mOptions = CardUtils.initUniversalImageLoaderLibrary(getActivity());
	}

	private void setItems(List<Item> items) {
		if (getActivity() == null) {
			return;
		}
		populate(items);
	}

	public void setOnItemSelectedListener(OnItemSelectedListener callback) {
		mCallback = callback;
	}

	class GetSoundUnitsTask extends AsyncTask<String, Void, ParentChildrenPair> {

		private Context tContext;

		public GetSoundUnitsTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			startLoaderAnimation();
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
			stopLoaderAnimation();
			if (getActivity() == null) {
				return;
			}
			if (result == null || result.getParent() == null || result.getChildren() == null) {
				showWarningMessage(R.string.warn_data_loading_failed, R.string.gen_again,
						new onWarningButtonClickedListener() {
							@Override
							public void onWarningButtonClicked() {
								new GetSoundUnitsTask(getActivity().getApplicationContext()).execute(mPid);
							}
						});
				return;
			}
			String title = TextUtil.parseTitle(result.getParent().getRootTitle());
			getSupportActionBar().setTitle(title);
//			if (ModelUtil.PERIODICAL.equals(result.getParent().getModel())) {
//			} else if (ModelUtil.PERIODICAL_VOLUME.equals(result.getParent().getModel())) {
//			}
			List<Item> items = new ArrayList<Item>();
			for(Item item : result.getChildren()) {
				if(ModelUtil.SOUND_UNIT.equals(item.getModel())) {
					items.add(item);
				}
			}
			if (result.getChildren().isEmpty()) {
				showWarningMessage(getString(R.string.warn_empty_result), null, null);
				return;
			} else {
				setItems(items);
			}
		}
	}

	private void populate(List<Item> items) {
		if (getActivity() == null) {
			return;
		}
		ArrayList<Card> cards = new ArrayList<Card>();
		for (Item item : items) {
			SoundUnitCard card = new SoundUnitCard(getActivity(), item, mOptions);
			card.setOnPopupMenuSelectedListener(this);
			card.setOnClickListener(new OnCardClickListener() {
				@Override
				public void onClick(Card card, View view) {
					if (mCallback != null) {
						mCallback.onItemSelected(((SoundUnitCard) card).getItem());
					}
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
		mAdapter = new CardGridArrayAdapter(getActivity(), cards);
		CardUtils.setAnimationAdapter(mAdapter, mCardGridView);
	}

	@Override
	public void onPopupOpenSelected(Item item) {
		if (mCallback != null) {
			mCallback.onItemSelected(item);
		}
	}

	@Override
	public void onPopupDetailsSelected(Item item) {
		Intent intent = new Intent(getActivity(), MetadataActivity.class);
		intent.putExtra(MetadataActivity.EXTRA_PID, item.getPid());
		startActivity(intent);
	}

	@Override
	public void onPopupShareSelected(Item item) {
		ShareUtils.openShareIntent(getActivity(), item.getPid());
	}

}
