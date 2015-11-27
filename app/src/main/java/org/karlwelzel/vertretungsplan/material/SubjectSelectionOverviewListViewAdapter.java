package org.karlwelzel.vertretungsplan.material;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
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
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Karl on 5.11.2015.
 */
public class SubjectSelectionOverviewListViewAdapter extends ArrayAdapter<String> implements AbsListView.MultiChoiceModeListener {

    private LayoutInflater inflater;

    private static int fieldId = android.R.id.text1;

    private static int resource = R.layout.substitute_schedule_list_item;

    private ActionBar actionBar;

    private DeletionListener deletionListener;

    public ArrayList<Integer> selected = new ArrayList<>();

    private int counter = 0;

    public interface DeletionListener {
        void onItemDeleted(String item);
    }

    private int getColor(int id) {
        return getContext().getResources().getColor(id);
    }

    private void updateSubjectSelectionNameOrderFile() {
        try {
            File dirPath = getContext().getExternalFilesDir(null);
            File file = new File(dirPath, SubjectSelection.SUBJECT_SELECTION_ORDER_FILE_NAME);
            if (!dirPath.exists()) dirPath.mkdirs();
            if (!file.exists()) file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            JSONArray array = new JSONArray();
            for (int i = 0; i < getCount(); i++) {
                array.put(i, getItem(i));
            }
            writer.write(array.toString());
            writer.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public SubjectSelectionOverviewListViewAdapter(Context context, ActionBar actionBar) {
        super(context, resource, fieldId);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.actionBar = actionBar;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        updateSubjectSelectionNameOrderFile();
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

        mode.invalidate();
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
        counter = 0;
        menu.findItem(R.id.action_rearrange).setVisible(selected.size() == 1);
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        // Respond to clicks on the actions in the CAB
        final ActionMode actionMode = mode;
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (deletionListener == null) {
                    Log.e("SubjectSelectOvAdapter", "You must set the DeletionListener before " +
                            "deleting selected list items.");
                    return true;
                }
                if (!selected.isEmpty()) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.are_you_sure)
                            .setMessage(getContext().getResources().getQuantityString(R.plurals.are_you_sure_delete_subject_selection, selected.size()))
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ArrayList<String> deletionItems = new ArrayList<>();
                                    for (Integer position : selected) {
                                        deletionItems.add(getItem(position));
                                    }
                                    for (String deletionItem : deletionItems) {
                                        deletionListener.onItemDeleted(deletionItem);
                                        remove(deletionItem);
                                    }
                                    actionMode.finish(); // Action picked, so close the CAB
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    actionMode.finish(); // Action picked, so close the CAB
                                    dialog.cancel();
                                }
                            }).show();
                }
                return true;
            case R.id.action_rearrange:
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setFilters(new InputFilter[]{
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                Log.d("SubjectSelectionOv.", "InputFilter: " + input.getText() + source);
                                int index;
                                try {
                                    index = Integer.parseInt(input.getText() + source.toString()) - 1;
                                } catch (NumberFormatException e) {
                                    return "";
                                }
                                if (0 <= index && index <= getCount() - 1) {
                                    return source;
                                } else {
                                    return "";
                                }
                            }
                        }
                });
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.rearrange)
                        .setMessage(R.string.rearrange_message)
                        .setView(input)
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                int index;
                                try {
                                    index = Integer.parseInt(input.getText().toString()) - 1;
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    return;
                                }
                                String subjSelection = getItem(selected.get(0));
                                remove(subjSelection);
                                insert(subjSelection, index);
                                actionMode.finish();
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                actionMode.finish();
                            }
                        }).show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // Here you can make any necessary updates to the activity when
        // the CAB is removed. By default, selected items are deselected/unchecked.
        selected.clear();
        actionBar.show();
    }
}
