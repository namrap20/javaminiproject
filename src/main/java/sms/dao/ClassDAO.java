package sms.dao;

import sms.database.DB;
import sms.model.ClassModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassDAO {

    public boolean addClass(ClassModel cm) {
        String sql = "INSERT INTO classes (class_id, course_code, semester, division, room_no) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cm.getClassId());
            ps.setString(2, cm.getCourseCode());
            ps.setInt(3, cm.getSemester());
            ps.setString(4, cm.getDivision());
            ps.setString(5, cm.getRoomNo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding class: " + e.getMessage());
            return false;
        }
    }

    public List<ClassModel> getAllClasses() {
        List<ClassModel> list = new ArrayList<>();
        String sql = "SELECT * FROM classes ORDER BY class_id ASC";
        try (Connection conn = DB.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
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

    public boolean deleteClass(String classId) {
        String sql = "DELETE FROM classes WHERE class_id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting class: " + e.getMessage());
            return false;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM classes";
        try (Connection conn = DB.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting classes: " + e.getMessage());
        }
        return 0;
    }
}
