package sms.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sms.service.AuthService;
import sms.util.JsonUtil;

import java.io.IOException;
import java.util.Map;

public class AuthController implements HttpHandler {
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("POST".equalsIgnoreCase(method) && path.endsWith("/login")) {
                handleLogin(exchange);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/logout")) {
                handleLogout(exchange);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/change-password")) {
                handleChangePassword(exchange);
            } else {
                HttpHelper.sendError(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (Exception e) {
            HttpHelper.sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = HttpHelper.readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);
        String u = req.get("username") != null ? req.get("username").toString() : "";
        String p = req.get("password") != null ? req.get("password").toString() : "";

        Map<String, Object> res = authService.authenticate(u, p);
        boolean ok = Boolean.TRUE.equals(res.get("success"));
        HttpHelper.sendJsonResponse(exchange, ok ? 200 : 401, res);
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        Map<String, Object> res = Map.of("success", true, "message", "Logged out successfully.");
        HttpHelper.sendJsonResponse(exchange, 200, res);
    }

    private void handleChangePassword(HttpExchange exchange) throws IOException {
        String body = HttpHelper.readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);
        String u = req.get("username") != null ? req.get("username").toString() : "admin";
        String p = req.get("password") != null ? req.get("password").toString() : "";

        Map<String, Object> res = authService.changePassword(u, p);
        boolean ok = Boolean.TRUE.equals(res.get("success"));
        HttpHelper.sendJsonResponse(exchange, ok ? 200 : 400, res);
    }
}
