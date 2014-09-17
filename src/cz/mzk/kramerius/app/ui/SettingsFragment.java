package cz.mzk.kramerius.app.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		if (view != null) {
			Configuration config = getResources().getConfiguration();
			if (config.smallestScreenWidthDp < 720) {
				ScreenUtil.setInsets(getActivity(), view);
			}			
		}

		Preference domainButton = (Preference) findPreference(getString(R.string.pref_select_domain_key));
		
		String domain = K5Api.getDomain(getActivity());
		domainButton.setSummary(domain);
		domainButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent(getActivity(), DomainActivity.class);
				startActivity(intent);
				return true;
			}
		});

		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_view_mode_key)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_viewer_bg_color_key)));
		// bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_hardware_buttons_key)));

		return view;
	}

	private void bindPreferenceSummaryToValue(Preference preference) {
		preference.setOnPreferenceChangeListener(this);
		onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext())
				.getString(preference.getKey(), ""));
	}

	private void bindBooleanPreferenceSummaryToValue(Preference preference) {
		preference.setOnPreferenceChangeListener(this);
		onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext())
				.getString(preference.getKey(), ""));
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object value) {
		String stringValue = value.toString();

		if (preference instanceof ListPreference) {
			ListPreference listPreference = (ListPreference) preference;
			int prefIndex = listPreference.findIndexOfValue(stringValue);
			if (prefIndex >= 0) {
				preference.setSummary(listPreference.getEntries()[prefIndex]);
			}
		} else {
			preference.setSummary(stringValue);
		}
		return true;
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    Analytics.sendScreenView(getActivity(), R.string.ga_appview_settings);
	}	

}