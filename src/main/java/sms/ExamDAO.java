package sms;

import sms.model.ExamResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ExamDAO: Data Access Object for Examination Results and Academic Grading.
 */
public class ExamDAO {

    /**
     * Standard College Grading Rule:
     * 90% and above = A+
     * 80% and above = A
     * 70% and above = B
     * 60% and above = C
     * 50% and above = D
     * Below 50% = F
     */
    public static String calculateGrade(double marks, double total) {
        if (total <= 0) return "F";
        double pct = (marks / total) * 100.0;
        if (pct >= 90.0) return "A+";
        if (pct >= 80.0) return "A";
        if (pct >= 70.0) return "B";
        if (pct >= 60.0) return "C";
        if (pct >= 50.0) return "D";
        return "F";
    }

    public boolean addExamResult(String studentId, String subjectCode, double marks, double total) {
        String grade = calculateGrade(marks, total);
        String sql = "INSERT INTO exam_results (student_id, subject_code, marks, total, grade) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            ps.setString(2, subjectCode.trim());
            ps.setDouble(3, marks);
            ps.setDouble(4, total);
            ps.setString(5, grade);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding exam result: " + e.getMessage());
            return false;
        }
    }

    public boolean updateExamResult(int id, double marks, double total) {
        String grade = calculateGrade(marks, total);
        String sql = "UPDATE exam_results SET marks = ?, total = ?, grade = ? WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, marks);
            ps.setDouble(2, total);
            ps.setString(3, grade);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating exam result: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteExamResult(int id) {
        String sql = "DELETE FROM exam_results WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting exam result: " + e.getMessage());
            return false;
        }
    }

    public List<ExamResult> getAllExamResults() {
        List<ExamResult> list = new ArrayList<>();
        String sql = "SELECT e.id, e.student_id, s.name, e.subject_code, e.marks, e.total, e.grade " +
                     "FROM exam_results e " +
                     "LEFT JOIN students s ON e.student_id = s.id " +
                     "ORDER BY e.id DESC";
        try (Connection conn = DB.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ExamResult(
                    rs.getInt("id"),
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("subject_code"),
                    rs.getDouble("marks"),
                    rs.getDouble("total"),
                    rs.getString("grade")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all exam results: " + e.getMessage());
        }
        return list;
    }

    public List<ExamResult> getResultsByStudent(String studentId) {
        List<ExamResult> list = new ArrayList<>();
        String sql = "SELECT e.id, e.student_id, s.name, e.subject_code, e.marks, e.total, e.grade " +
                     "FROM exam_results e " +
                     "LEFT JOIN students s ON e.student_id = s.id " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY e.id ASC";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ExamResult(
                        rs.getInt("id"),
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("subject_code"),
                        rs.getDouble("marks"),
                        rs.getDouble("total"),
                        rs.getString("grade")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student results: " + e.getMessage());
        }
        return list;
    }

    public Map<String, Object> getExamStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        String sql = "SELECT COUNT(*) as total_exams, " +
                     "AVG((marks / total) * 100.0) as avg_pct " +
                     "FROM exam_results WHERE total > 0";
        try (Connection conn = DB.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                stats.put("total_exams", rs.getInt("total_exams"));
                double avg = rs.getDouble("avg_pct");
                stats.put("average_score", Math.round(avg * 10.0) / 10.0);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating exam stats: " + e.getMessage());
            stats.put("total_exams", 0);
            stats.put("average_score", 0.0);
        }

        Map<String, Integer> grades = new LinkedHashMap<>();
        grades.put("A+", 0);
        grades.put("A", 0);
        grades.put("B", 0);
        grades.put("C", 0);
        grades.put("D", 0);
        grades.put("F", 0);

        String gSql = "SELECT grade, COUNT(*) as cnt FROM exam_results GROUP BY grade";
        try (Connection conn = DB.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(gSql)) {
            while (rs.next()) {
                String g = rs.getString("grade");
                if (g != null && grades.containsKey(g)) {
                    grades.put(g, rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating grade counts: " + e.getMessage());
        }
        stats.put("grade_distribution", grades);
        return stats;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM exam_results";
        try (Connection conn = DB.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting exam results: " + e.getMessage());
        }
        return 0;
    }
}
