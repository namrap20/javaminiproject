package sms.service;

import sms.dao.UserDAO;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public Map<String, Object> authenticate(String username, String password) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Username and password are required.");
            return res;
        }

        boolean valid = userDAO.validateUser(username.trim(), password.trim());
        if (valid) {
            res.put("success", true);
            res.put("message", "Login successful! Welcome to Student Management System.");
            res.put("username", username.trim());
            res.put("role", "Administrator");
        } else {
            res.put("success", false);
            res.put("message", "Invalid username or password. Please try again.");
        }
        return res;
    }

    public Map<String, Object> changePassword(String username, String newPassword) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (newPassword == null || newPassword.trim().length() < 4) {
            res.put("success", false);
            res.put("message", "Password must be at least 4 characters.");
            return res;
        }
        boolean ok = userDAO.changePassword(username, newPassword.trim());
        res.put("success", ok);
        res.put("message", ok ? "Password updated successfully." : "Failed to update password.");
        return res;
    }
}
