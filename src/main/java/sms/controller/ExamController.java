package sms.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sms.model.ExamResult;
import sms.service.ExamService;
import sms.util.JsonUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExamController implements HttpHandler {
    private final ExamService examService = new ExamService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                if (path.contains("/marksheet/")) {
                    handleMarksheet(exchange, path);
                } else if (path.endsWith("/stats")) {
                    handleStats(exchange);
                } else {
                    handleList(exchange);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                handleCreate(exchange);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handleUpdate(exchange, path);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDelete(exchange, path);
            } else {
                HttpHelper.sendError(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpHelper.sendError(exchange, 500, "Exam controller error: " + e.getMessage());
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        Map<String, String> params = HttpHelper.parseQueryParams(query);

        List<ExamResult> list;
        if (params.containsKey("studentId") && !params.get("studentId").trim().isEmpty()) {
            list = examService.getResultsByStudent(params.get("studentId").trim());
        } else {
            list = examService.getAllResults();
        }

        List<Map<String, Object>> resList = new ArrayList<>();
        for (ExamResult r : list) resList.add(r.toMap());
        HttpHelper.sendJsonResponse(exchange, 200, resList);
    }

    private void handleMarksheet(HttpExchange exchange, String path) throws IOException {
        String suffix = path.substring(path.indexOf("/marksheet/") + 11);
        if (suffix.startsWith("/")) suffix = suffix.substring(1);
        String studentId = URLDecoder.decode(suffix, StandardCharsets.UTF_8);

        Map<String, Object> sheet = examService.getStudentMarksheet(studentId);
        boolean ok = Boolean.TRUE.equals(sheet.get("success"));
        HttpHelper.sendJsonResponse(exchange, ok ? 200 : 404, sheet);
    }

    private void handleStats(HttpExchange exchange) throws IOException {
        Map<String, Object> stats = examService.getExamStats();
        HttpHelper.sendJsonResponse(exchange, 200, stats);
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        String body = HttpHelper.readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);

        String stId = getString(req, "student_id", "studentId");
        String subCode = getString(req, "subject_code", "subject");
        double marks = getDouble(req, "marks");
        double total = getDouble(req, "total");

        Map<String, Object> res = examService.addResult(stId, subCode, marks, total);
        boolean ok = Boolean.TRUE.equals(res.get("success"));
        HttpHelper.sendJsonResponse(exchange, ok ? 201 : 400, res);
    }

    private void handleUpdate(HttpExchange exchange, String path) throws IOException {
        String suffix = path.replaceFirst("^/api/results/?", "");
        if (suffix.startsWith("/")) suffix = suffix.substring(1);
        try {
            int id = Integer.parseInt(URLDecoder.decode(suffix, StandardCharsets.UTF_8));
            String body = HttpHelper.readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseObject(body);
            double marks = getDouble(req, "marks");
            double total = getDouble(req, "total");

            Map<String, Object> res = examService.updateResult(id, marks, total);
            boolean ok = Boolean.TRUE.equals(res.get("success"));
            HttpHelper.sendJsonResponse(exchange, ok ? 200 : 400, res);
        } catch (NumberFormatException e) {
            HttpHelper.sendError(exchange, 400, "Invalid ID format.");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String suffix = path.replaceFirst("^/api/results/?", "");
        if (suffix.startsWith("/")) suffix = suffix.substring(1);
        try {
            int id = Integer.parseInt(URLDecoder.decode(suffix, StandardCharsets.UTF_8));
            Map<String, Object> res = examService.deleteResult(id);
            HttpHelper.sendJsonResponse(exchange, 200, res);
        } catch (NumberFormatException e) {
            HttpHelper.sendError(exchange, 400, "Invalid ID format.");
        }
    }

    private String getString(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null) return v.toString().trim();
        }
        return "";
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return 0.0;
        try {
            if (v instanceof Number) return ((Number) v).doubleValue();
            return Double.parseDouble(v.toString().trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
