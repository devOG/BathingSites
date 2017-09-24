package se.miun.osgu1400.bathingsites;

import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Set summary of Weather URL to its value
        final EditTextPreference weatherURL = (EditTextPreference) findPreference("weather_url");
        weatherURL.setSummary(weatherURL.getText());

        // Change summary of Weather URL to its value every time it's updated
        weatherURL.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String url = newValue.toString();
                        weatherURL.setSummary(url);
                        return true;
                    }
                }
        );

        // Set summary of Download URL to its value
        final EditTextPreference downloadURL = (EditTextPreference) findPreference("download_url");
        downloadURL.setSummary(downloadURL.getText());

        // Change summary of Download URL to its value every time it's updated
        downloadURL.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String url = newValue.toString();
                        downloadURL.setSummary(url);
                        return true;
                    }
                }
        );
    }
}
