package cz.mzk.kramerius.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.util.Analytics;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

	private int mHackCounter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		//bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_view_mode_key)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_viewer_bg_color_key)));
		findPreference(getString(R.string.pref_advanced_button_key)).setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						onAdvancedSettings();
						return false;
					}
				});
		initHack();
		return view;
	}

	private void onAdvancedSettings() {
		Intent intent = new Intent(getActivity(), AdvancedSettingsActivity.class);
		startActivity(intent);
	}

	private void initHack() {
		mHackCounter = 0;
		Preference preference = findPreference(getString(R.string.pref_keep_screen_on_key));
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				mHackCounter++;
				if (mHackCounter >= 8) {
					if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(
							getString(R.string.pref_all_sources), false)) {
						Analytics.sendEvent(getActivity(), "settings", "domains_unlocked");
						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
								.putBoolean(getString(R.string.pref_all_sources), true).commit();
						Toast.makeText(getActivity(), "Other sources are available now!", Toast.LENGTH_LONG).show();
					} else {
						Analytics.sendEvent(getActivity(), "settings", "domains_locked");
						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
								.putBoolean(getString(R.string.pref_all_sources), false).commit();
						Toast.makeText(getActivity(), "Other sources are locked again!", Toast.LENGTH_LONG).show();						
					}
				}
				return true;
			}
		});
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

}