package sms.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Timetable {
    private int id;
    private String classId;
    private String day;
    private String timeSlot;
    private String subjectCode;

    public Timetable() {}

    public Timetable(int id, String classId, String day, String timeSlot, String subjectCode) {
        this.id = id;
        this.classId = classId;
        this.day = day;
        this.timeSlot = timeSlot;
        this.subjectCode = subjectCode;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("class_id", classId);
        map.put("day", day);
        map.put("time_slot", timeSlot);
        map.put("subject_code", subjectCode);
        return map;
    }
}
