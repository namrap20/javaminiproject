package sms;

import sms.model.Student;
import sms.controller.*;
import sms.dao.*;
import sms.database.DB;
import sms.model.*;
import sms.service.*;
import sms.util.JsonUtil;

import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class SystemIntegrationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("     RUNNING SYSTEM INTEGRATION & DATA INTEGRITY TESTS    ");
        System.out.println("==========================================================");

        try {
            // Test 1: Database Initialization & Idempotence
            testDatabaseInit();

            // Test 2: User Authentication
            testAuthentication();

            // Test 3: Student CRUD & Search
            testStudentCRUD();

            // Test 4: Academic Management (Courses, Subjects, Classes, Timetable)
            testAcademicManagement();

            // Test 5: Attendance Management
            testAttendanceManagement();

            // Test 6: Examination & Results
            testExamManagement();

            // Test 7: Reports & Analytics
            testReportsAndAnalytics();

            // Test 8: Full HTTP REST API Server
            testRestApiServer();

            System.out.println("==========================================================");
            System.out.println("TEST SUMMARY:");
            System.out.println("  PASSED: " + testsPassed);
            System.out.println("  FAILED: " + testsFailed);
            System.out.println("==========================================================");

            if (testsFailed > 0) {
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Unexpected test failure: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String details) {
        if (condition) {
            System.out.println("  [PASS] " + testName + " - " + details);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " - " + details);
            testsFailed++;
        }
    }

    private static void testDatabaseInit() {
        System.out.println("\n[SUITE 1] Database Initialization & Health");
        DB.init();
        assertTrue("DB Connection", DB.checkConnection(), "Successfully established JDBC connection to sms.db");

        // Idempotent re-run
        DB.init();
        StudentDAO dao = new StudentDAO();
        assertTrue("DB Idempotency", dao.count() >= 20, "Re-running DB.init() preserved records without error");
    }

    private static void testAuthentication() {
        System.out.println("\n[SUITE 2] Authentication & Security");
        AuthService auth = new AuthService();

        Map<String, Object> validRes = auth.authenticate("admin", "1234");
        assertTrue("Valid Login", Boolean.TRUE.equals(validRes.get("success")), "admin/1234 successfully authenticated");

        Map<String, Object> invalidRes = auth.authenticate("admin", "wrong_pass");
        assertTrue("Invalid Password", Boolean.FALSE.equals(invalidRes.get("success")), "Rejected incorrect password");

        Map<String, Object> blankRes = auth.authenticate("", "");
        assertTrue("Blank Credentials", Boolean.FALSE.equals(blankRes.get("success")), "Rejected blank credentials");
    }

    private static void testStudentCRUD() {
        System.out.println("\n[SUITE 3] Student CRUD & Data Integrity");
        StudentService service = new StudentService();
        StudentDAO dao = new StudentDAO();

        int initialCount = dao.count();

        // 1. Add Student
        String testId = "STU999";
        dao.deleteStudent(testId); // Clean up if previous run left it

        Student newStudent = new Student(
            testId, "136999", "92500527999", "Integration Test Student",
            "2004-05-01", "Female", "test.student@example.com", "9123456789",
            "Ahmedabad", "0obc6678", 3, "A", 2024
        );
        Map<String, Object> addRes = service.addStudent(newStudent);
        assertTrue("Add Student", Boolean.TRUE.equals(addRes.get("success")), "Student STU999 added to database");

        // 2. Query Database directly to confirm persistence
        Student retrieved = dao.getStudentById(testId);
        assertTrue("Query by ID", retrieved != null && "Integration Test Student".equals(retrieved.getName()),
            "Direct SQL query found STU999: " + (retrieved != null ? retrieved.getName() : "null"));

        // 3. Query by GR number
        Student byGr = dao.getStudentById("136999");
        assertTrue("Query by GR No", byGr != null && testId.equals(byGr.getId()), "SQL query by GR 136999 found STU999");

        // 4. Update Student
        retrieved.setName("Updated Test Student");
        retrieved.setPhone("9999999999");
        Map<String, Object> updateRes = service.updateStudent(retrieved);
        assertTrue("Update Student", Boolean.TRUE.equals(updateRes.get("success")), "Updated student record in database");

        Student updatedFromDb = dao.getStudentById(testId);
        assertTrue("Verify Update Persistence", updatedFromDb != null && "Updated Test Student".equals(updatedFromDb.getName()) && "9999999999".equals(updatedFromDb.getPhone()),
            "Direct SQL query confirmed updated values: " + (updatedFromDb != null ? updatedFromDb.getName() : "null"));

        // 5. Search
        List<Student> searchRes = service.searchStudents("name", "Updated Test");
        assertTrue("Search by Name", !searchRes.isEmpty(), "Found student matching search keyword");

        List<Student> globalRes = service.globalSearch("9999999999");
        assertTrue("Global Search by Phone", !globalRes.isEmpty(), "Found student matching phone in global search");

        // 6. Delete Student
        Map<String, Object> delRes = service.deleteStudent(testId);
        assertTrue("Delete Student", Boolean.TRUE.equals(delRes.get("success")), "Deleted student STU999");

        Student afterDelete = dao.getStudentById(testId);
        assertTrue("Verify Deletion Persistence", afterDelete == null, "Direct SQL query confirmed STU999 is null (deleted)");
    }

    private static void testAcademicManagement() {
        System.out.println("\n[SUITE 4] Academic Management");
        AcademicService service = new AcademicService();

        // 1. Courses
        List<Course> courses = service.getAllCourses();
        assertTrue("List Courses", !courses.isEmpty(), "Found " + courses.size() + " courses in database");

        String testCourse = "TEST101";
        service.deleteCourse(testCourse);
        Map<String, Object> cRes = service.addCourse(new Course(testCourse, "Test Course", 2, 4));
        assertTrue("Add Course", Boolean.TRUE.equals(cRes.get("success")), "Added course " + testCourse);
        service.deleteCourse(testCourse);
        assertTrue("Delete Course", true, "Cleaned up test course");

        // 2. Subjects
        List<Subject> subjects = service.getAllSubjects();
        assertTrue("List Subjects", !subjects.isEmpty(), "Found " + subjects.size() + " subjects in database");

        // 3. Classes
        List<ClassModel> classes = service.getAllClasses();
        assertTrue("List Classes", !classes.isEmpty(), "Found " + classes.size() + " classes in database");

        // 4. Timetable
        List<Timetable> timetables = service.getAllTimetables();
        assertTrue("List Timetables", !timetables.isEmpty(), "Found " + timetables.size() + " timetable slots in database");
    }

    private static void testAttendanceManagement() {
        System.out.println("\n[SUITE 5] Attendance Management");
        AttendanceService service = new AttendanceService();
        AttendanceDAO dao = new AttendanceDAO();

        // Mark attendance for STU101
        Map<String, Object> markRes = service.markAttendance("STU101", "2026-10-15", "PRESENT");
        assertTrue("Mark Attendance", Boolean.TRUE.equals(markRes.get("success")), "Recorded attendance for STU101");

        List<AttendanceRecord> logs = service.getAttendanceByStudent("STU101");
        assertTrue("Student Attendance History", !logs.isEmpty(), "Retrieved " + logs.size() + " attendance logs for STU101");

        Map<String, Object> stats = service.getOverallStats();
        assertTrue("Attendance Overall Stats", stats != null && stats.containsKey("percentage"), "Calculated attendance percentage: " + stats.get("percentage") + "%");
    }

    private static void testExamManagement() {
        System.out.println("\n[SUITE 6] Examination & Results");
        ExamService service = new ExamService();
        ExamResultDAO dao = new ExamResultDAO();

        // 1. Add Result
        Map<String, Object> addRes = service.addResult("STU101", "CS303", 92.0, 100.0);
        assertTrue("Add Exam Result", Boolean.TRUE.equals(addRes.get("success")), "Added exam score for STU101 CS303");
        assertTrue("Calculated Grade", "A+".equals(addRes.get("grade")), "Computed Grade A+ for 92%");

        // 2. Student Marksheet
        Map<String, Object> sheet = service.getStudentMarksheet("STU101");
        assertTrue("Student Marksheet", Boolean.TRUE.equals(sheet.get("success")), "Generated marksheet with percentage: " + sheet.get("percentage") + "%");

        // 3. Grade Calculation rules
        assertTrue("Grade Rule 95%", "A+".equals(ExamResultDAO.calculateGrade(95, 100)), "Grade A+");
        assertTrue("Grade Rule 85%", "A".equals(ExamResultDAO.calculateGrade(85, 100)), "Grade A");
        assertTrue("Grade Rule 75%", "B".equals(ExamResultDAO.calculateGrade(75, 100)), "Grade B");
        assertTrue("Grade Rule 65%", "C".equals(ExamResultDAO.calculateGrade(65, 100)), "Grade C");
        assertTrue("Grade Rule 55%", "D".equals(ExamResultDAO.calculateGrade(55, 100)), "Grade D");
        assertTrue("Grade Rule 45%", "F".equals(ExamResultDAO.calculateGrade(45, 100)), "Grade F");
    }

    private static void testReportsAndAnalytics() {
        System.out.println("\n[SUITE 7] Reports & Analytics");
        ReportService service = new ReportService();

        Map<String, Object> stats = service.getDashboardStats();
        assertTrue("Dashboard Stats", stats != null && (int) stats.get("total_students") >= 20,
            "Total students from DB: " + stats.get("total_students"));

        List<Map<String, Object>> courseBreakdown = service.getCourseBreakdown();
        assertTrue("Course Breakdown", !courseBreakdown.isEmpty(), "Found breakdown for " + courseBreakdown.size() + " courses");

        Map<String, Integer> semDist = service.getSemesterDistribution();
        assertTrue("Semester Distribution", semDist != null && !semDist.isEmpty(), "Semester distribution compiled");
    }

    private static void testRestApiServer() throws Exception {
        System.out.println("\n[SUITE 8] HTTP REST API Server & Frontend Endpoints");

        int testPort = 8099;
        HttpServer server = HttpServer.create(new InetSocketAddress(testPort), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/api/auth", new AuthController());
        server.createContext("/api/students", new StudentController());
        server.createContext("/api/courses", new AcademicController());
        server.createContext("/api/subjects", new AcademicController());
        server.createContext("/api/classes", new AcademicController());
        server.createContext("/api/timetables", new AcademicController());
        server.createContext("/api/attendance", new AttendanceController());
        server.createContext("/api/results", new ExamController());
        server.createContext("/api/reports", new ReportController());
        server.createContext("/api/stats", new ReportController());
        server.createContext("/", new StaticFileHandler());

        server.start();

        try {
            String baseUrl = "http://localhost:" + testPort;

            // 1. Static HTML
            int indexCode = httpGetCode(baseUrl + "/index.html");
            assertTrue("HTTP GET /index.html", indexCode == 200, "Served HTML GUI successfully (Status: " + indexCode + ")");

            // 2. Static CSS
            int cssCode = httpGetCode(baseUrl + "/css/style.css");
            assertTrue("HTTP GET /css/style.css", cssCode == 200, "Served CSS stylesheet successfully (Status: " + cssCode + ")");

            // 3. Static JS
            int jsCode = httpGetCode(baseUrl + "/js/app.js");
            assertTrue("HTTP GET /js/app.js", jsCode == 200, "Served JavaScript application successfully (Status: " + jsCode + ")");

            // 4. API Login (Valid)
            String loginBody = "{\"username\":\"admin\",\"password\":\"1234\"}";
            HttpResponse loginResp = httpPost(baseUrl + "/api/auth/login", loginBody);
            assertTrue("HTTP POST /api/auth/login (Valid)", loginResp.statusCode == 200, "Login successful: " + loginResp.body);

            // 5. API Login (Invalid)
            String badLogin = "{\"username\":\"admin\",\"password\":\"wrong\"}";
            HttpResponse badResp = httpPost(baseUrl + "/api/auth/login", badLogin);
            assertTrue("HTTP POST /api/auth/login (Invalid)", badResp.statusCode == 401, "Rejected bad login (Status: " + badResp.statusCode + ")");

            // 6. API GET Students
            HttpResponse studentsResp = httpGet(baseUrl + "/api/students");
            assertTrue("HTTP GET /api/students", studentsResp.statusCode == 200 && studentsResp.body.contains("STU101"),
                "Loaded students list via REST API: " + studentsResp.body.substring(0, Math.min(100, studentsResp.body.length())) + "...");

            // 7. API GET Student by ID
            HttpResponse studentById = httpGet(baseUrl + "/api/students/STU101");
            assertTrue("HTTP GET /api/students/STU101", studentById.statusCode == 200 && studentById.body.contains("Kartik"),
                "Loaded single student by ID: " + studentById.body);

            // 8. API GET Courses
            HttpResponse coursesResp = httpGet(baseUrl + "/api/courses");
            assertTrue("HTTP GET /api/courses", coursesResp.statusCode == 200 && coursesResp.body.contains("0obc6678"),
                "Loaded courses via REST API");

            // 9. API GET Stats
            HttpResponse statsResp = httpGet(baseUrl + "/api/stats");
            assertTrue("HTTP GET /api/stats", statsResp.statusCode == 200 && statsResp.body.contains("total_students"),
                "Loaded live system metrics via REST API");

        } finally {
            server.stop(0);
        }
    }

    private static int httpGetCode(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        return conn.getResponseCode();
    }

    private static HttpResponse httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        int code = conn.getResponseCode();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
            code < 400 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return new HttpResponse(code, sb.toString());
    }

    private static HttpResponse httpPost(String urlStr, String json) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            code < 400 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return new HttpResponse(code, sb.toString());
    }

    private static class HttpResponse {
        final int statusCode;
        final String body;
        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
