package org.karlwelzel.vertretungsplan.material;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    private SubstituteScheduleAlarmReceiver alarm = new SubstituteScheduleAlarmReceiver();
    private Toolbar toolbar;
    private Menu toolbarMenu;
    private boolean bannerMenuItemVisible;
    private Spinner spinner;
    private ListView listView1;
    private ListView listView2;
    private TextView emptyListView1;
    private TextView emptyListView2;
    private SwipeRefreshLayout swipeRefresh1;
    private SwipeRefreshLayout swipeRefresh2;
    private SubstituteScheduleTabHost tabHost;
    private SubstituteSchedule substituteSchedule;
    private SubstituteScheduleListViewAdapter substituteScheduleListViewAdapter1;
    private SubstituteScheduleListViewAdapter substituteScheduleListViewAdapter2;
    private JsonHttpResponseHandler responseHandler;

    private SharedPreferences preferences;
    private Intent serviceIntent;

    private Snackbar makeSnackbar(@StringRes int resId) {
        return Snackbar.make(findViewById(R.id.snackbarPosition), resId, Snackbar.LENGTH_LONG);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setBannerMenuItemVisible(boolean value) {
        bannerMenuItemVisible = value;
        if (toolbarMenu != null) {
            toolbarMenu.findItem(R.id.action_banner).setVisible(value);
        }
        if (bannerMenuItemVisible) {
            openBannerPopup();
        }
    }

    private void setSubstituteSchedule(SubstituteSchedule substituteSchedule, boolean showSnackbar) {
        if (showSnackbar) {
            makeSnackbar(R.string.download_successful).show();
        }
        emptyListView1.setText(R.string.no_entries);
        //emptyListView1.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        emptyListView2.setText(R.string.no_entries);
        //emptyListView2.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        try {
            MainActivity.this.substituteSchedule = substituteSchedule;
            tabHost.setSubstituteSchedule(substituteSchedule);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            substituteSchedule.saveToFile(getExternalFilesDir(null));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setBannerMenuItemVisible(substituteSchedule.hasBanner());
    }

    private void setSubstituteScheduleFromFile(boolean showSnackbar) {
        Snackbar snackbar;
        try {
            substituteSchedule = SubstituteSchedule.loadFromFile(getExternalFilesDir(null));
            tabHost.setSubstituteSchedule(substituteSchedule);
            snackbar = makeSnackbar(R.string.download_failed_load_from_cache);
            emptyListView1.setText(R.string.no_entries);
            emptyListView1.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            emptyListView2.setText(R.string.no_entries);
            emptyListView2.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        } catch (JSONException | IOException | ParseException e) {
            //Fataler Fehler
            e.printStackTrace();
            snackbar = makeSnackbar(R.string.download_failed_no_cache);
            emptyListView1.setText(R.string.no_entries_no_connection);
            emptyListView1.setText(R.string.no_entries_no_connection);
            emptyListView2.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            emptyListView2.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        }
        if (showSnackbar) snackbar.show();

        setBannerMenuItemVisible(substituteSchedule.hasBanner());
    }

    private void setSubstituteScheduleFromFile() {
        setSubstituteScheduleFromFile(true);
    }

    private void setSubstituteSchedule(SubstituteSchedule substituteSchedule) {
        setSubstituteSchedule(substituteSchedule, true);
    }

    private void refresh() {
        Log.d("MainActivity", "refresh");
        if (responseHandler == null) {
            responseHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("MainActivity", "onSuccess");
                    try {
                        setSubstituteSchedule(new SubstituteSchedule(response.getString("json")));
//                        setSubstituteSchedule(new SubstituteSchedule("{\"news\": {\"2015-10-15\": \"Nur ein paar News\"}, \"banner\": \"Nur ein Banner\", \"eintraege\": {\"2015-10-14\": {\"Q1\": [{\"fach\": \"GE L1\", \"art\": \"Raum-Vtr.\", \"stunde\": \"3\", \"vertreter\": \"KOR\", \"kfach\": \"GE L1\", \"klehrer\": \"KOR\", \"klasse\": \"Q1\", \"raum\": \"A-K04\"}], \"6A\": [{\"fach\": null, \"art\": \"Entfall\", \"stunde\": \"1\", \"vertreter\": null, \"kfach\": \"SP\", \"klehrer\": \"H\\u00dcW\", \"klasse\": \"6A\", \"raum\": null}], \"9B\": [{\"fach\": \"M\", \"art\": \"Statt-Vertretung\", \"stunde\": \"3\", \"vertreter\": \"HEY\", \"kfach\": \"M\", \"klehrer\": \"VUK\", \"klasse\": \"9B\", \"raum\": \"A-K13\"}], \"7A\": [{\"fach\": \"GE\", \"art\": \"Statt-Vertretung\", \"stunde\": \"2\", \"vertreter\": \"BEC\", \"kfach\": \"GE\", \"klehrer\": \"K\\u00d6N\", \"klasse\": \"7A\", \"raum\": \"A-K04\"}, {\"fach\": \"ITG\", \"art\": \"Vertretung\", \"stunde\": \"3\", \"vertreter\": \"REI\", \"kfach\": \"ITG\", \"klehrer\": \"K\\u00d6V\", \"klasse\": \"7A\", \"raum\": \"B-F1\"}, {\"fach\": \"MU\", \"art\": \"Statt-Vertretung\", \"stunde\": \"4\", \"vertreter\": \"NIE\", \"kfach\": \"MU\", \"klehrer\": \"H\\u00dcW\", \"klasse\": \"7A\", \"raum\": \"B-04\"}], \"8B\": [{\"fach\": \"WP2-SoW\", \"art\": \"Vertretung\", \"stunde\": \"4\", \"vertreter\": \"ECK\", \"kfach\": \"WP2-SoW\", \"klehrer\": \"K\\u00d6N\", \"klasse\": \"8A, 8B\", \"raum\": \"A-K04\"}, {\"fach\": null, \"art\": \"Entfall\", \"stunde\": \"5\", \"vertreter\": null, \"kfach\": \"POL\", \"klehrer\": \"K\\u00d6N\", \"klasse\": \"8B\", \"raum\": null}], \"8A\": [{\"fach\": \"WP2-SoW\", \"art\": \"Vertretung\", \"stunde\": \"4\", \"vertreter\": \"ECK\", \"kfach\": \"WP2-SoW\", \"klehrer\": \"K\\u00d6N\", \"klasse\": \"8A, 8B\", \"raum\": \"A-K04\"}], \"9A\": [{\"fach\": \"SP\", \"art\": \"Statt-Vertretung\", \"stunde\": \"3\", \"vertreter\": \"EUS\", \"kfach\": \"SP\", \"klehrer\": \"H\\u00dcW\", \"klasse\": \"9A\", \"raum\": \"SP2\"}], \"7T\": [{\"fach\": \"M\", \"art\": \"Vertretung\", \"stunde\": \"2\", \"vertreter\": \"REI\", \"kfach\": \"M\", \"klehrer\": \"VUK\", \"klasse\": \"7T\", \"raum\": \"A-K13\"}], \"9T\": [{\"fach\": \"POL\", \"art\": \"Statt-Vertretung\", \"stunde\": \"1\", \"vertreter\": \"CAR\", \"kfach\": \"POL\", \"klehrer\": \"K\\u00d6N\", \"klasse\": \"9T\", \"raum\": \"A-K04\"}, {\"fach\": \"SP\", \"art\": \"Statt-Vertretung\", \"stunde\": \"3\", \"vertreter\": \"CAR\", \"kfach\": \"SP\", \"klehrer\": \"KRU\", \"klasse\": \"9T\", \"raum\": \"SP1\"}, {\"fach\": \"ITG\", \"art\": \"Statt-Vertretung\", \"stunde\": \"4\", \"vertreter\": \"MEN\", \"kfach\": \"ITG\", \"klehrer\": \"K\\u00d6V\", \"klasse\": \"9T\", \"raum\": \"B-29\"}]}, \"2015-10-15\": {\"Q1\":[{\"fach\": null, \"art\": \"Entfall\", \"stunde\": \"2\", \"vertreter\": null, \"kfach\": \"E5eL1\", \"klehrer\": \"HAR\", \"klasse\": \"Q1\", \"raum\": null}], \"9C\":[{\"fach\": \"GE L1\", \"art\": \"Raum-Vtr.\", \"stunde\": \"3\", \"vertreter\": \"KOR\", \"kfach\": \"GE L1\", \"klehrer\": \"KOR\", \"klasse\": \"9C\", \"raum\": \"A-K04\"}]}}}"));
                    } catch (JSONException | ParseException e) {
                        setSubstituteScheduleFromFile();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                    Log.d("MainActivity", "onFailure");
                    Log.d("HTTP", "Status code: " + statusCode);
                    if (object != null) {
                        Log.d("HTTP", "Response: '" + object.toString() + "'");
                    }
                    setSubstituteScheduleFromFile();
                    throwable.printStackTrace();
                }

                @Override
                public void onFinish() {
                    Log.d("MainActivity", "onFinish");
                    swipeRefresh1.setRefreshing(false);
                    swipeRefresh2.setRefreshing(false);
                }
            };
        }
        if (isNetworkAvailable()) {
            swipeRefresh1.setRefreshing(true);
            swipeRefresh2.setRefreshing(true);
            ParseRestClient.getSubstituteSchedule(this, responseHandler);
        } else {
            setSubstituteScheduleFromFile();
        }
    }

    private void openBannerPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(substituteSchedule.banner)
                .setTitle(R.string.banner)
                .setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void openInfoPopup() {
        Date lastModified = SubstituteSchedule.getLastModifiedDate(getExternalFilesDir(null));
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        new AlertDialog.Builder(this)
                .setMessage(String.format(getResources().getString(R.string.info_message), dateFormat.format(lastModified), dateFormat.format(substituteSchedule.updatedAt)))
                .setTitle(R.string.info)
                .setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    public File getSubjectSelectionDir() {
        return getExternalFilesDir(SubjectSelection.SUBJECT_SELECTION_DIR_NAME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("MainActivity", "onCreate");

        listView1 = (ListView) findViewById(R.id.listView1);
        emptyListView1 = (TextView) findViewById(R.id.emptyListView1);
        emptyListView1.setText(R.string.loading);
        listView1.setEmptyView(emptyListView1);
        listView2 = (ListView) findViewById(R.id.listView2);
        emptyListView2 = (TextView) findViewById(R.id.emptyListView2);
        emptyListView2.setText(R.string.loading);
        listView2.setEmptyView(emptyListView2);

        substituteScheduleListViewAdapter1 = new SubstituteScheduleListViewAdapter(MainActivity.this, getSubjectSelectionDir());
        listView1.setAdapter(substituteScheduleListViewAdapter1);
        substituteScheduleListViewAdapter2 = new SubstituteScheduleListViewAdapter(MainActivity.this, getSubjectSelectionDir());
        listView2.setAdapter(substituteScheduleListViewAdapter2);

        spinner = (Spinner) findViewById(R.id.spinner);
        SubstituteScheduleSpinnerAdapter substituteScheduleSpinnerAdapter = new SubstituteScheduleSpinnerAdapter(this, substituteScheduleListViewAdapter1, substituteScheduleListViewAdapter2);
        substituteScheduleSpinnerAdapter.setSubjectSelectionNames(SubjectSelection.subjectSelectionNames(getSubjectSelectionDir()));
        spinner.setAdapter(substituteScheduleSpinnerAdapter);
        spinner.setOnItemSelectedListener(substituteScheduleSpinnerAdapter);

        swipeRefresh1 = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh1);
        swipeRefresh2 = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh2);
        swipeRefresh1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swipeRefresh2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        //fix bug with swipeRefresher1
        listView1.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listView1 == null || listView1.getChildCount() == 0) ?
                                0 : listView1.getChildAt(0).getTop();
                if (swipeRefresh1 != null) {
                    swipeRefresh1.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                }
            }
        });
        //fix bug with swipeRefresher2
        listView2.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listView2 == null || listView2.getChildCount() == 0) ?
                                0 : listView2.getChildAt(0).getTop();
                if (swipeRefresh2 != null) {
                    swipeRefresh2.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                }
            }
        });

        tabHost = (SubstituteScheduleTabHost) findViewById(R.id.tabHost);
        tabHost.setup(substituteScheduleListViewAdapter1, substituteScheduleListViewAdapter2);

        if (savedInstanceState == null) {
            if (SubstituteSchedule.getLastModifiedDate(getExternalFilesDir(null)).after(new Date((new Date()).getTime() - 3 * 60 * 1000))) { //newer than 3min
                Log.d("MainActivity", "substitute schedule loaded from file, because it was downloaded 5min ago");
                setSubstituteScheduleFromFile(false);
            } else {
                refresh();
            }
        } else {
            Log.d("MainActivity", "substitute schedule loaded from savedInstanceState");
            try {
                substituteSchedule = new SubstituteSchedule(savedInstanceState.getString("substitute_schedule"));
                setSubstituteSchedule(substituteSchedule, false);
            } catch (JSONException | ParseException | NullPointerException e) {
                e.printStackTrace();
                refresh();
            }
            spinner.setSelection(savedInstanceState.getInt("selected_spinner_position"));
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        alarm.setAlarm(this);
/*
        serviceIntent = new Intent(this, SubstituteScheduleNotificationService.class);
        startService(serviceIntent);
*/
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (substituteSchedule != null) {
            savedInstanceState.putString("substitute_schedule", substituteSchedule.json);
        }
        if (spinner != null) {
            savedInstanceState.putInt("selected_spinner_position", spinner.getSelectedItemPosition());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        toolbarMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        setBannerMenuItemVisible(bannerMenuItemVisible); //when the toolbarMenu is initialized, do what should have been done before
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_banner:
                openBannerPopup();
                return true;

            case R.id.action_info:
                openInfoPopup();
                return true;

            case R.id.action_refresh:
                refresh();
                return true;

            case R.id.action_subject_selection:
                startActivity(new Intent(this, SubjectSelectionOverviewActivity.class));
                return true;

/*
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
*/

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
