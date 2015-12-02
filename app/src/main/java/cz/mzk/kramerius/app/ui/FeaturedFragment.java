package cz.mzk.kramerius.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.List;

import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.PrefUtils;
import cz.mzk.kramerius.app.util.ShareUtils;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;

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
        inflateLoader(container, inflater);
        mCardGridView = (CardGridView) view.findViewById(R.id.card_grid);
        new GetFeaturedTask(getActivity().getApplicationContext()).execute(mType);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        switch (mType) {
            case K5Api.FEED_NEWEST:
                Analytics.sendScreenView(getActivity(), R.string.ga_appview_newest);
                break;
            case K5Api.FEED_CUSTOM:
                Analytics.sendScreenView(getActivity(), R.string.ga_appview_custom);
                break;
            case K5Api.FEED_MOST_DESIRABLE:
                Analytics.sendScreenView(getActivity(), R.string.ga_appview_most_desirable);
                break;
        }

    }

    class GetFeaturedTask extends AsyncTask<Integer, Void, List<Item>> {

        private Context tContext;
        private String tPolicy;

        public GetFeaturedTask(Context context) {
            tContext = context;
            tPolicy = PrefUtils.getPolicy(context);
        }

        @Override
        protected void onPreExecute() {
            startLoaderAnimation();
        }

        @Override
        protected List<Item> doInBackground(Integer... params) {
            int type = params[0];
            return K5ConnectorFactory.getConnector().getFeatured(tContext, type, K5Api.FEED_NO_LIMIT, tPolicy, null);
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
                        new GetFeaturedTask(getActivity().getApplicationContext()).execute(mType);
                    }
                });
                return;
            }
            populateGrid(result);
        }
    }

    private void populateGrid(List<Item> items) {
        if (getActivity() == null) {
            return;
        }
        mAdapter = CardUtils.createAdapter(getActivity(), items, mOnItemSelectedListener, this, mOptions);
        CardUtils.setAnimationAdapter(mAdapter, mCardGridView);
    }

    public void onOpenDetail(String pid) {
        Intent intent = new Intent(getActivity(), MetadataActivity.class);
        intent.putExtra(MetadataActivity.EXTRA_PID, pid);
        startActivity(intent);
    }

    @Override
    public void onPopupOpenSelected(Item item) {
        if (mOnItemSelectedListener != null) {
            mOnItemSelectedListener.onItemSelected(item);
        }
    }

    @Override
    public void onPopupDetailsSelected(Item item) {
        onOpenDetail(item.getPid());
    }

    @Override
    public void onPopupShareSelected(Item item) {
        ShareUtils.openShareIntent(getActivity(), item.getPid());
    }

}
