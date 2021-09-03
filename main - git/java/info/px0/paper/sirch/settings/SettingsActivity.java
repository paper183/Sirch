package info.px0.paper.sirch.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Paper on 01/07/2017.
 */

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

}
