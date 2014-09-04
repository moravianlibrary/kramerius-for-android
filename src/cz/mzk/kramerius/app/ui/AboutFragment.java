package cz.mzk.kramerius.app.ui;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class AboutFragment extends Fragment {

	private static final String TAG = AboutFragment.class.getName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);
		Configuration config = getResources().getConfiguration();
		if (config.smallestScreenWidthDp < 720) {
			ScreenUtil.setInsets(getActivity(), view);
		}				
		return view;
	}
	
	
	@Override
	public void onStart() {
	    super.onStart();
	    Analytics.sendScreenView(getActivity(), R.string.ga_appview_about);
	}	

}
