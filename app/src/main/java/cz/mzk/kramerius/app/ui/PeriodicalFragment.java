package cz.mzk.kramerius.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.OnOpenDetailListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.card.PeriodicalCard;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.ParentChildrenPair;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.ShareUtils;
import cz.mzk.kramerius.app.util.TextUtil;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.internal.Card.OnLongCardClickListener;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;

public class PeriodicalFragment extends BaseFragment implements SearchView.OnQueryTextListener,
        SearchView.OnCloseListener, OnOpenDetailListener, OnPopupMenuSelectedListener {

    public static final String TAG = PeriodicalFragment.class.getName();

    private OnItemSelectedListener mCallback;

    private CardGridView mCardGridView;
    private CardGridArrayAdapter mAdapter;

    private DisplayImageOptions mOptions;

    private List<Item> mItems;
    private String mPid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_grid, container, false);
        mCardGridView = (CardGridView) view.findViewById(R.id.card_grid);
        inflateLoader(container, inflater);
        new GetPeriodicalVolumesTask(getActivity().getApplicationContext()).execute(mPid);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPid = getArguments().getString(BaseActivity.EXTRA_PID, "");
        setHasOptionsMenu(true);
    }

    public static PeriodicalFragment newInstance(String pid) {
        PeriodicalFragment f = new PeriodicalFragment();
        Bundle args = new Bundle();
        args.putString(BaseActivity.EXTRA_PID, pid);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

    }

    ;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mOptions = CardUtils.initUniversalImageLoaderLibrary(getActivity());
    }

    public void setItems(List<Item> items) {
        if (getActivity() == null) {
            return;
        }
        mItems = items;
        populate(items);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener callback) {
        mCallback = callback;
    }

    class GetPeriodicalVolumesTask extends AsyncTask<String, Void, ParentChildrenPair> {

        private Context tContext;

        public GetPeriodicalVolumesTask(Context context) {
            tContext = context;
        }

        @Override
        protected void onPreExecute() {
            startLoaderAnimation();
        }

        @Override
        protected ParentChildrenPair doInBackground(String... params) {
            Item item = K5ConnectorFactory.getConnector().getItem(tContext, params[0]);
            if (item == null) {
                return null;
            }
            if (ModelUtil.PERIODICAL.equals(item.getModel())) {
                return new ParentChildrenPair(item, K5ConnectorFactory.getConnector().getChildren(tContext, item.getPid(), ModelUtil.PERIODICAL_VOLUME));
            } else if (ModelUtil.PERIODICAL_VOLUME.equals(item.getModel())) {
                return new ParentChildrenPair(item, K5ConnectorFactory.getConnector().getChildren(tContext, item.getPid(), ModelUtil.PERIODICAL_ITEM));
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ParentChildrenPair result) {
            stopLoaderAnimation();
            if (getActivity() == null) {
                return;
            }
            if (result == null || result.getParent() == null || result.getChildren() == null) {
                showWarningMessage(R.string.warn_data_loading_failed, R.string.gen_again, new onWarningButtonClickedListener() {
                    @Override
                    public void onWarningButtonClicked() {
                        new GetPeriodicalVolumesTask(getActivity()).execute(mPid);
                    }
                });
                return;
            }
            String title = TextUtil.parseTitle(result.getParent().getRootTitle());
            getSupportActionBar().setTitle(title);
            String subtitle = "";
            if (ModelUtil.PERIODICAL.equals(result.getParent().getModel())) {
            } else if (ModelUtil.PERIODICAL_VOLUME.equals(result.getParent().getModel())) {
                subtitle = getString(R.string.metadata_periodical_volume) + " " + result.getParent().getVolumeTitle();
                getSupportActionBar().setSubtitle(subtitle);
            }
            if (result.getChildren().isEmpty()) {
                showWarningMessage(getString(R.string.warn_empty_result), null, null);
                return;
            } else {
                setItems(result.getChildren());
            }
        }
    }

    private void filterItems(String prefix) {
        if (mAdapter == null || mItems == null) {
            return;
        }
        if (prefix == null) {
            if (mItems.size() > mAdapter.getCount()) {
                populate(mItems);
            }
            return;
        }
        List<Item> items = new ArrayList<Item>();
        for (Item item : mItems) {
            // mAdapter.filter(prefix);
            if (ModelUtil.PERIODICAL_VOLUME.equals(item.getModel())) {
                if ((item.getVolumeNumber() != null && item.getVolumeNumber().startsWith(prefix))
                        || (item.getYear() != null && item.getYear().startsWith(prefix))) {
                    items.add(item);
                }
            } else if (ModelUtil.PERIODICAL_ITEM.equals(item.getModel())) {
                if ((item.getDate() != null && item.getDate().startsWith(prefix))
                        || (item.getPartNumber() != null && item.getPartNumber().startsWith(prefix))
                        || (item.getIssueNumber() != null && item.getIssueNumber().startsWith(prefix))) {
                    items.add(item);
                }
            }
        }
        populate(items);
    }

    private void populate(List<Item> items) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (Item item : items) {

            PeriodicalCard card = new PeriodicalCard(getActivity(), item, mOptions);
            card.setOnPopupMenuSelectedListener(this);
            card.setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    if (mCallback != null) {
                        mCallback.onItemSelected(((PeriodicalCard) card).getItem());
                    }
                }
            });
            card.setOnLongClickListener(new OnLongCardClickListener() {
                @Override
                public boolean onLongClick(Card card, View view) {
                    onPopupDetailsSelected((((PeriodicalCard) card).getItem()));
                    return false;
                }
            });

            cards.add(card);
        }
        mAdapter = new CardGridArrayAdapter(getActivity(), cards);
        CardUtils.setAnimationAdapter(mAdapter, mCardGridView);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (query.length() > 0) {
            filterItems(query);
        } else {
            filterItems(null);
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return onQueryTextChange(query);
    }

    @Override
    public boolean onClose() {
        filterItems(null);
        return false;
    }

    @Override
    public void onOpenDetail(String pid) {
        Intent intent = new Intent(getActivity(), MetadataActivity.class);
        intent.putExtra(MetadataActivity.EXTRA_PID, pid);
        startActivity(intent);
    }

    @Override
    public void onPopupOpenSelected(Item item) {
        if (mCallback != null) {
            mCallback.onItemSelected(item);
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
