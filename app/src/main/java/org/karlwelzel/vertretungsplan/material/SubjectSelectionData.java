package org.karlwelzel.vertretungsplan.material;

import android.content.Context;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by Karl on 24.10.2015.
 */
public class SubjectSelectionData extends JSONObject {

    public String json;

    public static SubjectSelectionData loadFromFile(Context context) throws JSONException, ParseException, IOException {
        File file = new File(context.getExternalFilesDir(null), "SubjectSelectionData.json");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        SubjectSelectionData r = new SubjectSelectionData(reader.readLine());
        reader.close();
        return r;
    }

    public void saveToFile(Context context) throws IOException {
        File dirPath = context.getExternalFilesDir(null);
        File file = new File(dirPath, "SubjectSelectionData.json");
        if (!dirPath.exists()) dirPath.mkdirs();
        if (!file.exists()) file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(json);
        writer.close();
    }

    public SubjectSelectionData(String json) throws JSONException {
        super(StringEscapeUtils.unescapeJson(json));
        this.json = json;
    }

    private ArrayList<String> convertToList(Iterator<String> it) {
        ArrayList<String> arrayList = new ArrayList<>();
        while (it.hasNext()) {
            arrayList.add(it.next());
        }
        return arrayList;
    }

    public ArrayList<String> getGrades() {
        ArrayList<String> r = convertToList(this.keys());
        Collections.sort(r, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                Collator collator = Collator.getInstance();
                char lhs_first_char = lhs.charAt(0);
                char rhs_first_char = rhs.charAt(0);
                boolean lhs_starts_with_digits = '0' <= lhs_first_char && lhs_first_char <= '9';
                boolean rhs_starts_with_digits = '0' <= rhs_first_char && rhs_first_char <= '9';
                if (lhs_starts_with_digits && rhs_starts_with_digits) {
                    if (lhs.length() == rhs.length()) {
                        return collator.compare(lhs, rhs);
                    } else {
                        return lhs.length() - rhs.length();
                    }
                } else if (lhs_starts_with_digits) {
                    return -1;
                } else if (rhs_starts_with_digits) {
                    return 1;
                } else {
                    return collator.compare(lhs, rhs);
                }
            }
        });
        return r;
    }

    public ArrayList<String> getSubjectsOfGrade(String grade, ArrayList<String> exclude) throws JSONException {
        ArrayList<String> r = convertToList((new JSONObject(getString(grade))).keys());
        r.removeAll(exclude);
        Collections.sort(r);
        return r;
    }

    public ArrayList<String> getSubjectsOfGrade(String grade) throws JSONException {
        return getSubjectsOfGrade(grade, new ArrayList<String>());
    }

    public JSONArray getClassesOfSubject(String grade, String subject) throws JSONException {
        JSONArray jsonArray = new JSONArray((new JSONObject(getString(grade))).getString(subject));
        for (int i = 0; i < jsonArray.length(); i++) {
            jsonArray.put(i, new JSONArray(jsonArray.getString(i)));
        }
        return new JSONArray((new JSONObject(getString(grade))).getString(subject));
    }
}
