package sms.service;

import sms.dao.ClassDAO;
import sms.dao.CourseDAO;
import sms.dao.SubjectDAO;
import sms.dao.TimetableDAO;
import sms.model.ClassModel;
import sms.model.Course;
import sms.model.Subject;
import sms.model.Timetable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AcademicService {
    private final CourseDAO courseDAO = new CourseDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();

    // Courses
    public List<Course> getAllCourses() { return courseDAO.getAllCourses(); }

    public Map<String, Object> addCourse(Course c) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (c == null || c.getCode() == null || c.getCode().trim().isEmpty() || c.getName() == null || c.getName().trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Course code and course name are required.");
            return res;
        }
        boolean ok = courseDAO.addCourse(c);
        res.put("success", ok);
        res.put("message", ok ? "Course added successfully." : "Course code already exists or database error.");
        return res;
    }

    public Map<String, Object> deleteCourse(String code) {
        Map<String, Object> res = new LinkedHashMap<>();
        boolean ok = courseDAO.deleteCourse(code);
        res.put("success", ok);
        res.put("message", ok ? "Course deleted successfully." : "Failed to delete course.");
        return res;
    }

    // Subjects
    public List<Subject> getAllSubjects() { return subjectDAO.getAllSubjects(); }

    public Map<String, Object> addSubject(Subject s) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (s == null || s.getCode() == null || s.getCode().trim().isEmpty() || s.getName() == null || s.getName().trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Subject code and subject name are required.");
            return res;
        }
        boolean ok = subjectDAO.addSubject(s);
        res.put("success", ok);
        res.put("message", ok ? "Subject added successfully." : "Subject code already exists or database error.");
        return res;
    }

    public Map<String, Object> deleteSubject(String code) {
        Map<String, Object> res = new LinkedHashMap<>();
        boolean ok = subjectDAO.deleteSubject(code);
        res.put("success", ok);
        res.put("message", ok ? "Subject deleted successfully." : "Failed to delete subject.");
        return res;
    }

    // Classes
    public List<ClassModel> getAllClasses() { return classDAO.getAllClasses(); }

    public Map<String, Object> addClass(ClassModel cm) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (cm == null || cm.getClassId() == null || cm.getClassId().trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Class ID is required (e.g. BCA-3A).");
            return res;
        }
        boolean ok = classDAO.addClass(cm);
        res.put("success", ok);
        res.put("message", ok ? "Class registered successfully." : "Class ID already exists or database error.");
        return res;
    }

    public Map<String, Object> deleteClass(String classId) {
        Map<String, Object> res = new LinkedHashMap<>();
        boolean ok = classDAO.deleteClass(classId);
        res.put("success", ok);
        res.put("message", ok ? "Class deleted successfully." : "Failed to delete class.");
        return res;
    }

    // Timetables
    public List<Timetable> getAllTimetables() { return timetableDAO.getAllTimetables(); }

    public Map<String, Object> addTimetable(Timetable t) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (t == null || t.getClassId() == null || t.getClassId().trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Class ID, Day, and Subject are required.");
            return res;
        }
        boolean ok = timetableDAO.addTimetable(t);
        res.put("success", ok);
        res.put("message", ok ? "Timetable slot added successfully." : "Failed to add timetable slot.");
        return res;
    }

    public Map<String, Object> deleteTimetable(int id) {
        Map<String, Object> res = new LinkedHashMap<>();
        boolean ok = timetableDAO.deleteTimetable(id);
        res.put("success", ok);
        res.put("message", ok ? "Timetable slot deleted." : "Failed to delete timetable slot.");
        return res;
    }
}
