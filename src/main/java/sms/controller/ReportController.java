package sms.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sms.service.ReportService;

import java.io.IOException;
import java.util.Map;

public class ReportController implements HttpHandler {
    private final ReportService reportService = new ReportService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (!"GET".equalsIgnoreCase(method)) {
            HttpHelper.sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            if (path.endsWith("/stats") || path.endsWith("/summary")) {
                Map<String, Object> stats = reportService.getDashboardStats();
                HttpHelper.sendJsonResponse(exchange, 200, stats);
            } else if (path.endsWith("/courses")) {
                HttpHelper.sendJsonResponse(exchange, 200, reportService.getCourseBreakdown());
            } else if (path.endsWith("/semesters")) {
                HttpHelper.sendJsonResponse(exchange, 200, reportService.getSemesterDistribution());
            } else if (path.endsWith("/divisions")) {
                HttpHelper.sendJsonResponse(exchange, 200, reportService.getDivisionDistribution());
            } else {
                Map<String, Object> stats = reportService.getDashboardStats();
                HttpHelper.sendJsonResponse(exchange, 200, stats);
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpHelper.sendError(exchange, 500, "Report controller error: " + e.getMessage());
        }
    }
}
