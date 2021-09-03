package info.px0.paper.sirch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import info.px0.paper.sirch.config.GlobalConfig;
import info.px0.paper.sirch.config.RoomConfig;
import info.px0.paper.sirch.config.ServerConfig;
import info.px0.paper.sirch.settings.SettingsActivity;

import java.io.File;
import java.lang.ref.WeakReference;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONObject;



public class sirchMain extends AppCompatActivity {

    //private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SearchView searchView;

    private static UI ui = new UI(); //initialise in onCreate?

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sirch_main);

        // Ads
        AdView mAdView;
        MobileAds.initialize(this, "redacted");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Context context = this;
        UI.setmContext(new WeakReference<>(context));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        GlobalConfig.setStrNickname(SP.getString("pref_nickname", getString(R.string.pref_def_nickname)));
        GlobalConfig.setStrAltNickname(SP.getString("pref_altnickname", getString(R.string.pref_def_altnickname)));

        boolean bIniateOnSearch = SP.getBoolean("pref_iniateonsearch", false);

        String strEnvDl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        String sPrefDlFolder = "pref_dlfolder";

        if (SP.contains(sPrefDlFolder)) {
            String sSetFolder = SP.getString(sPrefDlFolder, strEnvDl);
            File f = new File(sSetFolder);
            if (!f.exists() || !f.isDirectory()) SP.edit().putString(sPrefDlFolder, strEnvDl).apply();
        }
        else SP.edit().putString(sPrefDlFolder, strEnvDl).apply();

        GlobalConfig.setStrDlFolder(SP.getString(sPrefDlFolder, strEnvDl));

        int versionCode = BuildConfig.VERSION_CODE;

        if ((!SP.contains("key_lastversion")) || (SP.getInt("key_lastversion", 0) < versionCode)) {
            SP.edit().putInt("key_lastversion", versionCode).apply();
            UI.dialogInfo(getString(R.string.first_title), getString(R.string.first_msg));
        }

        // TODO CHECK WIFI/NET STATE !!!!
        ui.initialize();

        //DEBUG Server
        //if (BuildConfig.DEBUG)
            GlobalConfig.getScServers().add(
                    new ServerConfig("debugserverhostname.com", 6667,
                            new RoomConfig("#mp3", "@find")
                    ));

        //TODO: Make server config
        //else
            GlobalConfig.getScServers().add(
                new ServerConfig("us.undernet.org", 6667,
                        new RoomConfig("#mp3passion", "@find"),
                        new RoomConfig("#mp3download", "@find"),
                        new RoomConfig("#mp3quebec", "@find")
                ));

        if ((isNicksValid()) && (requestAccessStoragePermission(true)) && (!bIniateOnSearch) && (UI.isbInitConf())) {
            GlobalConfig.startHandlers();
        }



        mAdView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest;
        if (BuildConfig.DEBUG) adRequest = new AdRequest.Builder()
                .addTestDevice("redacted")
                .addTestDevice("redacted")
                .build();
        else adRequest = new AdRequest.Builder().build();

        mAdView.loadAd(adRequest);

    }

    private final int REQUEST_PERMISSION_ACCESS_STORAGE=1;

    private boolean requestAccessStoragePermission(boolean bShowRationale) {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (bShowRationale)) {
                showExplanation("Storage Write Permission Needed", "This app cannot function without permission to write to your device's storage. Grant permission and try again.");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_ACCESS_STORAGE);
            }
            return false;
        } else {
            Toast.makeText(UI.getmContext().get(), "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private void showExplanation(String strTitle, String strRationale) {
        new AlertDialog.Builder(this)
                .setTitle(strTitle)
                .setMessage(strRationale)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestAccessStoragePermission(false);
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(UI.getmContext().get(), "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UI.getmContext().get(), "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sirch_main, menu);


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        if (null != searchView) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                //Here u can get the value "query" which is entered in the search box.
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(UI.getmContext().get());

                if (SP.getBoolean("pref_clearonsearch", true))
                    ui.clearResult();

                if (!GlobalConfig.isbHdlStarted()) {
                    if ((isNicksValid()) && (requestAccessStoragePermission(true))) {
                        GlobalConfig.startHandlers();
                        UI.delayedSearch(query);
                        Toast.makeText(UI.getmContext().get(), getString(R.string.delayed_connect), Toast.LENGTH_SHORT).show();
                    }
                }
                else
                    UI.searchSend(query);

                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.onActionViewCollapsed();
                //searchView.setIconified(true);

                //mViewPager = (ViewPager) findViewById(R.id.container);
                mViewPager.setCurrentItem(0);

                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.action_clearsearch) {
            //if (mViewPager.getCurrentItem() == 0) TODO tab awareness clear? clear completed downloads only
            //TODO : clear results in ServerConfig
            ui.clearResult();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public boolean isNicksValid() {
        if ((GlobalConfig.getStrNickname().compareToIgnoreCase(getString(R.string.pref_def_nickname)) == 0) ||
                (GlobalConfig.getStrAltNickname().compareToIgnoreCase(getString(R.string.pref_def_altnickname)) == 0)) {
            UI.dialogSettingsInvalid(getString(R.string.dialog_title_nick), getString(R.string.dialog_msg_defnick));
            return false;
        }
        else if ((!GlobalConfig.isNickValid(GlobalConfig.getStrNickname())) || (!GlobalConfig.isNickValid(GlobalConfig.getStrAltNickname()))) {
            UI.dialogSettingsInvalid(getString(R.string.dialog_title_nick), getString(R.string.dialog_msg_invalidnick));
            return false;
        }
        return true;
    }

    public void close() {
        System.out.println("MAIN CLOSE");
        GlobalConfig.clear();
        finish();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_results);
                case 1:
                    return getString(R.string.tab_downloads);
                /*case 2:
                    return "DEBUG";*/
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView;

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1: //Search
                    rootView = inflater.inflate(R.layout.fragment_main_tabs_ui, container, false);
                    ListView listViewResults = rootView.findViewById(R.id.listResults);

                    ui.setLvResults(listViewResults,this.getContext());
                    break;

                case 2: //Downloads
                default:
                    rootView = inflater.inflate(R.layout.fragment_downloads_tabs_ui, container, false);
                    ListView listViewDownloads = rootView.findViewById(R.id.listDownloads);

                    ui.setLvDownloads(listViewDownloads,this.getContext());

                    break;

                /*case 3: //Debug

                    rootView = inflater.inflate(R.layout.fragment_debug_tabs_ui, container, false);

                    //UI.setDisplayText((TextView)rootView.findViewById(R.id.debugText));
                    //UI.setSvScrollView((ScrollView)rootView.findViewById(R.id.scrView));
                    break;*/
            }
            //UI.setmContext(new WeakReference<>(rootView.getContext()));
            return rootView;
        }
    }
}
