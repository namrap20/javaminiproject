package sms.controller;

import com.sun.net.httpserver.HttpExchange;
import sms.util.JsonUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpHelper {

    public static void enableCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    public static boolean handlePreflight(HttpExchange exchange) throws IOException {
        enableCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream in = exchange.getRequestBody();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        enableCors(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        String json = JsonUtil.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("success", false);
        err.put("message", message);
        sendJsonResponse(exchange, statusCode, err);
    }

    public static Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isEmpty()) return Collections.emptyMap();
        Map<String, String> params = new LinkedHashMap<>();
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                try {
                    String k = java.net.URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String v = java.net.URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    params.put(k, v);
                } catch (Exception ignored) {}
            }
        }
        return params;
    }
}
