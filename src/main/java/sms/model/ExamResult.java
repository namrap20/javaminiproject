package sms.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExamResult {
    private int id;
    private String studentId;
    private String studentName;
    private String subjectCode;
    private double marks;
    private double total;
    private String grade;

    public ExamResult() {}

    public ExamResult(int id, String studentId, String studentName, String subjectCode, double marks, double total, String grade) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.subjectCode = subjectCode;
        this.marks = marks;
        this.total = total;
        this.grade = grade;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public double getMarks() { return marks; }
    public void setMarks(double marks) { this.marks = marks; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("student_id", studentId);
        map.put("name", studentName != null ? studentName : studentId);
        map.put("subject", subjectCode);
        map.put("marks", marks);
        map.put("total", total);
        map.put("grade", grade);
        return map;
    }
}
