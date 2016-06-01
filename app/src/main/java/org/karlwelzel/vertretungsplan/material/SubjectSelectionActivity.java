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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Karl on 15.10.2015.
 */
public class SubjectSelectionActivity extends AppCompatActivity {

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
                        data.saveToFile(SubjectSelectionActivity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    SubjectSelectionActivity.this.setSubjectSelectionData(data);
                }

                private void setSubjectSelectionDataFromFile() {
                    try {
                        SubjectSelectionData data = SubjectSelectionData.loadFromFile(SubjectSelectionActivity.this);
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
                        setSubjectSelectionData(new SubjectSelectionData(response.toString()));
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
            new NetworkClient(this).getSubjectSelectionData(responseHandler);
        } else {
            try {
                SubjectSelectionData data = SubjectSelectionData.loadFromFile(this);
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
                            subjectSelection.saveToFile(SubjectSelectionActivity.this);
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
                                boolean replaced = false;
                                for (int i = 0; i < subjectSelection.subjects.size(); i++) {
                                    Subject curSubject = subjectSelection.subjects.get(i);
                                    if (curSubject.get("subject").equals(subject)) {
                                        if (!replaced) {
                                            subjectSelection.subjects.set(i, new Subject(subject, class_.getString(0), class_.getString(1)));
                                            replaced = true;
                                        } else {
                                            subjectSelection.subjects.remove(i);
                                            i--;
                                        }
                                    }
                                }
                                if (!replaced) {
                                    subjectSelection.subjects.add(new Subject(subject, class_.getString(0), class_.getString(1)));
                                }
                                //TODO: Place the new new subject in the same spot of the old one (if one existed)
                                try {
                                    subjectSelection.saveToFile(SubjectSelectionActivity.this);
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
            if (subjectSelectionData == null) {
                openNotDownloadedPopup();
                return;
            }

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

    void setSubjectSelectionData(SubjectSelectionData data) {
        subjectSelectionData = data;

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
            subjectSelection = SubjectSelection.loadFromFile(this, name);
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
        getSupportActionBar().setTitle(name);

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
                        subjectSelection.saveToFile(SubjectSelectionActivity.this);
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
