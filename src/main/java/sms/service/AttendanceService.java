package sms.service;

import sms.dao.AttendanceDAO;
import sms.dao.StudentDAO;
import sms.model.AttendanceRecord;
import sms.model.Student;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AttendanceService {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    public List<AttendanceRecord> getAllAttendance() {
        return attendanceDAO.getAllAttendance();
    }

    public List<AttendanceRecord> getAttendanceByStudent(String studentId) {
        return attendanceDAO.getAttendanceByStudent(studentId);
    }

    public Map<String, Object> markAttendance(String studentId, String date, String status) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (studentId == null || studentId.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Student ID is required.");
            return res;
        }
        if (date == null || date.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Date is required (YYYY-MM-DD).");
            return res;
        }
        Student s = studentDAO.getStudentById(studentId.trim());
        if (s == null) {
            res.put("success", false);
            res.put("message", "Student with ID '" + studentId + "' not found.");
            return res;
        }

        String st = status != null && !status.trim().isEmpty() ? status.trim().toUpperCase() : "PRESENT";
        boolean ok = attendanceDAO.markAttendance(s.getId(), date.trim(), st);
        res.put("success", ok);
        res.put("message", ok ? "Attendance marked as " + st + " for " + s.getName() : "Failed to record attendance.");
        return res;
    }

    public Map<String, Object> getOverallStats() {
        return attendanceDAO.getOverallStats();
    }

    public Map<String, Object> deleteAttendance(int id) {
        Map<String, Object> res = new LinkedHashMap<>();
        boolean ok = attendanceDAO.deleteAttendance(id);
        res.put("success", ok);
        res.put("message", ok ? "Attendance log deleted." : "Failed to delete record.");
        return res;
    }
}
