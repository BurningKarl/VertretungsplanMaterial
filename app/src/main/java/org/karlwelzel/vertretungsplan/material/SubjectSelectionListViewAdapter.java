package org.karlwelzel.vertretungsplan.material;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Karl on 31.10.2015.
 */
public class SubjectSelectionListViewAdapter extends ArrayAdapter<String> implements AbsListView.MultiChoiceModeListener {

    private LayoutInflater inflater;

    private static int fieldId = android.R.id.text1;

    private static int resource = R.layout.substitute_schedule_list_item;

    private ActionMode actionMode;

    private ActionBar actionBar;

    private DeletionListener deletionListener;

    public ArrayList<Integer> selected = new ArrayList<>();

    private int counter = 0;

    SubjectSelection subjectSelection;

    public interface DeletionListener {
        void onItemDeleted(String item);
    }

    private int getColor(int id) {
        return getContext().getResources().getColor(id);
    }

    private void setItems(ArrayList<String> list) {
        clear();
        addAll(list);
    }

    public void updateItems() {
        Log.d("SubjSelecnAdapter", "updateItems");
        ArrayList<String> list = new ArrayList<>();
        list.add(getContext().getResources().getString(R.string.grade) + ": " + subjectSelection.getGrade());
        for (Subject subject : subjectSelection.subjects) {
            list.add(subject.toString());
        }
        setItems(list);
    }

    public SubjectSelectionListViewAdapter(Context context, ActionBar actionBar, SubjectSelection subjectSelection) {
        super(context, resource, fieldId);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.actionBar = actionBar;
        this.subjectSelection = subjectSelection;
        updateItems();
    }


    public void setDeletionListener(DeletionListener deletionListener) {
        this.deletionListener = deletionListener;
    }

    @Override
    public int getItemViewType(int position) {
        return selected.contains(Integer.valueOf(position)) ? 1 : 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        TextView text;

        try {
            if (convertView == null) {
                view = inflater.inflate(resource, parent, false);
                text = (TextView) view.findViewById(fieldId);
                switch (getItemViewType(position)) {
                    case 1: //checked
                        view.setBackgroundColor(getColor(R.color.other_entry_background_color));
                        break;

                    default: //not checked
                        view.setBackgroundColor(getColor(android.R.color.transparent));
                        break;
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

    @Override
    public boolean isEnabled(int position) {
        return position != 0; //!(actionMode != null && position == 0);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        // Here you can do something when items are selected/de-selected,
        // such as update the title in the CAB
        if (checked) {
            selected.add(position);
        } else {
            selected.remove(Integer.valueOf(position));
        }
        notifyDataSetChanged();
        counter += checked ? 1 : -1;
        mode.setTitle("" + counter);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate the menu for the CAB
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_subject_selection_overview_context, menu);
        actionBar.hide();
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Respond to clicks on the actions in the CAB
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (deletionListener == null) {
                    Log.e("SubjectSelectionAdapter", "You must set the DeletionListener before " +
                            "deleting selected list items.");
                    mode.finish();
                    return true;
                }
                ArrayList<String> deletionItems = new ArrayList<>();
                for (Integer position : selected) {
                    deletionItems.add(getItem(position));
                    deletionListener.onItemDeleted(getItem(position));
                }
                for (String deletionItem : deletionItems) {
                    remove(deletionItem);
                }
                mode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // Here you can make any necessary updates to the activity when
        // the CAB is removed. By default, selected items are deselected/unchecked.
        counter = 0;
        selected.clear();
        actionBar.show();
        actionMode = null;
        notifyDataSetChanged();
    }
}
