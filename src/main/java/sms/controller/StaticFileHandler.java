package sms.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class StaticFileHandler implements HttpHandler {
    private static final String FRONTEND_DIR = "src/main/resources/frontend";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null || path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        path = URLDecoder.decode(path, StandardCharsets.UTF_8.name());

        byte[] content = loadFile(path);
        if (content == null) {
            // SPA fallback to index.html for unrecognized routes
            content = loadFile("/index.html");
            if (content == null) {
                HttpHelper.sendError(exchange, 404, "File not found: " + path);
                return;
            }
            path = "/index.html";
        }

        String mime = getMimeType(path);
        HttpHelper.enableCors(exchange);
        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.sendResponseHeaders(200, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }

    private byte[] loadFile(String path) {
        // 1. Try local filesystem
        File file = new File(FRONTEND_DIR + path);
        if (file.exists() && file.isFile()) {
            try (InputStream in = new FileInputStream(file)) {
                return readBytes(in);
            } catch (IOException ignored) {}
        }

        // 2. Try relative directory "frontend"
        File relFile = new File("frontend" + path);
        if (relFile.exists() && relFile.isFile()) {
            try (InputStream in = new FileInputStream(relFile)) {
                return readBytes(in);
            } catch (IOException ignored) {}
        }

        // 3. Try classpath resource
        String resPath = "/frontend" + path;
        try (InputStream in = getClass().getResourceAsStream(resPath)) {
            if (in != null) {
                return readBytes(in);
            }
        } catch (IOException ignored) {}

        return null;
    }

    private byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    private String getMimeType(String path) {
        String p = path.toLowerCase();
        if (p.endsWith(".html") || p.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (p.endsWith(".css")) return "text/css; charset=UTF-8";
        if (p.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (p.endsWith(".json")) return "application/json; charset=UTF-8";
        if (p.endsWith(".svg")) return "image/svg+xml";
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".jpg") || p.endsWith(".jpeg")) return "image/jpeg";
        if (p.endsWith(".ico")) return "image/x-icon";
        if (p.endsWith(".woff2")) return "font/woff2";
        if (p.endsWith(".woff")) return "font/woff";
        if (p.endsWith(".ttf")) return "font/ttf";
        return "text/plain; charset=UTF-8";
    }
}
