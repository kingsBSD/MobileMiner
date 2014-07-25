package uk.ac.kcl.odo.mobileminer.activities.fragments;

import uk.ac.kcl.odo.mobileminer.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class MinerSettingsFragment extends PreferenceFragment {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
