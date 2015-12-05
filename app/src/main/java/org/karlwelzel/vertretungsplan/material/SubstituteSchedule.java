package org.karlwelzel.vertretungsplan.material;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Karl on 06.10.2015.
 */
public class SubstituteSchedule extends JSONObject { //represents the whole substitute schedule
    public String json;
    public String banner;
    public JSONObject news;
    public JSONObject entries;
    public ArrayList<String> date_strings = new ArrayList<>();
    public ArrayList<Date> dates = new ArrayList<>();

    public static Date getLastModifiedDate(File dirPath) {
        File file = new File(dirPath, "SubstituteSchedule.json");
        return new Date(file.lastModified());
    }

    public static SubstituteSchedule loadFromFile(File dirPath) throws JSONException, ParseException, IOException {
        File file = new File(dirPath, "SubstituteSchedule.json");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        SubstituteSchedule r = new SubstituteSchedule(reader.readLine());
        reader.close();
        return r;
    }

    public void saveToFile(File dirPath) throws IOException {
        File file = new File(dirPath, "SubstituteSchedule.json");
        if (!dirPath.exists()) dirPath.mkdirs();
        if (!file.exists()) file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(json);
        writer.close();
    }

    public SubstituteSchedule(String json) throws JSONException, ParseException {
        super(json);
        this.json = json;

        banner = getString("banner");

        entries = getJSONObject("eintraege");
        Iterator<String> datesIterator = entries.keys();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        while (datesIterator.hasNext()) {
            String date_string = datesIterator.next();
            date_strings.add(date_string);
            Date date = dateFormat.parse(date_string);
            dates.add(date);
        }
        Collections.sort(date_strings);
        Collections.sort(dates);
        for (String date : date_strings) {
            SubstituteScheduleDay day = new SubstituteScheduleDay(entries.getString(date));
            try {
                day.setNews(getJSONObject("news").getString(date));
            } catch (JSONException e) {
                day.setNews(null);
            }
            entries.put(date, day);
        }
    }

    public boolean hasBanner() {
        return !(banner == null || banner.equals(""));
    }

    public ArrayList<String> getSubstituteScheduleEntries() throws JSONException {
        ArrayList<String> result = new ArrayList<>();
        String date = date_strings.get(0); //only first day
        result.addAll(((SubstituteScheduleDay) this.get(date)).getSubstituteScheduleEntries());
        return result;
    }
}
