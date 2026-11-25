package sms.dao;

import sms.database.DB;
import sms.model.Timetable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {

    public boolean addTimetable(Timetable t) {
        String sql = "INSERT INTO timetables (class_id, day, time_slot, subject_code) VALUES (?, ?, ?, ?)";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getClassId());
            ps.setString(2, t.getDay());
            ps.setString(3, t.getTimeSlot());
            ps.setString(4, t.getSubjectCode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding timetable slot: " + e.getMessage());
            return false;
        }
    }

    public List<Timetable> getAllTimetables() {
        List<Timetable> list = new ArrayList<>();
        String sql = "SELECT * FROM timetables ORDER BY id ASC";
        try (Connection conn = DB.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
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

    public boolean deleteTimetable(int id) {
        String sql = "DELETE FROM timetables WHERE id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting timetable: " + e.getMessage());
            return false;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM timetables";
        try (Connection conn = DB.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting timetables: " + e.getMessage());
        }
        return 0;
    }
}
