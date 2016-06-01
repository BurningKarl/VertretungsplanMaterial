package org.karlwelzel.vertretungsplan.material;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.Locale;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    private SubstituteScheduleAlarmReceiver alarm = new SubstituteScheduleAlarmReceiver();
    private Toolbar toolbar;
    private Menu toolbarMenu;
    private boolean bannerMenuItemVisible;
    private Spinner spinner;
    private SubstituteScheduleSpinnerAdapter substituteScheduleSpinnerAdapter;
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
            substituteSchedule.saveToFile(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setBannerMenuItemVisible(substituteSchedule.hasBanner());
    }

    private void setSubstituteScheduleFromFile(boolean showSnackbar) {
        Snackbar snackbar;
        try {
            substituteSchedule = SubstituteSchedule.loadFromFile(this);
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
        if (responseHandler == null) responseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("MainActivity", "onSuccess");
                try {
                    Log.d("onSuccess", "status: "+response.getString("status"));
                    if (response.getString("status").equals("success")) {
                        setSubstituteSchedule(new SubstituteSchedule(response.toString()));
                    }
                    else {
                        emptyListView1.setText(R.string.wrong_login_text);
                        emptyListView1.setText(R.string.wrong_login_text);
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    }
                } catch (JSONException | ParseException e) {
                    setSubstituteScheduleFromFile();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d("MainActivity", "onFailure");
                Log.d("HTTP", "Status code: " + statusCode);
                Log.d("HTTP", "Response: '" + response + "'");
                setSubstituteScheduleFromFile();
                throwable.printStackTrace();
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
        if (isNetworkAvailable()) {
            swipeRefresh1.setRefreshing(true);
            swipeRefresh2.setRefreshing(true);
            new NetworkClient(this).getSubstituteSchedule(responseHandler);
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
        Date lastModified = SubstituteSchedule.getLastModifiedDate(this);
        DateFormat dateFormat;
        if (Locale.getDefault().getLanguage().equals("de")) {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN);
        } else {
            dateFormat = SimpleDateFormat.getDateTimeInstance();
        }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NetworkClient networkClient = new NetworkClient(this);
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

        substituteScheduleListViewAdapter1 = new SubstituteScheduleListViewAdapter(MainActivity.this);
        listView1.setAdapter(substituteScheduleListViewAdapter1);
        substituteScheduleListViewAdapter2 = new SubstituteScheduleListViewAdapter(MainActivity.this);
        listView2.setAdapter(substituteScheduleListViewAdapter2);

        spinner = (Spinner) findViewById(R.id.spinner);
        substituteScheduleSpinnerAdapter = new SubstituteScheduleSpinnerAdapter(this, substituteScheduleListViewAdapter1, substituteScheduleListViewAdapter2);
        substituteScheduleSpinnerAdapter.setNotifyOnChange(true);
        substituteScheduleSpinnerAdapter.setSubjectSelectionNames(SubjectSelection.getSubjectSelectionNames(this));
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
            if (SubstituteSchedule.getLastModifiedDate(this).after(new Date((new Date()).getTime() - 3 * 60 * 1000))) { //newer than 3min
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
    public void onResume() {
        Log.d("MainActivity", "onResume");
        super.onResume();

        substituteScheduleSpinnerAdapter.setSubjectSelectionNames(SubjectSelection.getSubjectSelectionNames(this));
    }

    @Override
    public void onStop() {
        Log.d("MainActivity", "onStop");
        super.onStop();

        try {
            Runtime.getRuntime().exec(new String[] { "logcat", "-f", (new File(Environment.getExternalStorageDirectory(), "vertretungsplan_log.txt")).toString(), "-v", "time", "ActivityManager:W", "org.karlwelzel.vertretungsplan.material:D"});
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
