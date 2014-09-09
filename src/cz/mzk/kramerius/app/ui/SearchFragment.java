package cz.mzk.kramerius.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class SearchFragment extends Fragment implements OnClickListener {

	private static final String TAG = SearchFragment.class.getName();

	private Button mSearch;
	private EditText mInputTitle;
	private OnSearchListener mOnSearchListener;

	public interface OnSearchListener {
		public void onSearchByTitle(String title);
	}

	public void setOnSearchListener(OnSearchListener onSearchListener) {
		mOnSearchListener = onSearchListener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		Configuration config = getResources().getConfiguration();
		if (config.smallestScreenWidthDp < 720) {
			ScreenUtil.setInsets(getActivity(), view);
		}

		mSearch = (Button) view.findViewById(R.id.search_button);
		mSearch.setOnClickListener(this);
		mInputTitle = (EditText) view.findViewById(R.id.search_input_title);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.sendScreenView(getActivity(), R.string.ga_appview_search);
	}

	@Override
	public void onClick(View v) {
		if (v == mSearch) {
			InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
					Context.INPUT_METHOD_SERVICE);

			// check if no view has focus:
			View view = getActivity().getCurrentFocus();
			if (view != null) {
				inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
			String title = mInputTitle.getText().toString();
			if (mOnSearchListener != null) {
				mOnSearchListener.onSearchByTitle(title);
			}
		}

	}

}
