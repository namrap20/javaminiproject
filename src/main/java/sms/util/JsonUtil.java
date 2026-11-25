package sms.util;

import java.util.*;

public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) {
            return quote((String) obj);
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append(quote(String.valueOf(entry.getKey()))).append(":");
                sb.append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Iterable) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Iterable<?>) obj) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj.getClass().isArray()) {
            Object[] arr = (Object[]) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(arr[i]));
            }
            sb.append("]");
            return sb.toString();
        }
        return quote(obj.toString());
    }

    private static String quote(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32 || c >= 127) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        if (json == null || json.trim().isEmpty()) return new LinkedHashMap<>();
        Object parsed = new Parser(json.trim()).parseValue();
        if (parsed instanceof Map) {
            return (Map<String, Object>) parsed;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        Object parsed = new Parser(json.trim()).parseValue();
        if (parsed instanceof List) {
            return (List<Object>) parsed;
        }
        return new ArrayList<>();
    }

    private static class Parser {
        private final String src;
        private int pos = 0;

        Parser(String src) {
            this.src = src;
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        private char peek() {
            skipWhitespace();
            if (pos >= src.length()) return '\0';
            return src.charAt(pos);
        }

        private char next() {
            skipWhitespace();
            if (pos >= src.length()) return '\0';
            return src.charAt(pos++);
        }

        Object parseValue() {
            skipWhitespace();
            char c = peek();
            if (c == '{') return parseObjectInternal();
            if (c == '[') return parseArrayInternal();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (Character.isDigit(c) || c == '-') return parseNumber();
            return null;
        }

        private Map<String, Object> parseObjectInternal() {
            Map<String, Object> map = new LinkedHashMap<>();
            next(); // consume '{'
            skipWhitespace();
            if (peek() == '}') {
                next();
                return map;
            }
            while (pos < src.length()) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                if (next() != ':') break;
                Object val = parseValue();
                map.put(key, val);
                skipWhitespace();
                char sep = next();
                if (sep == '}') break;
                if (sep != ',') break;
            }
            return map;
        }

        private List<Object> parseArrayInternal() {
            List<Object> list = new ArrayList<>();
            next(); // consume '['
            skipWhitespace();
            if (peek() == ']') {
                next();
                return list;
            }
            while (pos < src.length()) {
                list.add(parseValue());
                skipWhitespace();
                char sep = next();
                if (sep == ']') break;
                if (sep != ',') break;
            }
            return list;
        }

        private String parseString() {
            skipWhitespace();
            if (next() != '"') return "";
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') break;
                if (c == '\\' && pos < src.length()) {
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (pos + 4 <= src.length()) {
                                String hex = src.substring(pos, pos + 4);
                                pos += 4;
                                sb.append((char) Integer.parseInt(hex, 16));
                            }
                            break;
                        default: sb.append(esc); break;
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", pos)) {
                pos += 4;
                return true;
            }
            if (src.startsWith("false", pos)) {
                pos += 5;
                return false;
            }
            return false;
        }

        private Object parseNull() {
            if (src.startsWith("null", pos)) {
                pos += 4;
            }
            return null;
        }

        private Number parseNumber() {
            skipWhitespace();
            int start = pos;
            if (pos < src.length() && src.charAt(pos) == '-') pos++;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
                pos++;
            }
            String numStr = src.substring(start, pos);
            if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                try {
                    return Double.parseDouble(numStr);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            try {
                return Long.parseLong(numStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}
