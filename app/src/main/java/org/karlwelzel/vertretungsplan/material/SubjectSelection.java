package org.karlwelzel.vertretungsplan.material;

import android.util.Log;

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

    public static ArrayList<String> subjectSelectionNames(File dirPath) {
        File parentPath = dirPath.getParentFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(parentPath, SUBJECT_SELECTION_ORDER_FILE_NAME)));
            JSONArray array = new JSONArray(reader.readLine());
            reader.close();
            ArrayList<String> list = new ArrayList<>();
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

    public static SubjectSelection loadFromFile(File dirPath, String name) throws IOException, JSONException {
        File file = getFile(dirPath, name);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        SubjectSelection r = new SubjectSelection(name, reader.readLine());
        reader.close();
        return r;
    }

    public void saveToFile(File dirPath) throws IOException {
        File file = getFile(dirPath, name);
        if (!dirPath.exists()) dirPath.mkdirs();
        if (!file.exists()) file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(toString());
        writer.close();
        Log.d("SubjectSelection", "savedToFile: " + toString());
    }

    public SubjectSelection(String name, String json) throws JSONException {
        super(json);
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
