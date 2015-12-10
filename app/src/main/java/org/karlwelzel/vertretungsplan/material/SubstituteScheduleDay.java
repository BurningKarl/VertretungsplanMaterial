package org.karlwelzel.vertretungsplan.material;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Karl on 06.10.2015.
 */
public class SubstituteScheduleDay extends JSONObject { //represents one day of the substitute schedule
    public String news_title = "";
    public String news = "";
    public ArrayList<String> grades = new ArrayList<>();

    public SubstituteScheduleDay(String json) throws JSONException {
        super(json);
        Iterator<String> gradesIterator = this.keys();
        while (gradesIterator.hasNext()) {
            grades.add(gradesIterator.next());
        }
        Collections.sort(grades);
        for (String grade : grades) {
            ArrayList<SubstituteScheduleEntry> entries = new ArrayList<>();
            JSONArray jEntries = new JSONArray(this.getString(grade));
            for (int i = 0; i < jEntries.length(); i++) {
                entries.add(new SubstituteScheduleEntry(jEntries.getString(i)));
            }
            this.put(grade, entries);
        }
    }

    public void setNews(String news_json) {
        try {
            JSONArray array = new JSONArray(news_json);
            news_title = array.getString(0);
            news = array.getString(1);
        } catch (JSONException | NullPointerException e) {
            news_title = "";
            news = "";
        }
    }

    public boolean hasNews() {
        return news_title.length() != 0 && news.length() != 0;
    }

    public ArrayList<String> getSubstituteScheduleEntries() throws JSONException {
        ArrayList<SubstituteScheduleEntry> entries;
        ArrayList<String> result = new ArrayList<>();

/*
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("'Zuletzt aktualisiert:' d.M.yyyy H:mm 'Uhr'");
        result.add(simpleDateFormat.format(new Date()));
*/
        if (hasNews()) {
            result.add(news_title + ":");
            result.add(news);
        }
        for (String curGrade : grades) {
            entries = (ArrayList<SubstituteScheduleEntry>) this.get(curGrade);
            if (entries.size() > 0) {
                result.add(curGrade + ":");
                for (SubstituteScheduleEntry entry : entries) {
                    result.add(entry.toString());
                }
            }
        }
        return result;
    }

    public ArrayList<String> getSubstituteScheduleEntries(String grade, boolean add_news) throws JSONException {
        ArrayList<SubstituteScheduleEntry> entries;
        ArrayList<String> result = new ArrayList<>();

        if (add_news && hasNews()) {
            result.add(news_title + ":");
            result.add(news);
        }
        for (String curGrade : grades) {
            if (curGrade.equals(grade)) {
                entries = (ArrayList<SubstituteScheduleEntry>) this.get(curGrade);
                if (entries.size() > 0) {
                    result.add(grade + ":");
                    for (SubstituteScheduleEntry entry : entries) {
                        result.add(entry.toString());
                    }
                }
                break;
            }
        }
        return result;
    }

    public ArrayList<String> getSubstituteScheduleEntries(String grade) throws JSONException {
        return getSubstituteScheduleEntries(grade, true);
    }

    public ArrayList<String> getFilteredSubstituteScheduleEntries(SubjectSelection subjectSelection) throws JSONException {
        String grade = subjectSelection.getGrade();
        ArrayList<SubstituteScheduleEntry> entries;
        ArrayList<String> result = new ArrayList<>();

        if (hasNews()) {
            result.add(news_title + ":");
            result.add(news);
        }
        ArrayList<String> subjects = new ArrayList<>();
        for (Subject subj : subjectSelection.subjects) {
            subjects.add(subj.get("subject") + " " + subj.get("course"));
        }
        for (String curGrade : grades) {
            if (curGrade.equals(grade)) {
                entries = (ArrayList<SubstituteScheduleEntry>) this.get(curGrade);
                ArrayList<SubstituteScheduleEntry> interestingEntries = new ArrayList<>();
                for (SubstituteScheduleEntry entry : entries) {
                    if (subjects.contains(entry.subjectAndCourse())) {
                        interestingEntries.add(entry);
                    }
                }
                for (SubstituteScheduleEntry entry : interestingEntries) {
                    result.add(entry.toString());
                }
                break;
            }
        }
        return result;
    }
}
