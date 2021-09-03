package info.px0.paper.sirch.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.io.File;
import java.util.Arrays;

import info.px0.paper.sirch.R;
import info.px0.paper.sirch.config.GlobalConfig;

/**
 * Created by Paper on 01/07/2017.
 */

public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    //Defines keys to update summary automatically
    public final static String prefNickKey = "pref_nickname";
    public final static String prefAltNickKey = "pref_altnickname";
    public final static String prefDlKey = "pref_dlfolder";
    public final static String[] prefSummaryUpdate = {prefDlKey, prefNickKey, prefAltNickKey};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        String strEnvDl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();

        for (String sLoop : prefSummaryUpdate)
            setSummary(sp, sLoop, strEnvDl);

    }

    private void setSummary(SharedPreferences sp, String strKey, String strDefault) {
        EditTextPreference editTextPref = (EditTextPreference) findPreference(strKey);
        editTextPref
                .setSummary(sp.getString(strKey, strDefault));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (Arrays.asList(prefSummaryUpdate).contains(key)) {
            Preference pref = findPreference(key);
            EditTextPreference etp = null;
            if (pref instanceof EditTextPreference) {
                etp = (EditTextPreference) pref;
                pref.setSummary(etp.getText());
            }
            switch (key) {
                case prefNickKey:
                    if (etp != null) GlobalConfig.setStrNickname(etp.getText());
                    break;
                case prefAltNickKey:
                    if (etp != null) GlobalConfig.setStrAltNickname(etp.getText());
                    break;
                case prefDlKey:
                    if (etp != null) {

                        File f = new File(etp.getText());
                        if (!f.exists() || !f.isDirectory()) {
                            String strEnvDl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                            sharedPreferences.edit().putString(prefDlKey, strEnvDl).apply();
                        }
                        GlobalConfig.setStrDlFolder(etp.getText());
                    }
            }
        }
    }
}
