package sms.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sms.model.Student;
import sms.service.StudentService;
import sms.util.JsonUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentController implements HttpHandler {
    private final StudentService studentService = new StudentService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath(); // e.g. /api/students or /api/students/STU101
        String method = exchange.getRequestMethod();

        String suffix = path.replaceFirst("^/api/students/?", "");
        if (suffix.startsWith("/")) suffix = suffix.substring(1);
        String idParam = !suffix.isEmpty() ? URLDecoder.decode(suffix, StandardCharsets.UTF_8) : null;

        try {
            switch (method.toUpperCase()) {
                case "GET":
                    if (idParam != null && !idParam.isEmpty()) {
                        handleGetById(exchange, idParam);
                    } else {
                        handleList(exchange);
                    }
                    break;
                case "POST":
                    handleCreate(exchange);
                    break;
                case "PUT":
                    handleUpdate(exchange, idParam);
                    break;
                case "DELETE":
                    handleDelete(exchange, idParam);
                    break;
                default:
                    HttpHelper.sendError(exchange, 405, "Method Not Allowed: " + method);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpHelper.sendError(exchange, 500, "Error processing student request: " + e.getMessage());
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        Map<String, String> params = HttpHelper.parseQueryParams(query);

        List<Student> students;
        if (params.containsKey("search") && !params.get("search").trim().isEmpty()) {
            students = studentService.globalSearch(params.get("search").trim());
        } else if (params.containsKey("field") && params.containsKey("q") && !params.get("q").trim().isEmpty()) {
            students = studentService.searchStudents(params.get("field"), params.get("q"));
        } else {
            students = studentService.getAllStudents();
        }

        List<Map<String, Object>> resList = new ArrayList<>();
        for (Student s : students) {
            resList.add(s.toMap());
        }
        HttpHelper.sendJsonResponse(exchange, 200, resList);
    }

    private void handleGetById(HttpExchange exchange, String id) throws IOException {
        Student s = studentService.getStudentById(id);
        if (s != null) {
            HttpHelper.sendJsonResponse(exchange, 200, s.toMap());
        } else {
            HttpHelper.sendError(exchange, 404, "Student not found with ID: " + id);
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        String body = HttpHelper.readRequestBody(exchange);
        Map<String, Object> map = JsonUtil.parseObject(body);
        Student student = Student.fromMap(map);

        Map<String, Object> result = studentService.addStudent(student);
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        HttpHelper.sendJsonResponse(exchange, ok ? 201 : 400, result);
    }

    private void handleUpdate(HttpExchange exchange, String id) throws IOException {
        String body = HttpHelper.readRequestBody(exchange);
        Map<String, Object> map = JsonUtil.parseObject(body);
        Student student = Student.fromMap(map);
        if (id != null && !id.isEmpty() && (student.getId() == null || student.getId().isEmpty())) {
            student.setId(id);
        }

        Map<String, Object> result = studentService.updateStudent(student);
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        HttpHelper.sendJsonResponse(exchange, ok ? 200 : 400, result);
    }

    private void handleDelete(HttpExchange exchange, String id) throws IOException {
        if (id == null || id.isEmpty()) {
            HttpHelper.sendError(exchange, 400, "Student ID must be specified in the URL.");
            return;
        }
        Map<String, Object> result = studentService.deleteStudent(id);
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        HttpHelper.sendJsonResponse(exchange, ok ? 200 : 404, result);
    }
}
