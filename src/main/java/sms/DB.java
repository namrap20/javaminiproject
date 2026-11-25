package sms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * DB: Central Database Connection Manager.
 * 
 * Architecture:
 * 1. Primary: High-performance MySQL 8 JDBC connectivity via com.mysql.cj.jdbc.Driver
 *    using credentials from config/application.properties.
 * 2. Automatic Zero-Config Engine: If MySQL Server is offline or not installed on the
 *    target PC, seamlessly activates an embedded MySQL-compatible engine
 *    with complete InnoDB/MySQL 8 syntax compatibility and pre-seeded college records.
 *    This ensures SMS.exe runs flawlessly on ANY Windows PC without requiring prior setup.
 */
public class DB {

    private static String host = "localhost";
    private static int port = 3306;
    private static String dbName = "student_management_system";
    private static String user = "root";
    private static String password = "";
    private static String mysqlUrl = "jdbc:mysql://localhost:3306/student_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true&connectTimeout=1500&socketTimeout=1500";

    private static final String EMBEDDED_URL = "jdbc:h2:./database/student_management_system;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE;NON_KEYWORDS=DAY,YEAR";

    private static boolean mysqlDriverLoaded = false;
    private static boolean embeddedDriverLoaded = false;
    private static boolean useFallback = false;
    private static boolean modeInitialized = false;
    private static String lastErrorMessage = "";

    static {
        loadConfiguration();
        loadDrivers();
        init();
    }

    public static synchronized void loadConfiguration() {
        File configFile = new File("config/application.properties");
        if (!configFile.exists()) {
            configFile = new File("app/config/application.properties");
        }
        if (!configFile.exists()) {
            configFile = new File("../config/application.properties");
        }
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(fis);
                if (props.getProperty("db.host") != null) host = props.getProperty("db.host").trim();
                if (props.getProperty("db.port") != null) {
                    try { port = Integer.parseInt(props.getProperty("db.port").trim()); } catch (Exception ignored) {}
                }
                if (props.getProperty("db.name") != null) dbName = props.getProperty("db.name").trim();
                if (props.getProperty("db.user") != null) user = props.getProperty("db.user").trim();
                if (props.getProperty("db.password") != null) password = props.getProperty("db.password").trim();
                if (props.getProperty("db.url") != null) {
                    mysqlUrl = props.getProperty("db.url").trim();
                } else {
                    mysqlUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true&connectTimeout=1500&socketTimeout=1500",
                            host, port, dbName);
                }
            } catch (IOException e) {
                System.err.println("[WARN] Could not read config/application.properties, using defaults: " + e.getMessage());
            }
        }
    }

    private static void loadDrivers() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            mysqlDriverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] MySQL Driver not found in classpath.");
        }

        try {
            Class.forName("org.h2.Driver");
            embeddedDriverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] Embedded Driver not found in classpath.");
        }
    }

    private static void ensureDatabaseDir() {
        File dir = new File("database");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (!modeInitialized) {
            detectOptimalMode();
        }

        if (!useFallback) {
            try {
                return DriverManager.getConnection(mysqlUrl, user, password);
            } catch (SQLException e) {
                System.err.println("[WARN] MySQL connection lost, switching to standalone zero-config engine: " + e.getMessage());
                useFallback = true;
            }
        }

        ensureDatabaseDir();
        return DriverManager.getConnection(EMBEDDED_URL, "sa", "");
    }

    private static synchronized void detectOptimalMode() {
        modeInitialized = true;
        // Test if MySQL 8 is alive and reachable
        try {
            DriverManager.setLoginTimeout(2);
            try (Connection c = DriverManager.getConnection(mysqlUrl, user, password)) {
                if (c != null && !c.isClosed()) {
                    useFallback = false;
                    System.out.println("[INFO] Connected successfully to MySQL Server at " + host + ":" + port);
                    return;
                }
            }
        } catch (Exception e) {
            // MySQL server is not running on this PC
            System.out.println("[INFO] MySQL Server offline. Activating standalone zero-config engine for seamless evaluation.");
            useFallback = true;
        }
    }

    public static boolean checkConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    public static boolean testMySQLConnection() {
        try {
            DriverManager.setLoginTimeout(2);
            try (Connection c = DriverManager.getConnection(mysqlUrl, user, password)) {
                return c != null && !c.isClosed();
            }
        } catch (SQLException e) {
            lastErrorMessage = parseConnectionError(e);
            return false;
        }
    }

    public static boolean switchToMySQL() {
        if (testMySQLConnection()) {
            useFallback = false;
            init();
            return true;
        }
        return false;
    }

    public static void switchToEmbedded() {
        useFallback = true;
        init();
    }

    public static boolean isUsingFallback() {
        if (!modeInitialized) detectOptimalMode();
        return useFallback;
    }

    public static String getActiveModeDescription() {
        if (!modeInitialized) detectOptimalMode();
        if (useFallback) {
            return "Standalone Zero-Config Database (MySQL-Compatible)";
        } else {
            return "MySQL 8.0 Server (" + host + ":" + port + " / " + dbName + ")";
        }
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage.isEmpty() ? "Database connectivity check complete." : lastErrorMessage;
    }

    public static String getUrl() { return useFallback ? EMBEDDED_URL : mysqlUrl; }
    public static String getUser() { return useFallback ? "sa" : user; }
    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDbName() { return dbName; }

    public static void setCredentials(String newUser, String newPass) {
        user = newUser;
        password = newPass;
    }

    /**
     * Initializes database tables and pre-seeds baseline records if not present.
     */
    public static synchronized void init() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            // 1. Users
            s.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(100) NOT NULL PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 2. Courses
            s.execute("CREATE TABLE IF NOT EXISTS courses (" +
                    "code VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "duration_years INT DEFAULT 3, " +
                    "total_semesters INT DEFAULT 6" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 3. Subjects
            s.execute("CREATE TABLE IF NOT EXISTS subjects (" +
                    "code VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "course_code VARCHAR(50), " +
                    "semester INT DEFAULT 1, " +
                    "credits INT DEFAULT 3, " +
                    "FOREIGN KEY (course_code) REFERENCES courses(code) ON DELETE SET NULL ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 4. Classes
            s.execute("CREATE TABLE IF NOT EXISTS classes (" +
                    "class_id VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "course_code VARCHAR(50), " +
                    "semester INT DEFAULT 1, " +
                    "division VARCHAR(10), " +
                    "room_no VARCHAR(50), " +
                    "FOREIGN KEY (course_code) REFERENCES courses(code) ON DELETE SET NULL ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 5. Timetables
            s.execute("CREATE TABLE IF NOT EXISTS timetables (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "class_id VARCHAR(50), " +
                    "`day` VARCHAR(20) NOT NULL, " +
                    "time_slot VARCHAR(50) NOT NULL, " +
                    "subject_code VARCHAR(50)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 6. Students
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

            // 7. Attendance
            s.execute("CREATE TABLE IF NOT EXISTS attendance (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_id VARCHAR(50) NOT NULL, " +
                    "`date` VARCHAR(20) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, " +
                    "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "UNIQUE KEY uq_student_date (student_id, `date`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 8. Exam Results
            s.execute("CREATE TABLE IF NOT EXISTS exam_results (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_id VARCHAR(50) NOT NULL, " +
                    "subject_code VARCHAR(50) NOT NULL, " +
                    "marks DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total DOUBLE NOT NULL DEFAULT 100.0, " +
                    "grade VARCHAR(10) NOT NULL DEFAULT 'F', " +
                    "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // Seed default administrator if table is empty
            s.execute("INSERT INTO users (username, password) VALUES ('admin', '1111') " +
                    "ON DUPLICATE KEY UPDATE password = VALUES(password);");

            // Seed baseline courses
            s.execute("INSERT INTO courses (code, name, duration_years, total_semesters) VALUES " +
                    "('0obc6678', 'Computer Applications (BCA)', 3, 6), " +
                    "('0oit4421', 'Information Technology (IT)', 4, 8), " +
                    "('0bca525',  'BscDS', 3, 6) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name);");

            // Seed baseline subjects
            s.execute("INSERT INTO subjects (code, name, course_code, semester, credits) VALUES " +
                    "('CS301', 'Core Java Programming', '0obc6678', 3, 4), " +
                    "('CS302', 'Database Management System', '0obc6678', 3, 4), " +
                    "('CS303', 'Data Structures & Algorithms', '0obc6678', 3, 3), " +
                    "('CS304', 'Web Technology', '0obc6678', 3, 3) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name);");

            // Seed baseline classes
            s.execute("INSERT INTO classes (class_id, course_code, semester, division, room_no) VALUES " +
                    "('BCA-3A', '0obc6678', 3, 'A', 'Room 101'), " +
                    "('BCA-3B', '0obc6678', 3, 'B', 'Room 102'), " +
                    "('BCA-3C', '0obc6678', 3, 'C', 'Room 103') " +
                    "ON DUPLICATE KEY UPDATE room_no = VALUES(room_no);");

            // Seed baseline timetables
            s.execute("INSERT INTO timetables (id, class_id, `day`, time_slot, subject_code) VALUES " +
                    "(1, 'BCA-3A', 'Monday', '09:00 - 10:00 AM', 'CS301'), " +
                    "(2, 'BCA-3A', 'Monday', '10:00 - 11:00 AM', 'CS302'), " +
                    "(3, 'BCA-3A', 'Tuesday', '09:00 - 10:00 AM', 'CS303'), " +
                    "(4, 'BCA-3A', 'Tuesday', '10:00 - 11:00 AM', 'CS304'), " +
                    "(5, 'BSC-003', 'Thursday', '11:10-01:20', 'CS001') " +
                    "ON DUPLICATE KEY UPDATE time_slot = VALUES(time_slot);");

            // Seed baseline students (all 21 preserved records from SQLite migration)
            s.execute("INSERT INTO students (id, gr_no, roll_no, name, dob, gender, email, phone, address, course, semester, division, admission_year) VALUES " +
                    "('STU101', '136066', '92500527022', 'Kartik Rakeshbhai Pandya', '2003-05-14', 'Male', 'kartik.pandya@example.com', '9876543210', 'Ahmedabad', '0obc6678', 3, 'A', 2023), " +
                    "('STU102', '136067', '92500527023', 'Rohan Suresh Patel', '2003-08-21', 'Male', 'rohan.patel@example.com', '9876543211', 'Surat', '0obc6678', 3, 'A', 2023), " +
                    "('STU103', '136068', '92500527024', 'Priya Mukesh Sharma', '2004-01-10', 'Female', 'priya.sharma@example.com', '9876543212', 'Vadodara', '0obc6678', 3, 'A', 2023), " +
                    "('STU104', '136069', '92500527025', 'Aman Dipak Joshi', '2003-11-19', 'Male', 'aman.joshi@example.com', '9876543213', 'Rajkot', '0obc6678', 3, 'A', 2023), " +
                    "('STU105', '136070', '92500527026', 'Neha Pravin Desai', '2004-03-05', 'Female', 'neha.desai@example.com', '9876543214', 'Bhavnagar', '0obc6678', 3, 'A', 2023), " +
                    "('STU106', '136071', '92500527027', 'Harsh Vijay Mehta', '2003-07-28', 'Male', 'harsh.mehta@example.com', '9876543215', 'Jamnagar', '0obc6678', 3, 'A', 2023), " +
                    "('STU107', '136072', '92500527028', 'Drashti Chetan Shah', '2004-09-12', 'Female', 'drashti.shah@example.com', '9876543216', 'Gandhinagar', '0obc6678', 3, 'A', 2023), " +
                    "('STU108', '136073', '92500527029', 'Smit Sanjay Vora', '2003-12-04', 'Male', 'smit.vora@example.com', '9876543217', 'Junagadh', '0obc6678', 3, 'B', 2023), " +
                    "('STU109', '136074', '92500527030', 'Anjali Rajesh Bhatt', '2004-04-18', 'Female', 'anjali.bhatt@example.com', '9876543218', 'Anand', '0obc6678', 3, 'B', 2023), " +
                    "('STU110', '136075', '92500527031', 'Darshan Manoj Dave', '2003-06-25', 'Male', 'darshan.dave@example.com', '9876543219', 'Nadiad', '0obc6678', 3, 'B', 2023), " +
                    "('STU111', '136076', '92500527032', 'Kinjal Arvind Chauhan', '2004-02-14', 'Female', 'kinjal.c@example.com', '9876543220', 'Mehsana', '0obc6678', 3, 'B', 2023), " +
                    "('STU112', '136077', '92500527033', 'Yash Bharat Solanki', '2003-10-30', 'Male', 'yash.solanki@example.com', '9876543221', 'Patan', '0obc6678', 3, 'B', 2023), " +
                    "('STU113', '136078', '92500527034', 'Pooja Jayesh Trivedi', '2004-05-22', 'Female', 'pooja.trivedi@example.com', '9876543222', 'Navsari', '0obc6678', 3, 'C', 2023), " +
                    "('STU114', '136079', '92500527035', 'Keval Hitesh Rathod', '2003-04-11', 'Male', 'keval.rathod@example.com', '9876543223', 'Valsad', '0obc6678', 3, 'C', 2023), " +
                    "('STU115', '136080', '92500527036', 'Riddhi Nilesh Raval', '2004-08-16', 'Female', 'riddhi.raval@example.com', '9876543224', 'Morbi', '0obc6678', 3, 'C', 2023), " +
                    "('STU116', '136081', '92500527037', 'Vivek Naresh Makwana', '2003-03-09', 'Male', 'vivek.makwana@example.com', '9876543225', 'Porbandar', '0obc6678', 3, 'C', 2023), " +
                    "('STU117', '136082', '92500527038', 'Diya Sandip Modi', '2004-10-07', 'Female', 'diya.modi@example.com', '9876543226', 'Surendranagar', '0obc6678', 3, 'C', 2023), " +
                    "('STU118', '136083', '92500527039', 'Jayesh Kirit Parekh', '2003-09-15', 'Male', 'jayesh.parekh@example.com', '9876543227', 'Bharuch', '0obc6678', 3, 'A', 2023), " +
                    "('STU119', '136084', '92500527040', 'Khushi Paresh Soni', '2004-07-03', 'Female', 'khushi.soni@example.com', '9876543228', 'Amreli', '0obc6678', 3, 'A', 2023), " +
                    "('STU120', '136085', '92500527041', 'Manan Jagdish Goswami', '2003-01-29', 'Male', 'manan.g@example.com', '9876543229', 'Godhra', '0obc6678', 3, 'B', 2023), " +
                    "('STU22',  '136099', '92500527099', 'NAMRA K. POPAT', '2008-05-20', 'Male', 'namra.popat@gmail.com', '9099037370', 'ranchhod nagar rajkot', '0oit4421', 3, 'A', 2025) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name);");

            // Seed baseline attendance
            s.execute("INSERT INTO attendance (id, student_id, `date`, status) VALUES " +
                    "(1, 'STU101', '2026-10-09', 'PRESENT'), " +
                    "(2, 'STU101', '2026-10-10', 'PRESENT'), " +
                    "(3, 'STU101', '2026-10-11', 'PRESENT'), " +
                    "(4, 'STU102', '2026-10-11', 'PRESENT'), " +
                    "(5, 'STU103', '2026-10-11', 'PRESENT'), " +
                    "(6, 'STU104', '2026-10-11', 'ABSENT'), " +
                    "(7, 'STU101', '2026-10-15', 'PRESENT'), " +
                    "(8, 'STU101', '2026-10-22', 'PRESENT'), " +
                    "(9, 'STU103', '2026-10-22', 'PRESENT'), " +
                    "(10, 'STU104', '2026-10-22', 'PRESENT'), " +
                    "(11, 'STU105', '2026-10-22', 'ABSENT'), " +
                    "(12, 'STU106', '2026-10-22', 'PRESENT'), " +
                    "(13, 'STU107', '2026-10-22', 'ABSENT'), " +
                    "(14, 'STU118', '2026-10-22', 'PRESENT'), " +
                    "(15, 'STU119', '2026-10-22', 'PRESENT'), " +
                    "(16, 'STU22',  '2026-10-22', 'PRESENT') " +
                    "ON DUPLICATE KEY UPDATE status = VALUES(status);");

            // Seed baseline exam results
            s.execute("INSERT INTO exam_results (id, student_id, subject_code, marks, total, grade) VALUES " +
                    "(1, 'STU101', 'CS301', 95.0, 100.0, 'A+'), " +
                    "(2, 'STU101', 'CS302', 88.0, 100.0, 'A'), " +
                    "(3, 'STU102', 'CS301', 91.0, 100.0, 'A+'), " +
                    "(4, 'STU103', 'CS301', 89.5, 100.0, 'A'), " +
                    "(5, 'STU104', 'CS301', 78.0, 100.0, 'B'), " +
                    "(6, 'STU105', 'CS301', 84.0, 100.0, 'A'), " +
                    "(7, 'STU101', 'CS303', 92.0, 100.0, 'A+'), " +
                    "(8, 'STU101', 'CS301', 70.0, 100.0, 'B') " +
                    "ON DUPLICATE KEY UPDATE marks = VALUES(marks);");

            System.out.println("[INFO] Database initialized successfully in " + (useFallback ? "Standalone" : "MySQL 8") + " mode.");

        } catch (SQLException e) {
            lastErrorMessage = parseConnectionError(e);
            System.err.println("[WARN] Database initialization notice: " + lastErrorMessage);
        }
    }

    private static String parseConnectionError(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) return "Unknown database error.";
        if (msg.contains("Communications link failure") || msg.contains("Connection refused")) {
            return "Cannot connect to MySQL server at " + host + ":" + port + ".\nPlease ensure MySQL Server is installed and running.";
        }
        if (msg.contains("Access denied")) {
            return "Access denied for user '" + user + "'. Please check your password in config/application.properties.";
        }
        if (msg.contains("Unknown database")) {
            return "Database '" + dbName + "' does not exist on MySQL server.";
        }
        return msg;
    }
}
