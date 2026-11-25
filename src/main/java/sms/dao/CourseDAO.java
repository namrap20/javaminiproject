package sms.dao;

import sms.database.DB;
import sms.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public boolean addCourse(Course c) {
        String sql = "INSERT INTO courses (code, name, duration_years, total_semesters) VALUES (?, ?, ?, ?)";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getName());
            ps.setInt(3, c.getDurationYears());
            ps.setInt(4, c.getTotalSemesters());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding course: " + e.getMessage());
            return false;
        }
    }

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY code ASC";
        try (Connection conn = DB.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
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

    public Course getCourseByCode(String code) {
        String sql = "SELECT * FROM courses WHERE code = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getInt("duration_years"),
                        rs.getInt("total_semesters")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting course: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteCourse(String code) {
        String sql = "DELETE FROM courses WHERE code = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting course: " + e.getMessage());
            return false;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM courses";
        try (Connection conn = DB.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting courses: " + e.getMessage());
        }
        return 0;
    }
}
