package org.karlwelzel.vertretungsplan.material;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Created by Karl on 12.10.2015.
 */
public class SubjectSelectionOverviewActivity extends AppCompatActivity {

    public final static String EXTRA_NAME = "org.karlwelzel.vertretungsplan.material.NAME";

    public Toolbar toolbar;
    public ListView listView;
    public SubjectSelectionOverviewListViewAdapter listViewAdapter;

    private void editSubjectSelection(String name) {
        Log.d("SubjectSelectionOv.", "editSubjectSelection: " + name);
        Intent intent = new Intent(this, SubjectSelectionActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        startActivity(intent);
    }

    private void editSubjectSelection(int position) {
        editSubjectSelection(listViewAdapter.getItem(position));
    }

    private void deleteSubjectSelection(String name) {
        Log.d("SubjectSelectionOv.", "deleteSubjectSelection: " + name);
        listViewAdapter.remove(name); //This automatically deletes the SubjectSelection from SubjectSelectionOrder.json
        SubjectSelection.deleteSubjectSelection(this, name);
    }

    private void deleteSubjectSelection(int position) {
        deleteSubjectSelection(listViewAdapter.getItem(position));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_selection_overview);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        Log.d("SubjectSelectionOv.", "onCreate");

        final ActionBar actionBar = SubjectSelectionOverviewActivity.this.getSupportActionBar();
        listView = (ListView) findViewById(R.id.subjectSelectionListView);
        listViewAdapter = new SubjectSelectionOverviewListViewAdapter(this, actionBar);
        listView.setAdapter(listViewAdapter);
        listView.setMultiChoiceModeListener(listViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editSubjectSelection(position);
            }
        });
        listViewAdapter.setDeletionListener(new SubjectSelectionOverviewListViewAdapter.DeletionListener() {
            @Override
            public void onItemDeleted(String item) {
                deleteSubjectSelection(item);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("SubjectSelectionOv.", "onResume");

        listViewAdapter.setNotifyOnChange(false);
        listViewAdapter.clear();
        listViewAdapter.setNotifyOnChange(true);
        listViewAdapter.addAll(SubjectSelection.getSubjectSelectionNames(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_subject_selection_overview, menu);

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
                Log.d("SubjectSelectionOv.", "add SubjectSelection");
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                input.setFilters(new InputFilter[]{
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                if (source.equals("")) {
                                    return "";
                                } else if (source.toString().matches("[a-zA-Z]+")) {
                                    return source;
                                } else {
                                    return "";
                                }
                            }
                        }
                });
                new AlertDialog.Builder(this)
                        .setTitle(R.string.add_subject_selection)
                        .setView(input)
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                String name = input.getText().toString();
                                editSubjectSelection(name);
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
