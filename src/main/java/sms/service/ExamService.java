package sms.service;

import sms.dao.ExamResultDAO;
import sms.dao.StudentDAO;
import sms.dao.SubjectDAO;
import sms.model.ExamResult;
import sms.model.Student;
import sms.model.Subject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExamService {
    private final ExamResultDAO examResultDAO = new ExamResultDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    public List<ExamResult> getAllResults() {
        return examResultDAO.getAllExamResults();
    }

    public List<ExamResult> getResultsByStudent(String studentId) {
        return examResultDAO.getResultsByStudent(studentId);
    }

    public Map<String, Object> addResult(String studentId, String subjectCode, double marks, double total) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (studentId == null || studentId.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Student ID is required.");
            return res;
        }
        if (subjectCode == null || subjectCode.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Subject Code is required.");
            return res;
        }
        if (total <= 0) {
            res.put("success", false);
            res.put("message", "Total marks must be greater than zero.");
            return res;
        }
        if (marks < 0 || marks > total) {
            res.put("success", false);
            res.put("message", "Obtained marks must be between 0 and " + total + ".");
            return res;
        }

        Student s = studentDAO.getStudentById(studentId.trim());
        if (s == null) {
            res.put("success", false);
            res.put("message", "Student '" + studentId + "' not found.");
            return res;
        }

        boolean ok = examResultDAO.addExamResult(s.getId(), subjectCode.trim(), marks, total);
        String grade = ExamResultDAO.calculateGrade(marks, total);
        res.put("success", ok);
        res.put("message", ok ? "Exam result recorded successfully. Grade: " + grade : "Failed to record exam result.");
        res.put("grade", grade);
        return res;
    }

    public Map<String, Object> updateResult(int id, double marks, double total) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (total <= 0) {
            res.put("success", false);
            res.put("message", "Total marks must be greater than zero.");
            return res;
        }
        boolean ok = examResultDAO.updateExamResult(id, marks, total);
        res.put("success", ok);
        res.put("message", ok ? "Exam result updated successfully." : "Failed to update exam result.");
        return res;
    }

    public Map<String, Object> deleteResult(int id) {
        Map<String, Object> res = new LinkedHashMap<>();
        boolean ok = examResultDAO.deleteExamResult(id);
        res.put("success", ok);
        res.put("message", ok ? "Exam result deleted." : "Failed to delete exam result.");
        return res;
    }

    public Map<String, Object> getStudentMarksheet(String studentId) {
        Map<String, Object> sheet = new LinkedHashMap<>();
        Student s = studentDAO.getStudentById(studentId);
        if (s == null) {
            sheet.put("success", false);
            sheet.put("message", "Student not found.");
            return sheet;
        }

        List<ExamResult> results = examResultDAO.getResultsByStudent(s.getId());
        double sumMarks = 0;
        double sumTotal = 0;
        List<Map<String, Object>> resultMaps = new ArrayList<>();
        for (ExamResult r : results) {
            sumMarks += r.getMarks();
            sumTotal += r.getTotal();
            resultMaps.add(r.toMap());
        }

        double overallPct = sumTotal > 0 ? (sumMarks / sumTotal) * 100.0 : 0.0;
        String overallGrade = ExamResultDAO.calculateGrade(sumMarks, sumTotal);

        sheet.put("success", true);
        sheet.put("student", s.toMap());
        sheet.put("results", resultMaps);
        sheet.put("total_marks_obtained", Math.round(sumMarks * 100.0) / 100.0);
        sheet.put("total_max_marks", Math.round(sumTotal * 100.0) / 100.0);
        sheet.put("percentage", Math.round(overallPct * 10.0) / 10.0);
        sheet.put("overall_grade", overallGrade);
        return sheet;
    }

    public Map<String, Object> getExamStats() {
        return examResultDAO.getExamStats();
    }
}
