package sms.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * Java-based Migration Utility: SQLite -> MySQL 8
 * 
 * Migrates all records from sms.db (SQLite) to student_management_system (MySQL).
 * Uses Transactions, PreparedStatements, InnoDB, utf8mb4, and verifies counts.
 */
public class MigrateSQLiteToMySQL {

    private static String mysqlUrl = "jdbc:mysql://localhost:3306/student_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static String mysqlUser = "root";
    private static String mysqlPass = "";

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("     SMS - SQLITE TO MYSQL 8 DATABASE MIGRATION UTILITY         ");
        System.out.println("================================================================");

        loadConfig();

        File sqliteFile = new File("sms.db");
        if (!sqliteFile.exists()) {
            System.err.println("[ERROR] SQLite database file 'sms.db' not found in current directory!");
            return;
        }

        String sqliteUrl = "jdbc:sqlite:sms.db";

        try {
            Class.forName("org.sqlite.JDBC");
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] JDBC Driver missing: " + e.getMessage());
            return;
        }

        try (Connection sqliteConn = DriverManager.getConnection(sqliteUrl)) {
            System.out.println("[1/4] Connected to source SQLite database: sms.db");

            // Ensure MySQL Database exists
            ensureMySQLDatabase();

            try (Connection mysqlConn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass)) {
                System.out.println("[2/4] Connected to target MySQL database: " + mysqlUrl);

                // Create MySQL schema
                createMySQLSchema(mysqlConn);

                // Perform Migration in order of Foreign Key dependencies
                System.out.println("[3/4] Migrating tables using transactions...");
                mysqlConn.setAutoCommit(false);
                try {
                    int users = migrateUsers(sqliteConn, mysqlConn);
                    int courses = migrateCourses(sqliteConn, mysqlConn);
                    int subjects = migrateSubjects(sqliteConn, mysqlConn);
                    int classes = migrateClasses(sqliteConn, mysqlConn);
                    int timetables = migrateTimetables(sqliteConn, mysqlConn);
                    int students = migrateStudents(sqliteConn, mysqlConn);
                    int attendance = migrateAttendance(sqliteConn, mysqlConn);
                    int examResults = migrateExamResults(sqliteConn, mysqlConn);

                    mysqlConn.commit();
                    System.out.println("[SUCCESS] Transaction committed successfully!");

                    // Verify record counts
                    System.out.println("\n[4/4] Verifying record counts across SQLite and MySQL:");
                    System.out.println("----------------------------------------------------------------");
                    System.out.printf("%-18s | %-12s | %-12s | %-10s%n", "Table Name", "SQLite Rows", "MySQL Rows", "Status");
                    System.out.println("----------------------------------------------------------------");
                    verifyTable(sqliteConn, mysqlConn, "users");
                    verifyTable(sqliteConn, mysqlConn, "courses");
                    verifyTable(sqliteConn, mysqlConn, "subjects");
                    verifyTable(sqliteConn, mysqlConn, "classes");
                    verifyTable(sqliteConn, mysqlConn, "timetables");
                    verifyTable(sqliteConn, mysqlConn, "students");
                    verifyTable(sqliteConn, mysqlConn, "attendance");
                    verifyTable(sqliteConn, mysqlConn, "exam_results");
                    System.out.println("----------------------------------------------------------------");
                    System.out.println("Migration completed successfully! Original 'sms.db' preserved as backup.");

                } catch (Exception e) {
                    mysqlConn.rollback();
                    System.err.println("[ERROR] Migration failed. Transaction rolled back! " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Database connection failure: " + e.getMessage());
            System.err.println("Please verify MySQL Server is running on port 3306 with credentials in config/application.properties.");
        }
    }

    private static void loadConfig() {
        File cfg = new File("config/application.properties");
        if (cfg.exists()) {
            try (FileInputStream fis = new FileInputStream(cfg)) {
                Properties p = new Properties();
                p.load(fis);
                if (p.getProperty("db.url") != null) mysqlUrl = p.getProperty("db.url");
                if (p.getProperty("db.user") != null) mysqlUser = p.getProperty("db.user");
                if (p.getProperty("db.password") != null) mysqlPass = p.getProperty("db.password");
            } catch (IOException ignored) {}
        }
    }

    private static void ensureMySQLDatabase() {
        String baseUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection c = DriverManager.getConnection(baseUrl, mysqlUser, mysqlPass);
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS student_management_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        } catch (SQLException e) {
            // Might already exist or lack global privs, ignored
        }
    }

    private static void createMySQLSchema(Connection conn) throws SQLException {
        try (Statement s = conn.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(100) NOT NULL PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            s.execute("CREATE TABLE IF NOT EXISTS courses (" +
                    "code VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "duration_years INT DEFAULT 3, " +
                    "total_semesters INT DEFAULT 6" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            s.execute("CREATE TABLE IF NOT EXISTS subjects (" +
                    "code VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "course_code VARCHAR(50), " +
                    "semester INT DEFAULT 1, " +
                    "credits INT DEFAULT 3, " +
                    "FOREIGN KEY (course_code) REFERENCES courses(code) ON DELETE SET NULL ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            s.execute("CREATE TABLE IF NOT EXISTS classes (" +
                    "class_id VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "course_code VARCHAR(50), " +
                    "semester INT DEFAULT 1, " +
                    "division VARCHAR(10), " +
                    "room_no VARCHAR(50), " +
                    "FOREIGN KEY (course_code) REFERENCES courses(code) ON DELETE SET NULL ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            s.execute("CREATE TABLE IF NOT EXISTS timetables (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "class_id VARCHAR(50), " +
                    "day VARCHAR(20) NOT NULL, " +
                    "time_slot VARCHAR(50) NOT NULL, " +
                    "subject_code VARCHAR(50)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            s.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "id VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "gr_no VARCHAR(50) UNIQUE, " +
                    "roll_no VARCHAR(50), " +
                    "name VARCHAR(255) NOT NULL, " +
                    "dob VARCHAR(50), " +
                    "gender VARCHAR(20), " +
                    "email VARCHAR(255), " +
                    "phone VARCHAR(50), " +
                    "address TEXT, " +
                    "course VARCHAR(50), " +
                    "semester INT DEFAULT 1, " +
                    "division VARCHAR(10), " +
                    "admission_year INT DEFAULT 2024" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            s.execute("CREATE TABLE IF NOT EXISTS attendance (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_id VARCHAR(50) NOT NULL, " +
                    "date VARCHAR(20) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, " +
                    "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "UNIQUE KEY uq_student_date (student_id, date)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            s.execute("CREATE TABLE IF NOT EXISTS exam_results (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_id VARCHAR(50) NOT NULL, " +
                    "subject_code VARCHAR(50) NOT NULL, " +
                    "marks DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total DOUBLE NOT NULL DEFAULT 100.0, " +
                    "grade VARCHAR(10) NOT NULL DEFAULT 'F', " +
                    "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        }
    }

    private static int migrateUsers(Connection src, Connection dest) throws SQLException {
        String select = "SELECT username, password FROM users";
        String insert = "INSERT INTO users (username, password) VALUES (?, ?) ON DUPLICATE KEY UPDATE password = VALUES(password)";
        int count = 0;
        try (Statement s = src.createStatement(); ResultSet rs = s.executeQuery(select);
             PreparedStatement ps = dest.prepareStatement(insert)) {
            while (rs.next()) {
                ps.setString(1, rs.getString("username"));
                ps.setString(2, rs.getString("password"));
                ps.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private static int migrateCourses(Connection src, Connection dest) throws SQLException {
        String select = "SELECT code, name, duration_years, total_semesters FROM courses";
        String insert = "INSERT INTO courses (code, name, duration_years, total_semesters) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name), duration_years = VALUES(duration_years), total_semesters = VALUES(total_semesters)";
        int count = 0;
        try (Statement s = src.createStatement(); ResultSet rs = s.executeQuery(select);
             PreparedStatement ps = dest.prepareStatement(insert)) {
            while (rs.next()) {
                ps.setString(1, rs.getString("code"));
                ps.setString(2, rs.getString("name"));
                ps.setInt(3, rs.getInt("duration_years"));
                ps.setInt(4, rs.getInt("total_semesters"));
                ps.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private static int migrateSubjects(Connection src, Connection dest) throws SQLException {
        String select = "SELECT code, name, course_code, semester, credits FROM subjects";
        String insert = "INSERT INTO subjects (code, name, course_code, semester, credits) VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name), course_code = VALUES(course_code), semester = VALUES(semester), credits = VALUES(credits)";
        int count = 0;
        try (Statement s = src.createStatement(); ResultSet rs = s.executeQuery(select);
             PreparedStatement ps = dest.prepareStatement(insert)) {
            while (rs.next()) {
                ps.setString(1, rs.getString("code"));
                ps.setString(2, rs.getString("name"));
                ps.setString(3, rs.getString("course_code"));
                ps.setInt(4, rs.getInt("semester"));
                ps.setInt(5, rs.getInt("credits"));
                ps.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private static int migrateClasses(Connection src, Connection dest) throws SQLException {
        String select = "SELECT class_id, course_code, semester, division, room_no FROM classes";
        String insert = "INSERT INTO classes (class_id, course_code, semester, division, room_no) VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE course_code = VALUES(course_code), semester = VALUES(semester), division = VALUES(division), room_no = VALUES(room_no)";
        int count = 0;
        try (Statement s = src.createStatement(); ResultSet rs = s.executeQuery(select);
             PreparedStatement ps = dest.prepareStatement(insert)) {
            while (rs.next()) {
                ps.setString(1, rs.getString("class_id"));
                ps.setString(2, rs.getString("course_code"));
                ps.setInt(3, rs.getInt("semester"));
                ps.setString(4, rs.getString("division"));
                ps.setString(5, rs.getString("room_no"));
                ps.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private static int migrateTimetables(Connection src, Connection dest) throws SQLException {
        String select = "SELECT id, class_id, day, time_slot, subject_code FROM timetables";
        String insert = "INSERT INTO timetables (id, class_id, day, time_slot, subject_code) VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE class_id = VALUES(class_id), day = VALUES(day), time_slot = VALUES(time_slot), subject_code = VALUES(subject_code)";
        int count = 0;
        try (Statement s = src.createStatement(); ResultSet rs = s.executeQuery(select);
             PreparedStatement ps = dest.prepareStatement(insert)) {
            while (rs.next()) {
                ps.setInt(1, rs.getInt("id"));
                ps.setString(2, rs.getString("class_id"));
                ps.setString(3, rs.getString("day"));
                ps.setString(4, rs.getString("time_slot"));
                ps.setString(5, rs.getString("subject_code"));
                ps.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private static int migrateStudents(Connection src, Connection dest) throws SQLException {
        String select = "SELECT id, gr_no, roll_no, name, dob, gender, email, phone, address, course, semester, division, admission_year FROM students";
        String insert = "INSERT INTO students (id, gr_no, roll_no, name, dob, gender, email, phone, address, course, semester, division, admission_year) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name), dob = VALUES(dob), gender = VALUES(gender), course = VALUES(course), " +
                        "semester = VALUES(semester), division = VALUES(division), admission_year = VALUES(admission_year)";
        int count = 0;
        try (Statement s = src.createStatement(); ResultSet rs = s.executeQuery(select);
             PreparedStatement ps = dest.prepareStatement(insert)) {
            while (rs.next()) {
                ps.setString(1, rs.getString("id"));
                ps.setString(2, rs.getString("gr_no"));
                ps.setString(3, rs.getString("roll_no"));
                ps.setString(4, rs.getString("name"));
                ps.setString(5, rs.getString("dob"));
                ps.setString(6, rs.getString("gender"));
                ps.setString(7, rs.getString("email"));
                ps.setString(8, rs.getString("phone"));
                ps.setString(9, rs.getString("address"));
                ps.setString(10, rs.getString("course"));
                ps.setInt(11, rs.getInt("semester"));
                ps.setString(12, rs.getString("division"));
                ps.setInt(13, rs.getInt("admission_year"));
                ps.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private static int migrateAttendance(Connection src, Connection dest) throws SQLException {
        String select = "SELECT id, student_id, date, status FROM attendance";
        String insert = "INSERT INTO attendance (id, student_id, date, status) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE status = VALUES(status)";
        int count = 0;
        try (Statement s = src.createStatement(); ResultSet rs = s.executeQuery(select);
             PreparedStatement ps = dest.prepareStatement(insert)) {
            while (rs.next()) {
                ps.setInt(1, rs.getInt("id"));
                ps.setString(2, rs.getString("student_id"));
                ps.setString(3, rs.getString("date"));
                ps.setString(4, rs.getString("status"));
                ps.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private static int migrateExamResults(Connection src, Connection dest) throws SQLException {
        String select = "SELECT id, student_id, subject_code, marks, total, grade FROM exam_results";
        String insert = "INSERT INTO exam_results (id, student_id, subject_code, marks, total, grade) VALUES (?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE marks = VALUES(marks), total = VALUES(total), grade = VALUES(grade)";
        int count = 0;
        try (Statement s = src.createStatement(); ResultSet rs = s.executeQuery(select);
             PreparedStatement ps = dest.prepareStatement(insert)) {
            while (rs.next()) {
                ps.setInt(1, rs.getInt("id"));
                ps.setString(2, rs.getString("student_id"));
                ps.setString(3, rs.getString("subject_code"));
                ps.setDouble(4, rs.getDouble("marks"));
                ps.setDouble(5, rs.getDouble("total"));
                ps.setString(6, rs.getString("grade"));
                ps.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private static void verifyTable(Connection src, Connection dest, String tableName) {
        int srcCount = -1;
        int destCount = -1;
        try (Statement s = src.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) srcCount = rs.getInt(1);
        } catch (SQLException ignored) {}

        try (Statement s = dest.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) destCount = rs.getInt(1);
        } catch (SQLException ignored) {}

        String status = (srcCount == destCount && srcCount >= 0) ? "MATCH (OK)" : "MISMATCH";
        System.out.printf("%-18s | %-12d | %-12d | %-10s%n", tableName, srcCount, destCount, status);
    }
}
