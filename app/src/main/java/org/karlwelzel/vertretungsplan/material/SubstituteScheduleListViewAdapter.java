package org.karlwelzel.vertretungsplan.material;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Karl on 06.10.2015.
 */
public class SubstituteScheduleListViewAdapter extends ArrayAdapter<String> {

    private LayoutInflater inflater;

    private SubstituteScheduleDay day;

    private File subjectSelectionDir;

    private static int fieldId = android.R.id.text1;

    private static int resource = R.layout.substitute_schedule_list_item;

    private String filter;

    private void setItems(ArrayList<String> items) {
        clear();
        addAll(items);
    }

    private int getColor(int id) {
        return getContext().getResources().getColor(id);
    }

    public SubstituteScheduleListViewAdapter(Context context, File subjectSelectionDir) {
        super(context, resource, fieldId);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.subjectSelectionDir = subjectSelectionDir;
    }

    public void setSubstituteScheduleDay(SubstituteScheduleDay day) {
        this.day = day;
        if (day == null) {
            setItems(new ArrayList<String>());
        } else if (filter == null) {
            try {
                setItems(this.day.getSubstituteScheduleEntries());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            setFilter(filter);
        }
    }

    public boolean substituteScheduleDaySet() { //checks if substitute schedule is set
        return this.day != null;
    }

    public ArrayList<String> gradesInSubjectSelections() {
        ArrayList<String> results = new ArrayList<>();
        for (String name : SubjectSelection.subjectSelectionNames(subjectSelectionDir)) {
            try {
                String grade = SubjectSelection.loadFromFile(subjectSelectionDir, name).getGrade();
                if (!results.contains(grade)) {
                    results.add(grade);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        if (day == null) {
            setItems(new ArrayList<String>());
            return;
        }
        try {
            if (filter.equals("Alle")) {
                setItems(day.getSubstituteScheduleEntries());
            } else if (filter.equals(getContext().getResources().getQuantityString(R.plurals.grade, 1))
                    || filter.equals(getContext().getResources().getQuantityString(R.plurals.grade, 2))) {
                ArrayList<String> items = new ArrayList<>();
                boolean first = true;
                for (String grade : gradesInSubjectSelections()) {
                    items.addAll(day.getSubstituteScheduleEntries(grade, first));
                    first = false;
                }
                setItems(items);
            } else { //filter is subject selection
                try {
                    setItems(day.getFilteredSubstituteScheduleEntries(SubjectSelection.loadFromFile(subjectSelectionDir, filter)));
                } catch (IOException e) {
                    e.printStackTrace();
                    setItems(day.getSubstituteScheduleEntries());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        String item = getItem(position);

        if (position == 1 && day.hasNews()) {
            return 1;
        } else if (item.endsWith(":")) {
            return 2;
        } else if (item.startsWith("Entfall")) {
            return 3;
        } else if (item.startsWith("Vertretung")) {
            return 4;
        } else if (item.startsWith("Klausur")) {
            return 5;
        } else {
            return 0;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 6;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        TextView text;

        try {
            if (convertView == null) {
                view = inflater.inflate(resource, parent, false);
                text = (TextView) view.findViewById(fieldId);
                switch (getItemViewType(position)) {
                    case 1: //news
                        text.setGravity(Gravity.CENTER);
                        text.setBackgroundColor(getColor(R.color.normal_background_color));
                        break;
                    case 2: //header
                        text.setTypeface(Typeface.DEFAULT_BOLD);
                        text.setBackgroundColor(getColor(R.color.normal_background_color));
                        break;
                    case 3: //cancellation
                        text.setBackgroundColor(getColor(R.color.cancellation_background_color));
                        break;
                    case 4: //substitution
                        text.setBackgroundColor(getColor(R.color.substitution_background_color));
                        break;
                    case 5: //exam
                        text.setBackgroundColor(getColor(R.color.exam_background_color));
                        break;
                    default:
                        text.setBackgroundColor(getColor(R.color.other_entry_background_color));
                }
            } else {
                view = convertView;
                text = (TextView) view.findViewById(fieldId);
            }
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        String item = getItem(position);
        text.setText(item);

        return view;
    }
}
