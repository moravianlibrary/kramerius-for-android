package cz.mzk.kramerius.app.ui;

import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.PeriodicalArrayAdapter;
import cz.mzk.kramerius.app.model.Item;

public class PeriodicalFragment extends Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

	public static final String TAG = PeriodicalFragment.class.getName();

	private ListView mList;

	private PeriodicalArrayAdapter mAdapter;
	private OnItemSelectedListener mCallback;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_periodical, container, false);
		mList = (ListView) view.findViewById(R.id.periodical_list);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long i) {
				onPeriodicalVolumeSelected(position);
			}
		});

		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.search, menu);
		MenuItem item = menu.findItem(R.id.search);
		SearchView searchView = (SearchView) item.getActionView();
		searchView.setOnQueryTextListener(this);
		searchView.setOnCloseListener(this);

	};

	public void setItems(List<Item> items, int type) {
		if (getActivity() == null) {
			return;
		}
		mAdapter = new PeriodicalArrayAdapter(getActivity(), items, type);
		mList.setAdapter(mAdapter);
	}

	public void setOnItemSelectedListener(OnItemSelectedListener callback) {
		mCallback = callback;
	}

	private void onPeriodicalVolumeSelected(int position) {
		if (mAdapter == null || mAdapter.getCount() < position + 1) {
			return;
		}
		Item item = mAdapter.getItem(position);
		if (mCallback != null) {
			mCallback.onItemSelected(item);
		}
	}

	private void filterItems(String prefix) {
		if (mAdapter == null) {
			return;
		}
		mAdapter.filter(prefix);
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
}
