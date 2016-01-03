package org.karlwelzel.vertretungsplan.material;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Karl on 15.10.2015.
 */
public class SubjectSelectionActivity extends AppCompatActivity {

    public final static String EXTRA_NAME = "org.karlwelzel.vertretungsplantest.NAME";

    public JsonHttpResponseHandler responseHandler;

    public SubjectSelectionData subjectSelectionData;

    public String name;
    public SubjectSelection subjectSelection;

    public SubjectSelectionListViewAdapter adapter;

    private Snackbar makeSnackbar(@StringRes int resId) {
        return Snackbar.make(findViewById(R.id.snackbarPosition), resId, Snackbar.LENGTH_LONG);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void refresh() {
        Log.d("SubjectSelectionAct.", "refresh");
        if (responseHandler == null) {
            responseHandler = new JsonHttpResponseHandler() {
                private void setSubjectSelectionData(SubjectSelectionData data) {
                    makeSnackbar(R.string.download_successful).show();

                    try {
                        data.saveToFile(getExternalFilesDir(null));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    SubjectSelectionActivity.this.setSubjectSelectionData(data);
                }

                private void setSubjectSelectionDataFromFile() {
                    try {
                        SubjectSelectionData data = SubjectSelectionData.loadFromFile(getExternalFilesDir(null));
                        makeSnackbar(R.string.download_failed_load_subject_selection_from_cache).show();
                        SubjectSelectionActivity.this.setSubjectSelectionData(data);
                    } catch (JSONException | IOException | ParseException e) {
                        //Fatal Error
                        e.printStackTrace();
                        makeSnackbar(R.string.download_failed_no_cache).show();
                    }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("SubjectSelectionAct.", "onSuccess");
                    try {
                        setSubjectSelectionData(new SubjectSelectionData(response.getString("json")));
//                        setSubstituteSchedule(new SubstituteSchedule("{\"news\": {\"2015-10-15\": \"Nur ein paar News\"}, \"banner\": \"Nur ein Banner\", \"eintraege\": {\"2015-10-14\": {\"Q1\": [{\"fach\": \"GE L1\", \"art\": \"Raum-Vtr.\", \"stunde\": \"3\", \"vertreter\": \"KOR\", \"kfach\": \"GE L1\", \"klehrer\": \"KOR\", \"klasse\": \"Q1\", \"raum\": \"A-K04\"}], \"6A\": [{\"fach\": null, \"art\": \"Entfall\", \"stunde\": \"1\", \"vertreter\": null, \"kfach\": \"SP\", \"klehrer\": \"H\\u00dcW\", \"klasse\": \"6A\", \"raum\": null}], \"9B\": [{\"fach\": \"M\", \"art\": \"Statt-Vertretung\", \"stunde\": \"3\", \"vertreter\": \"HEY\", \"kfach\": \"M\", \"klehrer\": \"VUK\", \"klasse\": \"9B\", \"raum\": \"A-K13\"}], \"7A\": [{\"fach\": \"GE\", \"art\": \"Statt-Vertretung\", \"stunde\": \"2\", \"vertreter\": \"BEC\", \"kfach\": \"GE\", \"klehrer\": \"K\\u00d6N\", \"klasse\": \"7A\", \"raum\": \"A-K04\"}, {\"fach\": \"ITG\", \"art\": \"Vertretung\", \"stunde\": \"3\", \"vertreter\": \"REI\", \"kfach\": \"ITG\", \"klehrer\": \"K\\u00d6V\", \"klasse\": \"7A\", \"raum\": \"B-F1\"}, {\"fach\": \"MU\", \"art\": \"Statt-Vertretung\", \"stunde\": \"4\", \"vertreter\": \"NIE\", \"kfach\": \"MU\", \"klehrer\": \"H\\u00dcW\", \"klasse\": \"7A\", \"raum\": \"B-04\"}], \"8B\": [{\"fach\": \"WP2-SoW\", \"art\": \"Vertretung\", \"stunde\": \"4\", \"vertreter\": \"ECK\", \"kfach\": \"WP2-SoW\", \"klehrer\": \"K\\u00d6N\", \"klasse\": \"8A, 8B\", \"raum\": \"A-K04\"}, {\"fach\": null, \"art\": \"Entfall\", \"stunde\": \"5\", \"vertreter\": null, \"kfach\": \"POL\", \"klehrer\": \"K\\u00d6N\", \"klasse\": \"8B\", \"raum\": null}], \"8A\": [{\"fach\": \"WP2-SoW\", \"art\": \"Vertretung\", \"stunde\": \"4\", \"vertreter\": \"ECK\", \"kfach\": \"WP2-SoW\", \"klehrer\": \"K\\u00d6N\", \"klasse\": \"8A, 8B\", \"raum\": \"A-K04\"}], \"9A\": [{\"fach\": \"SP\", \"art\": \"Statt-Vertretung\", \"stunde\": \"3\", \"vertreter\": \"EUS\", \"kfach\": \"SP\", \"klehrer\": \"H\\u00dcW\", \"klasse\": \"9A\", \"raum\": \"SP2\"}], \"7T\": [{\"fach\": \"M\", \"art\": \"Vertretung\", \"stunde\": \"2\", \"vertreter\": \"REI\", \"kfach\": \"M\", \"klehrer\": \"VUK\", \"klasse\": \"7T\", \"raum\": \"A-K13\"}], \"9T\": [{\"fach\": \"POL\", \"art\": \"Statt-Vertretung\", \"stunde\": \"1\", \"vertreter\": \"CAR\", \"kfach\": \"POL\", \"klehrer\": \"K\\u00d6N\", \"klasse\": \"9T\", \"raum\": \"A-K04\"}, {\"fach\": \"SP\", \"art\": \"Statt-Vertretung\", \"stunde\": \"3\", \"vertreter\": \"CAR\", \"kfach\": \"SP\", \"klehrer\": \"KRU\", \"klasse\": \"9T\", \"raum\": \"SP1\"}, {\"fach\": \"ITG\", \"art\": \"Statt-Vertretung\", \"stunde\": \"4\", \"vertreter\": \"MEN\", \"kfach\": \"ITG\", \"klehrer\": \"K\\u00d6V\", \"klasse\": \"9T\", \"raum\": \"B-29\"}]}, \"2015-10-15\": {\"Q1\":[{\"fach\": null, \"art\": \"Entfall\", \"stunde\": \"2\", \"vertreter\": null, \"kfach\": \"E5eL1\", \"klehrer\": \"HAR\", \"klasse\": \"Q1\", \"raum\": null}], \"9C\":[{\"fach\": \"GE L1\", \"art\": \"Raum-Vtr.\", \"stunde\": \"3\", \"vertreter\": \"KOR\", \"kfach\": \"GE L1\", \"klehrer\": \"KOR\", \"klasse\": \"9C\", \"raum\": \"A-K04\"}]}}}"));
                    } catch (JSONException e) {
                        setSubjectSelectionDataFromFile();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                    Log.d("SubjectSelectionAct.", "onFailure");
                    Log.d("HTTP", "Status code: " + statusCode);
                    if (object != null) {
                        Log.d("HTTP", "Response: '" + object.toString() + "'");
                    }
                    throwable.printStackTrace();
                    setSubjectSelectionDataFromFile();
                }

                public void onFinish() {
                    Log.d("SubjectSelectionAct.", "onFinish");
                }
            };
        }
        if (isNetworkAvailable()) {
            ParseRestClient.getSubjectSelectionData(this, responseHandler);
        } else {
            try {
                SubjectSelectionData data = SubjectSelectionData.loadFromFile(getExternalFilesDir(null));
                makeSnackbar(R.string.download_failed_load_subject_selection_from_cache).show();
                SubjectSelectionActivity.this.setSubjectSelectionData(data);
            } catch (JSONException | IOException | ParseException e) {
                //Fatal Error
                e.printStackTrace();
                makeSnackbar(R.string.download_failed_no_cache).show();
            }
        }
    }

    private void openNotDownloadedPopup() {
        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(R.string.subject_selection_data_not_downloaded_yet_title)
                .setMessage(R.string.subject_selection_data_not_downloaded_yet_message)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        b.show();
    }

    private void openGradePopup() {
        if (subjectSelectionData == null) {
            openNotDownloadedPopup();
            return;
        }

        String curGrade = subjectSelection.getGrade();

        ArrayList<String> grades = subjectSelectionData.getGrades();

        int itemPosition = (curGrade == null) ? 0 : grades.indexOf(curGrade);

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(R.string.grade)
                .setCancelable(curGrade != null)
                .setSingleChoiceItems(grades.toArray(new String[]{}), itemPosition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        dialog.dismiss();
                        try {
                            subjectSelection.setGrade(subjectSelectionData.getGrades().get(position));
                            subjectSelection.saveToFile(getSubjectSelectionDir());
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                        adapter.updateItems();
                        Log.d("SubjectSelectionAct.", subjectSelectionData.getGrades().get(position));
                    }
                });

        b.show();
    }

    private void openSelectCoursePopup(final String subject) {
        try {
            if (subjectSelectionData == null) {
                openNotDownloadedPopup();
                return;
            }

            final JSONArray classes = subjectSelectionData.getClassesOfSubject(subjectSelection.getGrade(), subject);

            final ArrayList<String> stringClasses = new ArrayList<>();
            for (int i = 0; i < classes.length(); i++) {
                stringClasses.add(Subject.classToString(classes.getJSONArray(i)));
            }

            AlertDialog.Builder b = new AlertDialog.Builder(this)
                    .setTitle(subject)
                    .setSingleChoiceItems(stringClasses.toArray(new String[]{}), 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            dialog.dismiss();
                            try {
                                JSONArray class_ = classes.getJSONArray(position);
                                Log.d("SubjectSelectionAct.", stringClasses.get(position));
                                subjectSelection.subjects.add(new Subject(subject, class_.getString(0), class_.getString(1)));
                                try {
                                    subjectSelection.saveToFile(getSubjectSelectionDir());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                adapter.updateItems();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            b.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openAddSubjectPopup() {
        try {
            ArrayList<String> usedSubjects = new ArrayList<>();
            for (Subject sub : subjectSelection.subjects) {
                usedSubjects.add(sub.get("subject"));
            }
            
            final ArrayList<String> subjects = subjectSelectionData.getSubjectsOfGrade(subjectSelection.getGrade(), usedSubjects);

            AlertDialog.Builder b = new AlertDialog.Builder(this)
                    .setTitle(R.string.subject)
                    .setSingleChoiceItems(subjects.toArray(new String[]{}), 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            dialog.dismiss();
                            Log.d("SubjectSelectionAct.", subjects.get(position));
                            openSelectCoursePopup(subjects.get(position));
                        }
                    });
            b.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public File getSubjectSelectionDir() {
        return getExternalFilesDir(SubjectSelection.SUBJECT_SELECTION_DIR_NAME);
    }

    void setSubjectSelectionData(SubjectSelectionData data) {
        subjectSelectionData = data;

        try {
            Log.d("SubjectSelectionAct.", subjectSelectionData.getGrades().toString());
            Log.d("SubjectSelectionAct.", subjectSelectionData.getSubjectsOfGrade("Q1").toString());
            Log.d("SubjectSelectionAct.", subjectSelectionData.getClassesOfSubject("Q1", "Me").getJSONArray(0).getString(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (subjectSelection.getGrade() == null) {
            openGradePopup();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_selection);

        Log.d("SubjectSelectionAct.", "onCreate");

        name = getIntent().getStringExtra(SubjectSelectionOverviewActivity.EXTRA_NAME);
        try {
            subjectSelection = SubjectSelection.loadFromFile(getSubjectSelectionDir(), name);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            subjectSelection = new SubjectSelection(name);
        }
        Log.d("SubjectSelectionAct.", subjectSelection.toString());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        ListView listView = (ListView) findViewById(R.id.subject_selection_list_view);
        adapter = new SubjectSelectionListViewAdapter(this, getSupportActionBar(), subjectSelection);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    openGradePopup();
                } else {
                    openSelectCoursePopup(adapter.getItem(position).split(" ")[0]);
                }
            }
        });
        listView.setMultiChoiceModeListener(adapter);
        adapter.setDeletionListener(new SubjectSelectionListViewAdapter.DeletionListener() {
            @Override
            public void onItemDeleted(String item) {
                if (!subjectSelection.removeSubjectString(item)) {
                    Log.e("SubjectSelectionAct.", item + " could not be deleted");
                } else {
                    try {
                        subjectSelection.saveToFile(getSubjectSelectionDir());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_subject_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_add:
                openAddSubjectPopup();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
