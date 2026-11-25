package sms.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sms.model.AttendanceRecord;
import sms.service.AttendanceService;
import sms.util.JsonUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttendanceController implements HttpHandler {
    private final AttendanceService attendanceService = new AttendanceService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                if (path.endsWith("/stats")) {
                    handleStats(exchange);
                } else {
                    handleList(exchange);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                handleMark(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDelete(exchange, path);
            } else {
                HttpHelper.sendError(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpHelper.sendError(exchange, 500, "Attendance controller error: " + e.getMessage());
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        Map<String, String> params = HttpHelper.parseQueryParams(query);

        List<AttendanceRecord> list;
        if (params.containsKey("studentId") && !params.get("studentId").trim().isEmpty()) {
            list = attendanceService.getAttendanceByStudent(params.get("studentId").trim());
        } else {
            list = attendanceService.getAllAttendance();
        }

        List<Map<String, Object>> resList = new ArrayList<>();
        for (AttendanceRecord r : list) resList.add(r.toMap());
        HttpHelper.sendJsonResponse(exchange, 200, resList);
    }

    private void handleStats(HttpExchange exchange) throws IOException {
        Map<String, Object> stats = attendanceService.getOverallStats();
        HttpHelper.sendJsonResponse(exchange, 200, stats);
    }

    private void handleMark(HttpExchange exchange) throws IOException {
        String body = HttpHelper.readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);
        String stId = req.get("student_id") != null ? req.get("student_id").toString() :
                     req.get("studentId") != null ? req.get("studentId").toString() : "";
        String date = req.get("date") != null ? req.get("date").toString() : "";
        String status = req.get("status") != null ? req.get("status").toString() : "PRESENT";

        Map<String, Object> res = attendanceService.markAttendance(stId, date, status);
        boolean ok = Boolean.TRUE.equals(res.get("success"));
        HttpHelper.sendJsonResponse(exchange, ok ? 201 : 400, res);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String suffix = path.replaceFirst("^/api/attendance/?", "");
        if (suffix.startsWith("/")) suffix = suffix.substring(1);
        if (suffix.isEmpty()) {
            HttpHelper.sendError(exchange, 400, "Attendance ID required in URL.");
            return;
        }
        try {
            int id = Integer.parseInt(URLDecoder.decode(suffix, StandardCharsets.UTF_8));
            Map<String, Object> res = attendanceService.deleteAttendance(id);
            HttpHelper.sendJsonResponse(exchange, 200, res);
        } catch (NumberFormatException e) {
            HttpHelper.sendError(exchange, 400, "Invalid ID format.");
        }
    }
}
