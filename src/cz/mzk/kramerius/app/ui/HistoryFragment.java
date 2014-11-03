package cz.mzk.kramerius.app.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.view.CardGridView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.card.GridCard;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.card.RecentCard;
import cz.mzk.kramerius.app.data.KrameriusContract.HistoryEntry;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class HistoryFragment extends BaseFragment implements OnPopupMenuSelectedListener {

	private static final String LOG_TAG = HistoryFragment.class.getSimpleName();

	private OnItemSelectedListener mOnItemSelectedListener;

	private CardGridView mCardGridView;
	private CardGridArrayAdapter mAdapter;

	private DisplayImageOptions mOptions;

	public HistoryFragment() {

	}

	public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
		mOnItemSelectedListener = onItemSelectedListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mOptions = CardUtils.initUniversalImageLoaderLibrary(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_card_grid, container, false);
		if (isPhone()) {
	//		ScreenUtil.setInsets(getActivity(), view);
		}
		inflateLoader(container, inflater);
		mCardGridView = (CardGridView) view.findViewById(R.id.card_grid);		
		return view;
	}
	
	@Override
	public void onResume() {	
		super.onResume();
		new GetHistoryTask(getActivity()).execute(10);
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.sendScreenView(getActivity(), R.string.ga_appview_history);
	}

	class GetHistoryTask extends AsyncTask<Integer, Void, List<Card>> {

		private Context tContext;

		public GetHistoryTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
		//	startLoaderAnimation();
		}

		@Override
		protected List<Card> doInBackground(Integer... params) {
			int limit = params[0];
			String domain = K5Api.getDomain(tContext);
			Cursor c = tContext.getContentResolver().query(HistoryEntry.CONTENT_URI, HistoryEntry.PROJECTION,
					HistoryEntry.COLUMN_DOMAIN + "=?", new String[] { domain },
					HistoryEntry.COLUMN_TIMESTAMP + " DESC LIMIT " + limit);
			List<Card> cards = new ArrayList<Card>();
			while (c.moveToNext()) {
				RecentCard card = new RecentCard(tContext)
						.title(c.getString(c.getColumnIndex(HistoryEntry.COLUMN_TITLE)))
						.subtitle(c.getString(c.getColumnIndex(HistoryEntry.COLUMN_SUBTITLE)))
						.pid(c.getString(c.getColumnIndex(HistoryEntry.COLUMN_PID)))
						.parentPid(c.getString(c.getColumnIndex(HistoryEntry.COLUMN_PARENT_PID)))
						.timestamp(c.getLong(c.getColumnIndex(HistoryEntry.COLUMN_TIMESTAMP)))
						.popupListener(HistoryFragment.this)
						.build(mOptions);

				// card.setOnPopupMenuSelectedListener(popupListener);
				card.setOnClickListener(new OnCardClickListener() {
					@Override
					public void onClick(Card card, View view) {
						if (mOnItemSelectedListener != null) {
							mOnItemSelectedListener.onItemSelected(((RecentCard) card).getItem());
						}
					}
				});
				cards.add(card);

			}
			c.close();
			return cards;
		}

		@Override
		protected void onPostExecute(List<Card> result) {
			//stopLoaderAnimation();
			if (tContext == null || result == null) {
				return;
			}
			populateGrid(result);
		}
	}

	private void populateGrid(List<Card> cards) {
		mAdapter = new CardGridArrayAdapter(getActivity(), cards);
		CardUtils.setAnimationAdapter(mAdapter, mCardGridView);
	}

	public void onOpenDetail(String pid) {
		Intent intent = new Intent(getActivity(), MetadataActivity.class);
		intent.putExtra(MetadataActivity.EXTRA_PID, pid);
		startActivity(intent);
	}

	@Override
	public void onPopupOpenSelectd(Item item) {
		if (mOnItemSelectedListener != null) {
			mOnItemSelectedListener.onItemSelected(item);
		}
	}

	@Override
	public void onPopupDetailsSelectd(Item item) {
		onOpenDetail(item.getPid());
	}

	@Override
	public void onPopupShareSelectd(Item item) {
		// TODO Auto-generated method stub

	}

}
