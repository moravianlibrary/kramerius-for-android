package cz.mzk.kramerius.app.ui;

import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.OnOpenDetailListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.BaseFragment.onWarningButtonClickedListener;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.ui.PeriodicalFragment.GetPeriodicalVolumesTask;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class FeaturedFragment extends BaseFragment implements OnPopupMenuSelectedListener {

	private static final String EXTRA_TYPE = "extra_type";

	private static final String TAG = FeaturedFragment.class.getSimpleName();

	private int mType;
	private OnItemSelectedListener mOnItemSelectedListener;

	private CardGridView mCardGridView;
	private CardGridArrayAdapter mAdapter;

	private DisplayImageOptions mOptions;

	public static FeaturedFragment newInstance(int type) {
		FeaturedFragment f = new FeaturedFragment();
		Bundle args = new Bundle();
		args.putInt(EXTRA_TYPE, type);
		f.setArguments(args);
		return f;
	}

	public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
		mOnItemSelectedListener = onItemSelectedListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mType = getArguments().getInt(EXTRA_TYPE, K5Api.FEED_NEWEST);
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
		new GetFeaturedTask(getActivity()).execute(mType);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		switch (mType) {
		case K5Api.FEED_NEWEST:
			Analytics.sendScreenView(getActivity(), R.string.ga_appview_newest);
			break;
		case K5Api.FEED_SELECTED:
			Analytics.sendScreenView(getActivity(), R.string.ga_appview_selected);
			break;
		case K5Api.FEED_MOST_DESIRABLE:
			Analytics.sendScreenView(getActivity(), R.string.ga_appview_most_desirable);
			break;
		}

	}

	class GetFeaturedTask extends AsyncTask<Integer, Void, List<Item>> {

		private Context tContext;

		public GetFeaturedTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			startLoaderAnimation();
		}

		@Override
		protected List<Item> doInBackground(Integer... params) {
			if (params[0] == K5Api.FEED_NEWEST) {
				return K5Connector.getInstance().getNewest(tContext, -1);
			} else if (params[0] == K5Api.FEED_MOST_DESIRABLE) {
				return K5Connector.getInstance().getMostDesirable(tContext, -1);
			} else {
				return K5Connector.getInstance().getSelected(tContext, true, -1);
			}
		}

		@Override
		protected void onPostExecute(List<Item> result) {
			stopLoaderAnimation();
			if (tContext == null || result == null) {
				showWarningMessage("Nepodařilo se načíst data.", "Opakovat", new onWarningButtonClickedListener() {
					@Override
					public void onWarningButtonClicked() {
						new GetFeaturedTask(getActivity()).execute(mType);
					}
				});
				return;
			}			
			populateGrid(result);
		}
	}

	private void populateGrid(List<Item> items) {
		mAdapter = CardUtils.createAdapter(getActivity(), items, mOnItemSelectedListener, this, mOptions);
		CardUtils.setAnimationAdapter(mAdapter, mCardGridView);		
	}

	public void onOpenDetail(String pid) {
		Intent intent = new Intent(getActivity(), MetadataActivity.class);
		intent.putExtra(MetadataActivity.EXTRA_PID, pid);
		startActivity(intent);
	}

	
	@Override
	public void onPopupOpenSelectd(Item item) {
		if(mOnItemSelectedListener != null) {
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
