package org.karlwelzel.vertretungsplan.material;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Karl on 11.10.2015.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
