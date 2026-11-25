package sms.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sms.model.ClassModel;
import sms.model.Course;
import sms.model.Subject;
import sms.model.Timetable;
import sms.service.AcademicService;
import sms.util.JsonUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AcademicController implements HttpHandler {
    private final AcademicService academicService = new AcademicService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.startsWith("/api/courses")) {
                handleCourses(exchange, path, method);
            } else if (path.startsWith("/api/subjects")) {
                handleSubjects(exchange, path, method);
            } else if (path.startsWith("/api/classes")) {
                handleClasses(exchange, path, method);
            } else if (path.startsWith("/api/timetables")) {
                handleTimetables(exchange, path, method);
            } else {
                HttpHelper.sendError(exchange, 404, "Not Found: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpHelper.sendError(exchange, 500, "Academic controller error: " + e.getMessage());
        }
    }

    // Courses
    private void handleCourses(HttpExchange exchange, String path, String method) throws IOException {
        String param = extractParam(path, "/api/courses");
        if ("GET".equalsIgnoreCase(method)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Course c : academicService.getAllCourses()) list.add(c.toMap());
            HttpHelper.sendJsonResponse(exchange, 200, list);
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseObject(body);
            Course c = new Course(
                getString(req, "code"),
                getString(req, "name"),
                getInt(req, "duration_years", 3),
                getInt(req, "total_semesters", 6)
            );
            Map<String, Object> res = academicService.addCourse(c);
            boolean ok = Boolean.TRUE.equals(res.get("success"));
            HttpHelper.sendJsonResponse(exchange, ok ? 201 : 400, res);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            if (param == null || param.isEmpty()) {
                HttpHelper.sendError(exchange, 400, "Course code required in URL.");
                return;
            }
            Map<String, Object> res = academicService.deleteCourse(param);
            HttpHelper.sendJsonResponse(exchange, 200, res);
        } else {
            HttpHelper.sendError(exchange, 405, "Method Not Allowed");
        }
    }

    // Subjects
    private void handleSubjects(HttpExchange exchange, String path, String method) throws IOException {
        String param = extractParam(path, "/api/subjects");
        if ("GET".equalsIgnoreCase(method)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Subject s : academicService.getAllSubjects()) list.add(s.toMap());
            HttpHelper.sendJsonResponse(exchange, 200, list);
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseObject(body);
            Subject s = new Subject(
                getString(req, "code"),
                getString(req, "name"),
                getString(req, "course_code", "courseCode"),
                getInt(req, "semester", 1),
                getInt(req, "credits", 3)
            );
            Map<String, Object> res = academicService.addSubject(s);
            boolean ok = Boolean.TRUE.equals(res.get("success"));
            HttpHelper.sendJsonResponse(exchange, ok ? 201 : 400, res);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            if (param == null || param.isEmpty()) {
                HttpHelper.sendError(exchange, 400, "Subject code required in URL.");
                return;
            }
            Map<String, Object> res = academicService.deleteSubject(param);
            HttpHelper.sendJsonResponse(exchange, 200, res);
        } else {
            HttpHelper.sendError(exchange, 405, "Method Not Allowed");
        }
    }

    // Classes
    private void handleClasses(HttpExchange exchange, String path, String method) throws IOException {
        String param = extractParam(path, "/api/classes");
        if ("GET".equalsIgnoreCase(method)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (ClassModel cm : academicService.getAllClasses()) list.add(cm.toMap());
            HttpHelper.sendJsonResponse(exchange, 200, list);
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseObject(body);
            ClassModel cm = new ClassModel(
                getString(req, "class_id", "id"),
                getString(req, "course_code", "courseCode"),
                getInt(req, "semester", 1),
                getString(req, "division"),
                getString(req, "room_no", "room")
            );
            Map<String, Object> res = academicService.addClass(cm);
            boolean ok = Boolean.TRUE.equals(res.get("success"));
            HttpHelper.sendJsonResponse(exchange, ok ? 201 : 400, res);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            if (param == null || param.isEmpty()) {
                HttpHelper.sendError(exchange, 400, "Class ID required in URL.");
                return;
            }
            Map<String, Object> res = academicService.deleteClass(param);
            HttpHelper.sendJsonResponse(exchange, 200, res);
        } else {
            HttpHelper.sendError(exchange, 405, "Method Not Allowed");
        }
    }

    // Timetables
    private void handleTimetables(HttpExchange exchange, String path, String method) throws IOException {
        String param = extractParam(path, "/api/timetables");
        if ("GET".equalsIgnoreCase(method)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Timetable t : academicService.getAllTimetables()) list.add(t.toMap());
            HttpHelper.sendJsonResponse(exchange, 200, list);
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseObject(body);
            Timetable t = new Timetable(
                0,
                getString(req, "class_id", "classId"),
                getString(req, "day"),
                getString(req, "time_slot", "timeSlot"),
                getString(req, "subject_code", "subjectCode")
            );
            Map<String, Object> res = academicService.addTimetable(t);
            boolean ok = Boolean.TRUE.equals(res.get("success"));
            HttpHelper.sendJsonResponse(exchange, ok ? 201 : 400, res);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            if (param == null || param.isEmpty()) {
                HttpHelper.sendError(exchange, 400, "Timetable ID required in URL.");
                return;
            }
            try {
                int id = Integer.parseInt(param);
                Map<String, Object> res = academicService.deleteTimetable(id);
                HttpHelper.sendJsonResponse(exchange, 200, res);
            } catch (NumberFormatException e) {
                HttpHelper.sendError(exchange, 400, "Invalid ID format.");
            }
        } else {
            HttpHelper.sendError(exchange, 405, "Method Not Allowed");
        }
    }

    private String extractParam(String path, String prefix) {
        String suffix = path.replaceFirst("^" + prefix + "/?", "");
        if (suffix.startsWith("/")) suffix = suffix.substring(1);
        if (suffix.isEmpty()) return null;
        try {
            return URLDecoder.decode(suffix, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return suffix;
        }
    }

    private String getString(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null) return v.toString().trim();
        }
        return "";
    }

    private int getInt(Map<String, Object> map, String key, int def) {
        Object v = map.get(key);
        if (v == null) return def;
        try {
            if (v instanceof Number) return ((Number) v).intValue();
            return Integer.parseInt(v.toString().trim());
        } catch (Exception e) {
            return def;
        }
    }
}
