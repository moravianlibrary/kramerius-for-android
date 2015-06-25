package cz.mzk.kramerius.app.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.card.VirtualCollectionCard;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.CardUtils;

public class VirtualCollectionsFragment extends BaseFragment {

	private static final String TAG = VirtualCollectionsFragment.class.getName();

	private OnVirtualCollectionListener mOnVirtualCollectionListener;

	private CardGridView mCardGridView;
	private CardGridArrayAdapter mAdapter;

	public static VirtualCollectionsFragment newInstance() {
		VirtualCollectionsFragment f = new VirtualCollectionsFragment();
		return f;
	}

	public void setOnVirtualCollectionListener(OnVirtualCollectionListener listener) {
		mOnVirtualCollectionListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_virtual_collections, container, false);
		mCardGridView = (CardGridView) view.findViewById(R.id.card_grid);
		inflateLoader((ViewGroup) view, inflater);
		new GetVirtualCollectionsTask(getActivity().getApplicationContext()).execute();
		return view;
	}

	public interface OnVirtualCollectionListener {
		public void onVirtualCollectionSelected(Item vc);
	}

	private void onVirtualCollectionSelected(Item vc) {
		if (mOnVirtualCollectionListener != null) {
			mOnVirtualCollectionListener.onVirtualCollectionSelected(vc);
		}
	}

	
	class GetVirtualCollectionsTask extends AsyncTask<Void, Void, List<Item>> {

		private Context tContext;

		public GetVirtualCollectionsTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			startLoaderAnimation();
		}

		@Override
		protected List<Item> doInBackground(Void... params) {
			return K5ConnectorFactory.getConnector().getVirtualCollections(tContext);
		}

		@Override
		protected void onPostExecute(List<Item> result) {
			stopLoaderAnimation();
			if (getActivity() == null) {
				return;
			}
			if (result == null) {
				showWarningMessage(R.string.warn_data_loading_failed, R.string.gen_again, new onWarningButtonClickedListener() {

					@Override
					public void onWarningButtonClicked() {
						new GetVirtualCollectionsTask(getActivity().getApplicationContext()).execute();
					}
				});
				return;
			}
			if (result.isEmpty()) {
				showWarningMessage(getString(R.string.warn_empty_vc), null, null);
				return;
			}

			ArrayList<Card> cards = new ArrayList<Card>();
			for (Item item : result) {
				VirtualCollectionCard card = new VirtualCollectionCard(getActivity(), item);
				card.setOnClickListener(new OnCardClickListener() {
					@Override
					public void onClick(Card card, View view) {
						onVirtualCollectionSelected(((VirtualCollectionCard) card).getItem());
					}
				});

				cards.add(card);
			}

			mAdapter = new CardGridArrayAdapter(getActivity(), cards);
			CardUtils.setAnimationAdapter(mAdapter, mCardGridView);
		}

	}
	
	@Override
	public void onStart() {
		super.onStart();
		Analytics.sendScreenView(getActivity(), R.string.ga_appview_virtual_collections);
	}

}
