package sms.service;

import sms.dao.StudentDAO;
import sms.model.Student;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentService {
    private final StudentDAO studentDAO = new StudentDAO();

    public List<Student> getAllStudents() {
        return studentDAO.getAllStudents();
    }

    public Student getStudentById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        return studentDAO.getStudentById(id.trim());
    }

    public Map<String, Object> addStudent(Student s) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (s == null) {
            res.put("success", false);
            res.put("message", "Invalid student data.");
            return res;
        }

        // Validate mandatory fields
        if (isEmpty(s.getId())) {
            res.put("success", false);
            res.put("message", "Student ID is required (e.g. STU121).");
            return res;
        }
        if (isEmpty(s.getName())) {
            res.put("success", false);
            res.put("message", "Full Name is required.");
            return res;
        }
        if (isEmpty(s.getGrNo())) {
            res.put("success", false);
            res.put("message", "GR Number is required.");
            return res;
        }
        if (isEmpty(s.getRollNo())) {
            res.put("success", false);
            res.put("message", "Roll Number is required.");
            return res;
        }

        // Check if student ID already exists
        Student existing = studentDAO.getStudentById(s.getId().trim());
        if (existing != null) {
            res.put("success", false);
            res.put("message", "A student with ID '" + s.getId() + "' already exists in the database.");
            return res;
        }

        // Validate semester range
        if (s.getSemester() < 1 || s.getSemester() > 10) {
            s.setSemester(1);
        }

        // Validate Admission Year
        if (s.getAdmissionYear() < 2000 || s.getAdmissionYear() > 2030) {
            s.setAdmissionYear(2024);
        }

        boolean saved = studentDAO.addStudent(s);
        if (saved) {
            res.put("success", true);
            res.put("message", "Student added successfully.");
            res.put("student", s.toMap());
        } else {
            res.put("success", false);
            res.put("message", "Unable to save student. Please verify that GR Number or Roll Number is not a duplicate.");
        }
        return res;
    }

    public Map<String, Object> updateStudent(Student s) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (s == null || isEmpty(s.getId())) {
            res.put("success", false);
            res.put("message", "Valid Student ID is required to update.");
            return res;
        }

        Student existing = studentDAO.getStudentById(s.getId().trim());
        if (existing == null) {
            res.put("success", false);
            res.put("message", "Student not found with ID: " + s.getId());
            return res;
        }

        if (isEmpty(s.getName())) s.setName(existing.getName());
        if (isEmpty(s.getGrNo())) s.setGrNo(existing.getGrNo());
        if (isEmpty(s.getRollNo())) s.setRollNo(existing.getRollNo());
        if (isEmpty(s.getCourse())) s.setCourse(existing.getCourse());
        if (s.getSemester() < 1) s.setSemester(existing.getSemester());
        if (isEmpty(s.getDivision())) s.setDivision(existing.getDivision());
        if (s.getAdmissionYear() < 2000) s.setAdmissionYear(existing.getAdmissionYear());

        boolean updated = studentDAO.updateStudent(s);
        if (updated) {
            res.put("success", true);
            res.put("message", "Student updated successfully.");
            res.put("student", s.toMap());
        } else {
            res.put("success", false);
            res.put("message", "Failed to update student in database. Check constraints.");
        }
        return res;
    }

    public Map<String, Object> deleteStudent(String id) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (isEmpty(id)) {
            res.put("success", false);
            res.put("message", "Student ID is required.");
            return res;
        }

        boolean deleted = studentDAO.deleteStudent(id.trim());
        if (deleted) {
            res.put("success", true);
            res.put("message", "Student deleted successfully.");
        } else {
            res.put("success", false);
            res.put("message", "Failed to delete student. Record not found or foreign key restriction.");
        }
        return res;
    }

    public List<Student> searchStudents(String field, String val) {
        if (val == null || val.trim().isEmpty()) {
            return studentDAO.getAllStudents();
        }
        return studentDAO.searchStudents(field, val.trim());
    }

    public List<Student> globalSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return studentDAO.getAllStudents();
        }
        return studentDAO.globalSearch(query.trim());
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
