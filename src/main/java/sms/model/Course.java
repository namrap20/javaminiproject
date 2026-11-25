package sms.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Course {
    private String code;
    private String name;
    private int durationYears;
    private int totalSemesters;

    public Course() {}

    public Course(String code, String name, int durationYears, int totalSemesters) {
        this.code = code;
        this.name = name;
        this.durationYears = durationYears;
        this.totalSemesters = totalSemesters;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDurationYears() { return durationYears; }
    public void setDurationYears(int durationYears) { this.durationYears = durationYears; }

    public int getTotalSemesters() { return totalSemesters; }
    public void setTotalSemesters(int totalSemesters) { this.totalSemesters = totalSemesters; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", code);
        map.put("name", name);
        map.put("duration_years", durationYears);
        map.put("total_semesters", totalSemesters);
        return map;
    }
}
