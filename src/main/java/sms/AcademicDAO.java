package sms;

import sms.model.ClassModel;
import sms.model.Course;
import sms.model.Subject;
import sms.model.Timetable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AcademicDAO: Data Access Object for Academic Management.
 * 
 * Unifies MySQL database operations for:
 * 1. Courses
 * 2. Subjects
 * 3. Classes
 * 4. Timetable Slots
 */
public class AcademicDAO {

    // ========================================================
    // 1. COURSES
    // ========================================================

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY code ASC";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Course(
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getInt("duration_years"),
                    rs.getInt("total_semesters")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching courses: " + e.getMessage());
        }
        return list;
    }

    public boolean addCourse(Course c) {
        String sql = "INSERT INTO courses (code, name, duration_years, total_semesters) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), duration_years = VALUES(duration_years), total_semesters = VALUES(total_semesters)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCode().trim());
            ps.setString(2, c.getName().trim());
            ps.setInt(3, c.getDurationYears());
            ps.setInt(4, c.getTotalSemesters());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding course: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCourse(Course c) {
        String sql = "UPDATE courses SET name = ?, duration_years = ?, total_semesters = ? WHERE code = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName().trim());
            ps.setInt(2, c.getDurationYears());
            ps.setInt(3, c.getTotalSemesters());
            ps.setString(4, c.getCode().trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating course: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCourse(String code) {
        String sql = "DELETE FROM courses WHERE code = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting course: " + e.getMessage());
            return false;
        }
    }

    public int countCourses() {
        String sql = "SELECT COUNT(*) FROM courses";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting courses: " + e.getMessage());
        }
        return 0;
    }

    // ========================================================
    // 2. SUBJECTS
    // ========================================================

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects ORDER BY code ASC";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Subject(
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getString("course_code"),
                    rs.getInt("semester"),
                    rs.getInt("credits")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subjects: " + e.getMessage());
        }
        return list;
    }

    public List<Subject> getSubjectsByCourseAndSemester(String courseCode, int semester) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE course_code = ? AND semester = ? ORDER BY code ASC";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Subject(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("course_code"),
                        rs.getInt("semester"),
                        rs.getInt("credits")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering subjects: " + e.getMessage());
        }
        return list;
    }

    public boolean addSubject(Subject s) {
        String sql = "INSERT INTO subjects (code, name, course_code, semester, credits) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), course_code = VALUES(course_code), semester = VALUES(semester), credits = VALUES(credits)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getCode().trim());
            ps.setString(2, s.getName().trim());
            ps.setString(3, s.getCourseCode() != null ? s.getCourseCode().trim() : null);
            ps.setInt(4, s.getSemester());
            ps.setInt(5, s.getCredits());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding subject: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSubject(Subject s) {
        String sql = "UPDATE subjects SET name = ?, course_code = ?, semester = ?, credits = ? WHERE code = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName().trim());
            ps.setString(2, s.getCourseCode() != null ? s.getCourseCode().trim() : null);
            ps.setInt(3, s.getSemester());
            ps.setInt(4, s.getCredits());
            ps.setString(5, s.getCode().trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating subject: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSubject(String code) {
        String sql = "DELETE FROM subjects WHERE code = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting subject: " + e.getMessage());
            return false;
        }
    }

    public int countSubjects() {
        String sql = "SELECT COUNT(*) FROM subjects";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting subjects: " + e.getMessage());
        }
        return 0;
    }

    // ========================================================
    // 3. CLASSES
    // ========================================================

    public List<ClassModel> getAllClasses() {
        List<ClassModel> list = new ArrayList<>();
        String sql = "SELECT * FROM classes ORDER BY class_id ASC";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ClassModel(
                    rs.getString("class_id"),
                    rs.getString("course_code"),
                    rs.getInt("semester"),
                    rs.getString("division"),
                    rs.getString("room_no")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching classes: " + e.getMessage());
        }
        return list;
    }

    public boolean addClass(ClassModel cm) {
        String sql = "INSERT INTO classes (class_id, course_code, semester, division, room_no) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE course_code = VALUES(course_code), semester = VALUES(semester), division = VALUES(division), room_no = VALUES(room_no)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cm.getClassId().trim());
            ps.setString(2, cm.getCourseCode() != null ? cm.getCourseCode().trim() : null);
            ps.setInt(3, cm.getSemester());
            ps.setString(4, cm.getDivision().trim());
            ps.setString(5, cm.getRoomNo() != null ? cm.getRoomNo().trim() : "");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding class: " + e.getMessage());
            return false;
        }
    }

    public boolean updateClass(ClassModel cm) {
        String sql = "UPDATE classes SET course_code = ?, semester = ?, division = ?, room_no = ? WHERE class_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cm.getCourseCode() != null ? cm.getCourseCode().trim() : null);
            ps.setInt(2, cm.getSemester());
            ps.setString(3, cm.getDivision().trim());
            ps.setString(4, cm.getRoomNo() != null ? cm.getRoomNo().trim() : "");
            ps.setString(5, cm.getClassId().trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating class: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteClass(String classId) {
        String sql = "DELETE FROM classes WHERE class_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting class: " + e.getMessage());
            return false;
        }
    }

    public int countClasses() {
        String sql = "SELECT COUNT(*) FROM classes";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting classes: " + e.getMessage());
        }
        return 0;
    }

    // ========================================================
    // 4. TIMETABLES
    // ========================================================

    public List<Timetable> getAllTimetables() {
        List<Timetable> list = new ArrayList<>();
        String sql = "SELECT * FROM timetables ORDER BY id ASC";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Timetable(
                    rs.getInt("id"),
                    rs.getString("class_id"),
                    rs.getString("day"),
                    rs.getString("time_slot"),
                    rs.getString("subject_code")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching timetables: " + e.getMessage());
        }
        return list;
    }

    public boolean addTimetable(Timetable t) {
        String sql = "INSERT INTO timetables (class_id, `day`, time_slot, subject_code) VALUES (?, ?, ?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getClassId().trim());
            ps.setString(2, t.getDay().trim());
            ps.setString(3, t.getTimeSlot().trim());
            ps.setString(4, t.getSubjectCode().trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding timetable: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTimetable(Timetable t) {
        String sql = "UPDATE timetables SET class_id = ?, `day` = ?, time_slot = ?, subject_code = ? WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getClassId().trim());
            ps.setString(2, t.getDay().trim());
            ps.setString(3, t.getTimeSlot().trim());
            ps.setString(4, t.getSubjectCode().trim());
            ps.setInt(5, t.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating timetable: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTimetable(int id) {
        String sql = "DELETE FROM timetables WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting timetable: " + e.getMessage());
            return false;
        }
    }

    public int countTimetables() {
        String sql = "SELECT COUNT(*) FROM timetables";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting timetables: " + e.getMessage());
        }
        return 0;
    }
}
