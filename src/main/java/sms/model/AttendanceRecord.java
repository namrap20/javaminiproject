package sms.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttendanceRecord {
    private int id;
    private String studentId;
    private String studentName;
    private String date;
    private String status;

    public AttendanceRecord() {}

    public AttendanceRecord(int id, String studentId, String studentName, String date, String status) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.date = date;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("student_id", studentId);
        map.put("name", studentName != null ? studentName : studentId);
        map.put("date", date);
        map.put("status", status);
        return map;
    }
}
