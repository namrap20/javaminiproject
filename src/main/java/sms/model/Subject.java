package sms.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Subject {
    private String code;
    private String name;
    private String courseCode;
    private int semester;
    private int credits;

    public Subject() {}

    public Subject(String code, String name, String courseCode, int semester, int credits) {
        this.code = code;
        this.name = name;
        this.courseCode = courseCode;
        this.semester = semester;
        this.credits = credits;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", code);
        map.put("name", name);
        map.put("course_code", courseCode);
        map.put("semester", semester);
        map.put("credits", credits);
        return map;
    }
}
