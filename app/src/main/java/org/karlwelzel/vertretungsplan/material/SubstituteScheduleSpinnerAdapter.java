package org.karlwelzel.vertretungsplan.material;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Karl on 07.10.2015.
 */
public class SubstituteScheduleSpinnerAdapter extends ArrayAdapter<String> implements AdapterView.OnItemSelectedListener {

    private Resources resources;

    private SubstituteScheduleListViewAdapter adapter1;
    private SubstituteScheduleListViewAdapter adapter2;

    private ArrayList<String> spinnerItems = new ArrayList<>();

    private void setItems(ArrayList<String> items) {
        clear();
        addAll(items);
    }

    public SubstituteScheduleSpinnerAdapter(Context context, SubstituteScheduleListViewAdapter adapter1, SubstituteScheduleListViewAdapter adapter2) {
        super(context, android.R.layout.simple_spinner_item);
        resources = context.getResources();
        this.adapter1 = adapter1;
        this.adapter2 = adapter2;
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setSubjectSelectionNames(new ArrayList<String>());
    }

    public void setSubjectSelectionNames(ArrayList<String> names) {
        spinnerItems = new ArrayList<>();
        spinnerItems.addAll(names);
        int count = adapter1.gradesInSubjectSelections().size();
        if (count >= 1) {
            spinnerItems.add(resources.getQuantityString(R.plurals.grade, count));
        }
        spinnerItems.add(resources.getString(R.string.all));
        setItems(spinnerItems);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = spinnerItems.get(position);
        adapter1.setFilter(item);
        adapter2.setFilter(item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
