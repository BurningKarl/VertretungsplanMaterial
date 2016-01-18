package org.karlwelzel.vertretungsplan.material;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Karl on 03.10.2015.
 */
public class SubstituteScheduleEntry extends JSONObject { //represents one substitute schedule entry
    //example: {"fach": "GE", "art": "Statt-Vertretung", "stunde": "2", "vertreter": "BEC", "kfach": "GE", "klehrer": "K\u00d6N", "klasse": "7A", "raum": "A-K04"}
    public String grade;          //0
    public String lesson;         //1
    public String room;           //2
    public String type;           //3
    public String bracketSubject; //4
    public String subject;        //5
    public String bracketTeacher; //6
    public String substitute;     //7

    public SubstituteScheduleEntry(String json) throws JSONException {
        super(StringEscapeUtils.unescapeJson(json));
        grade = replaceIfNull(this.getString("klasse"), "???");
        lesson = replaceIfNull(this.getString("stunde"), "???");
        room = replaceIfNull(this.getString("raum"), "???");
        type = replaceIfNull(this.getString("art"), "???");
        bracketSubject = replaceIfNull(this.getString("kfach"), "???");
        subject = replaceIfNull(this.getString("fach"), "???");
        bracketTeacher = replaceIfNull(this.getString("klehrer"), "???");
        substitute = replaceIfNull(this.getString("vertreter"), "???");
    }

    private String replaceIfNull(String string, String replace) {
        return string == null || string == "null" ? replace : string;
    }

    public String subjectAndCourse() {
        return bracketSubject.equals("???") ? subject : bracketSubject;
    }

    public String toString() {
        //TODO: replace with string resource
        if (type.equals("Entfall"))
            return String.format("%1$s von %2$s bei %3$s in der %5$s. Stunde", type, bracketSubject.equals("???") ? subject : bracketSubject, substitute.equals("???") ? bracketTeacher : substitute, room, lesson);
        else
            return String.format("%1$s im Fach %2$s mit %3$s im Raum %4$s in der %5$s. Stunde", type, bracketSubject.equals("???") ? subject : bracketSubject, substitute.equals("???") ? bracketTeacher : substitute, room, lesson);
    }
}
