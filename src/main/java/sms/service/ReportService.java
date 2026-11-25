package sms.service;

import sms.dao.*;
import sms.database.DB;
import sms.model.Course;
import sms.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ReportService {
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ExamResultDAO examResultDAO = new ExamResultDAO();

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // Real counts from DB
        int totalStudents = studentDAO.count();
        int totalCourses = courseDAO.count();
        int totalSubjects = subjectDAO.count();
        int totalClasses = classDAO.count();
        int totalAttendance = attendanceDAO.count();
        int totalExams = examResultDAO.count();

        stats.put("total_students", totalStudents);
        stats.put("total_courses", totalCourses);
        stats.put("total_subjects", totalSubjects);
        stats.put("total_classes", totalClasses);
        stats.put("total_attendance_logs", totalAttendance);
        stats.put("total_exam_records", totalExams);

        // Attendance stats
        Map<String, Object> attStats = attendanceDAO.getOverallStats();
        stats.put("attendance_percentage", attStats.getOrDefault("percentage", 0.0));
        stats.put("attendance_present", attStats.getOrDefault("present", 0));
        stats.put("attendance_absent", attStats.getOrDefault("absent", 0));

        // Exam stats
        Map<String, Object> examStats = examResultDAO.getExamStats();
        stats.put("average_exam_score", examStats.getOrDefault("average_score", 0.0));
        stats.put("grade_distribution", examStats.getOrDefault("grade_distribution", Collections.emptyMap()));

        // Course Breakdown
        List<Map<String, Object>> courseBreakdown = getCourseBreakdown();
        stats.put("course_breakdown", courseBreakdown);

        // Semester Distribution
        stats.put("semester_distribution", getSemesterDistribution());

        // Division Distribution
        stats.put("division_distribution", getDivisionDistribution());

        return stats;
    }

    public List<Map<String, Object>> getCourseBreakdown() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Course> courses = courseDAO.getAllCourses();
        for (Course c : courses) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", c.getCode());
            item.put("name", c.getName());
            int count = 0;
            String sql = "SELECT COUNT(*) FROM students WHERE course = ?";
            try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, c.getCode());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) count = rs.getInt(1);
                }
            } catch (SQLException e) {
                System.err.println("Error calculating course breakdown: " + e.getMessage());
            }
            item.put("enrolled_count", count);
            list.add(item);
        }
        return list;
    }

    public Map<String, Integer> getSemesterDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 1; i <= 8; i++) map.put("Sem " + i, 0);

        String sql = "SELECT semester, COUNT(*) as cnt FROM students GROUP BY semester";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int sem = rs.getInt("semester");
                int cnt = rs.getInt("cnt");
                map.put("Sem " + sem, cnt);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating semester distribution: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getDivisionDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT division, COUNT(*) as cnt FROM students WHERE division IS NOT NULL AND division != '' GROUP BY division";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("division"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("Error calculating division distribution: " + e.getMessage());
        }
        return map;
    }
}
