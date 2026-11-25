package sms;

import sms.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentDAO: Data Access Object for Student Records in MySQL.
 */
public class StudentDAO {

    public boolean addStudent(Student s) {
        String sql = "INSERT INTO students (id, gr_no, roll_no, name, dob, gender, email, phone, address, course, semester, division, admission_year) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getId().trim());
            ps.setString(2, s.getGrNo() != null ? s.getGrNo().trim() : null);
            ps.setString(3, s.getRollNo() != null ? s.getRollNo().trim() : null);
            ps.setString(4, s.getName().trim());
            ps.setString(5, s.getDob() != null ? s.getDob().trim() : null);
            ps.setString(6, s.getGender() != null ? s.getGender().trim() : null);
            ps.setString(7, s.getEmail() != null ? s.getEmail().trim() : null);
            ps.setString(8, s.getPhone() != null ? s.getPhone().trim() : null);
            ps.setString(9, s.getAddress() != null ? s.getAddress().trim() : null);
            ps.setString(10, s.getCourse() != null ? s.getCourse().trim() : null);
            ps.setInt(11, s.getSemester());
            ps.setString(12, s.getDivision() != null ? s.getDivision().trim() : null);
            ps.setInt(13, s.getAdmissionYear());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStudent(Student s) {
        String sql = "UPDATE students SET gr_no=?, roll_no=?, name=?, dob=?, gender=?, course=?, semester=?, division=?, admission_year=? WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getGrNo() != null ? s.getGrNo().trim() : null);
            ps.setString(2, s.getRollNo() != null ? s.getRollNo().trim() : null);
            ps.setString(3, s.getName().trim());
            ps.setString(4, s.getDob() != null ? s.getDob().trim() : null);
            ps.setString(5, s.getGender() != null ? s.getGender().trim() : null);
            ps.setString(6, s.getCourse() != null ? s.getCourse().trim() : null);
            ps.setInt(7, s.getSemester());
            ps.setString(8, s.getDivision() != null ? s.getDivision().trim() : null);
            ps.setInt(9, s.getAdmissionYear());
            ps.setString(10, s.getId().trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStudent(String id) {
        String sql = "DELETE FROM students WHERE id = ? OR gr_no = ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            ps.setString(2, id.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
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
            ps.setString(1, id.trim());
            ps.setString(2, id.trim());
            ps.setString(3, id.trim());
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

    public List<Student> getStudentsByClass(String course, int semester, String division) {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1 ");
        if (course != null && !course.trim().isEmpty() && !"All".equalsIgnoreCase(course)) {
            sql.append("AND (course = ? OR course IN (SELECT code FROM courses WHERE name = ?)) ");
        }
        if (semester > 0) {
            sql.append("AND semester = ? ");
        }
        if (division != null && !division.trim().isEmpty() && !"All".equalsIgnoreCase(division)) {
            sql.append("AND division = ? ");
        }
        sql.append("ORDER BY roll_no ASC, id ASC");

        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (course != null && !course.trim().isEmpty() && !"All".equalsIgnoreCase(course)) {
                ps.setString(paramIdx++, course.trim());
                ps.setString(paramIdx++, course.trim());
            }
            if (semester > 0) {
                ps.setInt(paramIdx++, semester);
            }
            if (division != null && !division.trim().isEmpty() && !"All".equalsIgnoreCase(division)) {
                ps.setString(paramIdx++, division.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering students by class: " + e.getMessage());
        }
        return list;
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

    public List<Student> searchStudents(String query) {
        List<Student> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllStudents();
        }
        String sql = "SELECT * FROM students WHERE " +
                     "LOWER(id) LIKE ? OR " +
                     "LOWER(gr_no) LIKE ? OR " +
                     "LOWER(roll_no) LIKE ? OR " +
                     "LOWER(name) LIKE ? OR " +
                     "LOWER(course) LIKE ? OR " +
                     "LOWER(division) LIKE ? OR " +
                     "CAST(semester AS CHAR) LIKE ? " +
                     "ORDER BY id ASC";
        String q = "%" + query.trim().toLowerCase() + "%";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 7; i++) {
                ps.setString(i, q);
            }
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

    public boolean isIdExists(String id) {
        String sql = "SELECT 1 FROM students WHERE id = ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isGrExists(String grNo, String excludeId) {
        String sql = "SELECT 1 FROM students WHERE gr_no = ? AND id != ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, grNo.trim());
            ps.setString(2, excludeId != null ? excludeId.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
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
