package sms.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassModel {
    private String classId;
    private String courseCode;
    private int semester;
    private String division;
    private String roomNo;

    public ClassModel() {}

    public ClassModel(String classId, String courseCode, int semester, String division, String roomNo) {
        this.classId = classId;
        this.courseCode = courseCode;
        this.semester = semester;
        this.division = division;
        this.roomNo = roomNo;
    }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("class_id", classId);
        map.put("course_code", courseCode);
        map.put("semester", semester);
        map.put("division", division);
        map.put("room_no", roomNo);
        return map;
    }
}
