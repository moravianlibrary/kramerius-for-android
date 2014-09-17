package cz.mzk.kramerius.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class HelpFragment extends Fragment {

	private static final String TAG = HelpFragment.class.getName();
 	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_help, container, false);
		Configuration config = getResources().getConfiguration();
		if (config.smallestScreenWidthDp < 720) {
			ScreenUtil.setInsets(getActivity(), view);
		}				
		new GetDoctypeCountTask(getActivity(),(TextView) view.findViewById(R.id.doctype_count_monograph)).execute(ModelUtil.MONOGRAPH);		
		new GetDoctypeCountTask(getActivity(),(TextView) view.findViewById(R.id.doctype_count_sound_recording)).execute(ModelUtil.SOUND_RECORDING);
		new GetDoctypeCountTask(getActivity(),(TextView) view.findViewById(R.id.doctype_count_map)).execute(ModelUtil.MAP);
		new GetDoctypeCountTask(getActivity(),(TextView) view.findViewById(R.id.doctype_count_periodical)).execute(ModelUtil.PERIODICAL);
		new GetDoctypeCountTask(getActivity(),(TextView) view.findViewById(R.id.doctype_count_graphic)).execute(ModelUtil.GRAPHIC);
		new GetDoctypeCountTask(getActivity(),(TextView) view.findViewById(R.id.doctype_count_manuscript)).execute(ModelUtil.MANUSCRIPT);								
		new GetDoctypeCountTask(getActivity(),(TextView) view.findViewById(R.id.doctype_count_sheetmusic)).execute(ModelUtil.SHEET_MUSIC);
		new GetDoctypeCountTask(getActivity(),(TextView) view.findViewById(R.id.doctype_count_archive)).execute(ModelUtil.ARCHIVE);
		new GetDoctypeCountTask(getActivity(),(TextView) view.findViewById(R.id.doctype_count_page)).execute(ModelUtil.PAGE);
		return view;
	}
	
	
	@Override
	public void onStart() {
	    super.onStart();
	    Analytics.sendScreenView(getActivity(), R.string.ga_appview_help);
	}	
	
	
	

	class GetDoctypeCountTask extends AsyncTask<String, Void, Integer> {

		private Context tContext;
		private TextView tView;

		public GetDoctypeCountTask(Context context, TextView view) {
			tContext = context;
			tView = view;
		}

		@Override
		protected Integer doInBackground(String... params) {
			return K5Connector.getInstance().getDoctypeCount(tContext, params[0]);
		}

		@Override
		protected void onPostExecute(Integer count) {
			if (tContext == null || tView == null || count == null) {
				return;
			}
			tView.setText(String.valueOf(count));
		}
	}
	


}
