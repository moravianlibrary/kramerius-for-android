package cz.mzk.kramerius.app.ui;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.VirtualCollectionsArrayAdapter;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class VirtualCollectionsFragment extends Fragment {

	private static final String TAG = VirtualCollectionsFragment.class.getName();

	private ListView mListView;

	public static VirtualCollectionsFragment newInstance() {
		VirtualCollectionsFragment f = new VirtualCollectionsFragment();
		// Bundle args = new Bundle();
		// args.putInt(EXTRA_TYPE, type);
		// f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// mType = getArguments().getInt(EXTRA_TYPE, K5Connector.TYPE_NEWEST);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_virtual_collections, container, false);
		Configuration config = getResources().getConfiguration();
		if (config.smallestScreenWidthDp < 720) {
			ScreenUtil.setInsets(getActivity(), view);
		}				
		mListView = (ListView) view.findViewById(R.id.listview);
		new GetVirtualCollectionsTask(getActivity()).execute();
		return view;
	}

	class GetVirtualCollectionsTask extends AsyncTask<Void, Void, List<Item>> {

		private Context tContext;

		public GetVirtualCollectionsTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected List<Item> doInBackground(Void... params) {
			return K5Connector.getInstance().getVirtualCollctions(tContext);
		}

		@Override
		protected void onPostExecute(List<Item> result) {
			if(tContext == null) {
				return;
			}
			mListView.setAdapter(new VirtualCollectionsArrayAdapter(tContext, result));

		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

}
