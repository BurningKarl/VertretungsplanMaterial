package org.karlwelzel.vertretungsplan.material;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Karl on 12.10.2015.
 */
public class Subject extends HashMap<String, String> {

    public static String subjectToString(String subject, String course, String teacher) {
        return subject + " " + (course == null || course.equals("null") ? "" : (course + " ")) + teacher;
    }

    public static String classToString(String course, String teacher) {
        return (course == null || course.equals("null") ? "" : (course + " ")) + teacher;
    }

    public static String classToString(JSONArray array) throws JSONException {
        return classToString(array.getString(0), array.getString(1));
    }

    public Subject(String json) throws JSONException {
        super();
        JSONObject jsonObject = new JSONObject(json);
        for (Iterator keys = jsonObject.keys(); keys.hasNext();) {
            String key = (String) keys.next();
            this.put(key, jsonObject.getString(key));
        }
    }

    public Subject(String subject, String course, String teacher) {
        super();
        put("subject", subject);
        put("course", course);
        put("teacher", teacher);
    }

    public String toString() {
        return subjectToString(get("subject"), get("course"), get("teacher"));
    }
}

