package sms.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Student {
    private String id;
    private String grNo;
    private String rollNo;
    private String name;
    private String dob;
    private String gender;
    private String email;
    private String phone;
    private String address;
    private String course;
    private int semester;
    private String division;
    private int admissionYear;

    public Student() {}

    public Student(String id, String grNo, String rollNo, String name, String dob,
                   String gender, String email, String phone, String address,
                   String course, int semester, String division, int admissionYear) {
        this.id = id;
        this.grNo = grNo;
        this.rollNo = rollNo;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.course = course;
        this.semester = semester;
        this.division = division;
        this.admissionYear = admissionYear;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGrNo() { return grNo; }
    public void setGrNo(String grNo) { this.grNo = grNo; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public int getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(int admissionYear) { this.admissionYear = admissionYear; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("gr_no", grNo);
        map.put("roll_no", rollNo);
        map.put("name", name);
        map.put("dob", dob);
        map.put("gender", gender);
        map.put("email", email);
        map.put("phone", phone);
        map.put("address", address);
        map.put("course", course);
        map.put("semester", semester);
        map.put("division", division);
        map.put("admission_year", admissionYear);
        return map;
    }

    public static Student fromMap(Map<String, ?> map) {
        Student s = new Student();
        s.setId(getString(map, "id"));
        s.setGrNo(getString(map, "gr_no", "grNo"));
        s.setRollNo(getString(map, "roll_no", "rollNo"));
        s.setName(getString(map, "name"));
        s.setDob(getString(map, "dob"));
        s.setGender(getString(map, "gender"));
        s.setEmail(getString(map, "email"));
        s.setPhone(getString(map, "phone"));
        s.setAddress(getString(map, "address"));
        s.setCourse(getString(map, "course"));
        s.setSemester(getInt(map, "semester", 1));
        s.setDivision(getString(map, "division"));
        s.setAdmissionYear(getInt(map, "admission_year", "admissionYear", 2024));
        return s;
    }

    private static String getString(Map<String, ?> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) return val.toString().trim();
        }
        return "";
    }

    private static int getInt(Map<String, ?> map, String key, int def) {
        Object val = map.get(key);
        if (val == null) return def;
        try {
            if (val instanceof Number) return ((Number) val).intValue();
            return Integer.parseInt(val.toString().trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static int getInt(Map<String, ?> map, String key1, String key2, int def) {
        Object val = map.get(key1);
        if (val == null) val = map.get(key2);
        if (val == null) return def;
        try {
            if (val instanceof Number) return ((Number) val).intValue();
            return Integer.parseInt(val.toString().trim());
        } catch (Exception e) {
            return def;
        }
    }
}
