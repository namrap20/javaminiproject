package sms;

import sms.model.AttendanceRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AttendanceDAO: Data Access Object for Student Attendance Management.
 */
public class AttendanceDAO {

    public boolean markAttendance(String studentId, String date, String status) {
        String sql = "INSERT INTO attendance (student_id, `date`, status) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status = VALUES(status)";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            ps.setString(2, date.trim());
            ps.setString(3, status.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking attendance: " + e.getMessage());
            return false;
        }
    }

    public boolean saveBatchAttendance(List<AttendanceRecord> records) {
        if (records == null || records.isEmpty()) return true;
        String sql = "INSERT INTO attendance (student_id, `date`, status) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status = VALUES(status)";
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (AttendanceRecord r : records) {
                    ps.setString(1, r.getStudentId().trim());
                    ps.setString(2, r.getDate().trim());
                    ps.setString(3, r.getStatus().trim().toUpperCase());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error during batch attendance save: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Connection error during attendance save: " + e.getMessage());
            return false;
        }
    }

    public List<AttendanceRecord> getAllAttendance() {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT a.id, a.student_id, s.name, a.date, a.status " +
                     "FROM attendance a " +
                     "LEFT JOIN students s ON a.student_id = s.id " +
                     "ORDER BY a.date DESC, a.id DESC";
        try (Connection conn = DB.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AttendanceRecord(
                    rs.getInt("id"),
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("date"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all attendance: " + e.getMessage());
        }
        return list;
    }

    public List<AttendanceRecord> getAttendanceByStudent(String studentId) {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT a.id, a.student_id, s.name, a.date, a.status " +
                     "FROM attendance a " +
                     "LEFT JOIN students s ON a.student_id = s.id " +
                     "WHERE a.student_id = ? " +
                     "ORDER BY a.date DESC";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AttendanceRecord(
                        rs.getInt("id"),
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("date"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student attendance: " + e.getMessage());
        }
        return list;
    }

    public Map<String, String> getAttendanceMapForDate(String date) {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT student_id, status FROM attendance WHERE date = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("student_id"), rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance map for date: " + e.getMessage());
        }
        return map;
    }

    public double getAttendancePercentage(String studentId) {
        String sql = "SELECT COUNT(*) as total, " +
                     "SUM(CASE WHEN UPPER(status) = 'PRESENT' THEN 1 ELSE 0 END) as present " +
                     "FROM attendance WHERE student_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    return total > 0 ? (present * 100.0) / total : 0.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating attendance percentage: " + e.getMessage());
        }
        return 0.0;
    }

    public Map<String, Object> getOverallStats() {
        Map<String, Object> map = new LinkedHashMap<>();
        String sql = "SELECT COUNT(*) as total, " +
                     "SUM(CASE WHEN UPPER(status) = 'PRESENT' THEN 1 ELSE 0 END) as present, " +
                     "SUM(CASE WHEN UPPER(status) = 'ABSENT' THEN 1 ELSE 0 END) as absent " +
                     "FROM attendance";
        try (Connection conn = DB.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                int absent = rs.getInt("absent");
                double pct = total > 0 ? (present * 100.0) / total : 0.0;
                map.put("total", total);
                map.put("present", present);
                map.put("absent", absent);
                map.put("percentage", Math.round(pct * 10.0) / 10.0);
            }
        } catch (SQLException e) {
            System.err.println("Error getting overall attendance stats: " + e.getMessage());
            map.put("total", 0);
            map.put("present", 0);
            map.put("absent", 0);
            map.put("percentage", 0.0);
        }
        return map;
    }

    public boolean deleteAttendance(int id) {
        String sql = "DELETE FROM attendance WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting attendance: " + e.getMessage());
            return false;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM attendance";
        try (Connection conn = DB.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting attendance: " + e.getMessage());
        }
        return 0;
    }
}
