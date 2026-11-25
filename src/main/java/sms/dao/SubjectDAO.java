package sms.dao;

import sms.database.DB;
import sms.model.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    public boolean addSubject(Subject s) {
        String sql = "INSERT INTO subjects (code, name, course_code, semester, credits) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getCode());
            ps.setString(2, s.getName());
            ps.setString(3, s.getCourseCode());
            ps.setInt(4, s.getSemester());
            ps.setInt(5, s.getCredits());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding subject: " + e.getMessage());
            return false;
        }
    }

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects ORDER BY code ASC";
        try (Connection conn = DB.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
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

    public Subject getSubjectByCode(String code) {
        String sql = "SELECT * FROM subjects WHERE code = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Subject(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("course_code"),
                        rs.getInt("semester"),
                        rs.getInt("credits")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting subject: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteSubject(String code) {
        String sql = "DELETE FROM subjects WHERE code = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting subject: " + e.getMessage());
            return false;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM subjects";
        try (Connection conn = DB.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting subjects: " + e.getMessage());
        }
        return 0;
    }
}
