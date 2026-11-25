package sms.dao;

import sms.database.DB;
import sms.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public boolean addStudent(Student s) {
        String sql = "INSERT INTO students (id, gr_no, roll_no, name, dob, gender, email, phone, address, course, semester, division, admission_year) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getId());
            ps.setString(2, s.getGrNo());
            ps.setString(3, s.getRollNo());
            ps.setString(4, s.getName());
            ps.setString(5, s.getDob());
            ps.setString(6, s.getGender());
            ps.setString(7, s.getEmail());
            ps.setString(8, s.getPhone());
            ps.setString(9, s.getAddress());
            ps.setString(10, s.getCourse());
            ps.setInt(11, s.getSemester());
            ps.setString(12, s.getDivision());
            ps.setInt(13, s.getAdmissionYear());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
            return false;
        }
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY id ASC";
        try (Connection c = DB.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all students: " + e.getMessage());
        }
        return list;
    }

    public Student getStudentById(String id) {
        String sql = "SELECT * FROM students WHERE id = ? OR gr_no = ? OR roll_no = ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, id);
            ps.setString(3, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean updateStudent(Student s) {
        String sql = "UPDATE students SET gr_no=?, roll_no=?, name=?, dob=?, gender=?, email=?, phone=?, address=?, course=?, semester=?, division=?, admission_year=? WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getGrNo());
            ps.setString(2, s.getRollNo());
            ps.setString(3, s.getName());
            ps.setString(4, s.getDob());
            ps.setString(5, s.getGender());
            ps.setString(6, s.getEmail());
            ps.setString(7, s.getPhone());
            ps.setString(8, s.getAddress());
            ps.setString(9, s.getCourse());
            ps.setInt(10, s.getSemester());
            ps.setString(11, s.getDivision());
            ps.setInt(12, s.getAdmissionYear());
            ps.setString(13, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStudent(String id) {
        String sql = "DELETE FROM students WHERE id = ? OR gr_no = ? OR roll_no = ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, id);
            ps.setString(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }

    public List<Student> searchStudents(String field, String val) {
        List<Student> list = new ArrayList<>();
        String col = "name";
        if ("id".equalsIgnoreCase(field)) col = "id";
        else if ("gr_no".equalsIgnoreCase(field) || "grNo".equalsIgnoreCase(field)) col = "gr_no";
        else if ("roll_no".equalsIgnoreCase(field) || "rollNo".equalsIgnoreCase(field)) col = "roll_no";
        else if ("course".equalsIgnoreCase(field)) col = "course";
        else if ("semester".equalsIgnoreCase(field)) col = "semester";
        else if ("division".equalsIgnoreCase(field)) col = "division";

        String sql = "SELECT * FROM students WHERE LOWER(" + col + ") LIKE ? ORDER BY id ASC";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + val.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching students: " + e.getMessage());
        }
        return list;
    }

    public List<Student> globalSearch(String query) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE " +
                     "LOWER(id) LIKE ? OR " +
                     "LOWER(gr_no) LIKE ? OR " +
                     "LOWER(roll_no) LIKE ? OR " +
                     "LOWER(name) LIKE ? OR " +
                     "LOWER(email) LIKE ? OR " +
                     "LOWER(phone) LIKE ? OR " +
                     "LOWER(course) LIKE ? OR " +
                     "LOWER(address) LIKE ? " +
                     "ORDER BY id ASC";
        String q = "%" + query.toLowerCase() + "%";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 8; i++) {
                ps.setString(i, q);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing global search: " + e.getMessage());
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection c = DB.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting students: " + e.getMessage());
        }
        return 0;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
            rs.getString("id"),
            rs.getString("gr_no"),
            rs.getString("roll_no"),
            rs.getString("name"),
            rs.getString("dob"),
            rs.getString("gender"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"),
            rs.getString("course"),
            rs.getInt("semester"),
            rs.getString("division"),
            rs.getInt("admission_year")
        );
    }
}
