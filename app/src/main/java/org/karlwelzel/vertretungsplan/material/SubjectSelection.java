package org.karlwelzel.vertretungsplan.material;

import android.content.Context;
import android.util.Log;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Karl on 12.10.2015.
 */
public class SubjectSelection extends JSONObject {

    public static String SUBJECT_SELECTION_DIR_NAME = "Subject selection";
    public static String SUBJECT_SELECTION_ORDER_FILE_NAME = "SubjectSelectionOrder.json";

    public String name;
    public ArrayList<Subject> subjects = new ArrayList<>();

    public static File getFile(File dirPath, String name) {
        return new File(dirPath, name + ".json");
    }

    public static File getSubjectSelectionDir(Context context) {
        return context.getExternalFilesDir(SUBJECT_SELECTION_DIR_NAME);
    }

    public static void updateSubjectSelectionNames(Context context, ArrayList<String> names) {
        File dirPath = context.getExternalFilesDir(null);
        Log.d("SubjectSelection", "updateSubjectSelectionNames: "+dirPath.toString());
        Log.d("SubjectSelection", "updateSubjectSelectionNames: "+names.size());
        try {
            File file = new File(dirPath, SUBJECT_SELECTION_ORDER_FILE_NAME);
            if (!dirPath.exists()) dirPath.mkdirs();
            if (!file.exists()) file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            JSONArray array = new JSONArray(names);
            Log.d("SubjectSelection", "updateSubjectSelectionNames: "+array.toString());
            writer.write(array.toString());
            writer.close();
            Log.d("SubjectSelection", "updateSubjectSelectionNames: "+getSubjectSelectionNames(context).size());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void addSubjectSelectionName(Context context, String name) {
        ArrayList<String> names = getSubjectSelectionNames(context);
        names.add(name);
        updateSubjectSelectionNames(context, names);
    }

    public static ArrayList<String> getSubjectSelectionNames(Context context) {
        File dirPath = context.getExternalFilesDir(null);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(dirPath, SUBJECT_SELECTION_ORDER_FILE_NAME)));
            JSONArray array = new JSONArray(reader.readLine());
            reader.close();
            ArrayList<String> list = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
            return list;
        } catch (IOException | JSONException e) {
/*
            File[] files = dirPath.listFiles();
            ArrayList<String> list = new ArrayList<>();
            for (File file : files) {
                list.add(file.getName().replace(".json", ""));
            }
            return list;
*/
            return new ArrayList<>();
        }
    }

    public static void deleteSubjectSelection(Context context, String name) {
        File file = SubjectSelection.getFile(getSubjectSelectionDir(context), name);
        if (!file.delete()) {
            Log.e("SubjectSelection", "Failed to delete " + file.toString());
        }
    }

    public static SubjectSelection loadFromFile(Context context, String name) throws IOException, JSONException {
        File file = getFile(getSubjectSelectionDir(context), name);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        SubjectSelection r = new SubjectSelection(name, reader.readLine());
        reader.close();
        return r;
    }

    public void saveToFile(Context context) throws IOException {
        File dirPath = getSubjectSelectionDir(context);
        File file = getFile(dirPath, name);
        if (!dirPath.exists()) dirPath.mkdirs();
        if (!file.exists()) file.createNewFile();
        if (!getSubjectSelectionNames(context).contains(name)) {
            addSubjectSelectionName(context, name);
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(toString());
        writer.close();
        Log.d("SubjectSelection", "savedToFile: " + toString());
    }

    public SubjectSelection(String name, String json) throws JSONException {
        super(StringEscapeUtils.unescapeJson(json));
        this.name = name;
        JSONArray subjectsArray = new JSONArray(getString("subjects"));
        for (int i = 0; i < subjectsArray.length(); i++) {
            subjects.add(new Subject(subjectsArray.getString(i)));
        }
    }

    public SubjectSelection(String name) {
        super();
        this.name = name;
        try {
            put("subjects", "[]");
            put("grade", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getGrade() {
        try {
            String grade = getString("grade");
            return grade.equals("") ? null : grade;
        } catch (JSONException e) {
            return null;
        }
    }

    public void setGrade(String value) throws JSONException { //this will reset all subject values, be careful
        put("grade", value);
        subjects.clear();
    }

    public boolean removeSubjectString(String subjectString) {
        String subject = subjectString.split(" ")[0];
        for (Subject subj : subjects) {
            if (subj.get("subject").equals(subject)) {
                subjects.remove(subj);
                return true;
            }
        }
        return false;
    }

    private void writeTo(JSONStringer stringer) throws JSONException {
        stringer.object();
        for (Iterator<String> it = keys(); it.hasNext(); ) {
            String key = it.next();
            stringer.key(key);
            if (key.equals("subjects")) {
                stringer.value(new JSONArray(subjects));
            } else {
                stringer.value(get(key));
            }
        }
        stringer.endObject();
    }

    @Override
    public String toString() {
        try {
            JSONStringer stringer = new JSONStringer();
            writeTo(stringer);
            return stringer.toString();
        } catch (JSONException e) {
            return null;
        }
    }
}
