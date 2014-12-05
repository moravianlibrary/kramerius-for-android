package cz.mzk.kramerius.app.ui;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.util.VersionUtils;

public class HelpContentFragment extends BaseFragment {

	private static final String EXTRA_VIEW = "extra_view_resource";

	private static final String LOG_TAG = HelpContentFragment.class.getSimpleName();

	private int mViewResource;

	public static HelpContentFragment newInstance(int viewResource) {
		HelpContentFragment f = new HelpContentFragment();
		Bundle args = new Bundle();
		args.putInt(EXTRA_VIEW, viewResource);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewResource = getArguments().getInt(EXTRA_VIEW, 0);
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(mViewResource, container, false);
		if(mViewResource == R.layout.fragment_help_about) {
			TextView version = (TextView) view.findViewById(R.id.help_about_version);
			if(version != null) {
				version.setText(VersionUtils.getVersion(getActivity()));
			}
			TextView description = (TextView) view.findViewById(R.id.help_about_description);
			if(description != null) {
				description.setLinkTextColor(getResources().getColor(R.color.color_primary));
				description.setText(Html.fromHtml(getString(R.string.help_about_description)));
				description.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
		return view;
	}

}
