package cz.mzk.kramerius.app.ui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.ServerErrorException;
import cz.mzk.kramerius.app.BaseFragment.onWarningButtonClickedListener;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.Domain;
import cz.mzk.kramerius.app.ui.PeriodicalFragment.GetPeriodicalVolumesTask;
import cz.mzk.kramerius.app.util.DomainUtil;
import cz.mzk.kramerius.app.util.ModelUtil;

public class DomainDetailFragment extends BaseFragment {

	private static final String LOG_TAG = DomainDetailFragment.class.getSimpleName();
	private String mDomainKey;

	private ViewGroup mDoctypeContainer;
	private Domain domain;

	public static DomainDetailFragment newInstance(String domain) {
		DomainDetailFragment f = new DomainDetailFragment();
		Bundle args = new Bundle();
		args.putString(BaseActivity.EXTRA_DOMAIN, domain);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDomainKey = getArguments().getString(BaseActivity.EXTRA_DOMAIN);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_domain_detail, container, false);
		if (mDomainKey == null) {
			return view;
		}
		domain = DomainUtil.getDomain(mDomainKey);
		if (domain == null) {
			return view;
		}
		getSupportActionBar().setTitle(domain.getTitle());
		TextView name = (TextView) view.findViewById(R.id.domain_detail_name);
		TextView url = (TextView) view.findViewById(R.id.domain_detail_url);
		name.setText(domain.getTitle());
		url.setText(domain.getDomain());
		mDoctypeContainer = (ViewGroup) view.findViewById(R.id.domain_detail_doctype_container);
		inflateLoader(mDoctypeContainer, inflater);
		new GetDomainDetailTask(getActivity().getApplicationContext()).execute(domain);
		return view;
	}

	class GetDomainDetailTask extends AsyncTask<Domain, Void, Map<String, Integer>> {

		private Context tContext;

		public GetDomainDetailTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			startLoaderAnimation();
		}

		@Override
		protected Map<String, Integer> doInBackground(Domain... params) {
			Map<String, Integer> result = new LinkedHashMap<String, Integer>();
			Domain domain = params[0];
			Domain currentDomain = DomainUtil.getCurrentDomain(tContext);
			K5Api.setDomain(tContext, domain);
			try {
				addType(result, ModelUtil.MONOGRAPH);
				addType(result, ModelUtil.PERIODICAL);
				addType(result, ModelUtil.ARCHIVE);
				addType(result, ModelUtil.GRAPHIC);
				addType(result, ModelUtil.MANUSCRIPT);
				addType(result, ModelUtil.MAP);
				addType(result, ModelUtil.SHEET_MUSIC);
				addType(result, ModelUtil.SOUND_RECORDING);
				addType(result, ModelUtil.PAGE);
			} catch (ServerErrorException ex) {
				K5Api.setDomain(tContext, currentDomain);
				return null;
			}
			K5Api.setDomain(tContext, currentDomain);
			return result;
		}

		private void addType(Map<String, Integer> map, String type) throws ServerErrorException {
			int count = K5Connector.getInstance().getDoctypeCount(tContext, type);
			if (count == -1) {
				throw new ServerErrorException("doctype count request failed for " + type);
			}
			if (count > 0) {
				map.put(type, count);
			}
		}

		@Override
		protected void onPostExecute(Map<String, Integer> result) {
			stopLoaderAnimation();
			if (getActivity() == null) {
				return;
			}
			if (result == null) {
				showWarningMessage(R.string.warn_data_loading_failed, R.string.gen_again,
						new onWarningButtonClickedListener() {
							@Override
							public void onWarningButtonClicked() {
								new GetDomainDetailTask(getActivity().getApplicationContext()).execute(domain);
							}
						});
				return;
			}
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for (String type : result.keySet()) {
				View view = inflater.inflate(R.layout.view_doctype, mDoctypeContainer, false);
				ImageView icon = (ImageView) view.findViewById(R.id.doctype_icon);
				TextView name = (TextView) view.findViewById(R.id.doctype_name);
				TextView count = (TextView) view.findViewById(R.id.doctype_count);
				icon.setImageResource(ModelUtil.getIcon(type));
				name.setText(getString(ModelUtil.getLabel(type)));
				count.setText(String.valueOf(result.get(type)));
				mDoctypeContainer.addView(view);
			}

		}
	}
}
