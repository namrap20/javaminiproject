package sms;

import sms.model.Student;
import sms.dao.*;
import sms.model.*;

import java.util.*;

public class ConsoleSMS {

    private static final Scanner sc = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAO();
    private static final StudentDAO studentDAO = new StudentDAO();
    private static final CourseDAO courseDAO = new CourseDAO();
    private static final SubjectDAO subjectDAO = new SubjectDAO();
    private static final ClassDAO classDAO = new ClassDAO();
    private static final TimetableDAO timetableDAO = new TimetableDAO();
    private static final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private static final ExamResultDAO examResultDAO = new ExamResultDAO();

    public static void run() {
        System.out.println("==================================================");
        System.out.println("            STUDENT MANAGEMENT SYSTEM             ");
        System.out.println("==================================================");

        boolean running = true;
        while (running) {
            System.out.println("\n----------------------------------");
            System.out.println("            LOGIN MENU            ");
            System.out.println("----------------------------------");
            System.out.println("1. Login");
            System.out.println("2. Exit System");
            System.out.println("----------------------------------");
            System.out.print("Enter choice (1-2): ");
            String opt = sc.nextLine().trim();

            if ("1".equals(opt)) {
                if (login()) {
                    mainMenu();
                }
            } else if ("2".equals(opt)) {
                System.out.println("\nExiting Student Management System. Goodbye!");
                running = false;
            } else {
                System.out.println("Invalid selection! Please enter 1 or 2.");
            }
        }
    }

    private static boolean login() {
        System.out.println("\n----------------------------------");
        System.out.println("              LOGIN               ");
        System.out.println("----------------------------------");
        System.out.print("Username: ");
        String u = sc.nextLine().trim();
        System.out.print("Password: ");
        String p = sc.nextLine().trim();

        if (userDAO.validateUser(u, p)) {
            System.out.println("\nLogin Successful! Welcome, Administrator.");
            return true;
        } else {
            System.out.println("\nInvalid Username or Password!");
            return false;
        }
    }

    private static void mainMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n==========================================");
            System.out.println("               MAIN MENU                  ");
            System.out.println("==========================================");
            System.out.println("1. Student Management");
            System.out.println("2. Academic Management");
            System.out.println("3. Attendance Management");
            System.out.println("4. Examination & Result Management");
            System.out.println("5. Reports & Search");
            System.out.println("6. Logout");
            System.out.println("7. Exit");
            System.out.println("==========================================");
            System.out.print("Enter choice (1-7): ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1": studentMenu(); break;
                case "2": academicMenu(); break;
                case "3": attendanceMenu(); break;
                case "4": examMenu(); break;
                case "5": reportsMenu(); break;
                case "6":
                    System.out.println("\nLogged out successfully.");
                    inMenu = false;
                    break;
                case "7":
                    System.out.println("\nExiting Student Management System. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option! Choose between 1 and 7.");
            }
        }
    }

    // 1. STUDENT MANAGEMENT
    private static void studentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------------------------------------");
            System.out.println("            STUDENT MANAGEMENT            ");
            System.out.println("------------------------------------------");
            System.out.println("1. Student Registration");
            System.out.println("2. Student Profile");
            System.out.println("3. Student Academic Information");
            System.out.println("4. Student Search");
            System.out.println("5. Back");
            System.out.println("------------------------------------------");
            System.out.print("Enter choice (1-5): ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1": registrationSubMenu(); break;
                case "2": profileSubMenu(); break;
                case "3": academicInfoSubMenu(); break;
                case "4": searchSubMenu(); break;
                case "5": back = true; break;
                default: System.out.println("Invalid option!");
            }
        }
    }

    // 1.1 REGISTRATION
    private static void registrationSubMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------------------------------------");
            System.out.println("           STUDENT REGISTRATION           ");
            System.out.println("------------------------------------------");
            System.out.println("1. Add New Student");
            System.out.println("2. View All Students");
            System.out.println("3. Update Student");
            System.out.println("4. Delete Student");
            System.out.println("5. Search Student");
            System.out.println("6. Back");
            System.out.println("------------------------------------------");
            System.out.print("Enter choice (1-6): ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1":
                    Student s = new Student();
                    System.out.println("\n--- [ENTER STUDENT INFORMATION] ---");
                    System.out.print("Student ID (e.g. STU121): "); s.setId(sc.nextLine().trim());
                    System.out.print("GR Number  (e.g. 136086): "); s.setGrNo(sc.nextLine().trim());
                    System.out.print("Roll Number(e.g. 92500527042): "); s.setRollNo(sc.nextLine().trim());
                    System.out.print("Full Name  : "); s.setName(sc.nextLine().trim());
                    System.out.print("Date of Birth (YYYY-MM-DD): "); s.setDob(sc.nextLine().trim());
                    System.out.print("Gender (Male/Female): "); s.setGender(sc.nextLine().trim());
                    System.out.print("Email ID   : "); s.setEmail(sc.nextLine().trim());
                    System.out.print("Phone No   : "); s.setPhone(sc.nextLine().trim());
                    System.out.print("Address    : "); s.setAddress(sc.nextLine().trim());
                    System.out.print("Course Code(e.g. 0obc6678): "); s.setCourse(sc.nextLine().trim());
                    System.out.print("Semester [1-8]: ");
                    try { s.setSemester(Integer.parseInt(sc.nextLine().trim())); } catch (Exception e) { s.setSemester(1); }
                    System.out.print("Division   : "); s.setDivision(sc.nextLine().trim());
                    System.out.print("Admission Year: ");
                    try { s.setAdmissionYear(Integer.parseInt(sc.nextLine().trim())); } catch (Exception e) { s.setAdmissionYear(2024); }

                    if (studentDAO.addStudent(s)) {
                        System.out.println("\nStudent Added Successfully!");
                    } else {
                        System.out.println("\nFailed to Add Student (ID, GR No, or Roll No already exists).");
                    }
                    break;
                case "2":
                    displayStudentsTable(studentDAO.getAllStudents());
                    break;
                case "3":
                    System.out.print("\nEnter Student ID or GR No to Update: ");
                    String uid = sc.nextLine().trim();
                    Student curr = studentDAO.getStudentById(uid);
                    if (curr == null) {
                        System.out.println("Student record not found.");
                        break;
                    }
                    System.out.println("Updating " + curr.getName() + " (Press Enter to keep unchanged)");
                    System.out.print("New Name [" + curr.getName() + "]: ");
                    String nn = sc.nextLine().trim(); if (!nn.isEmpty()) curr.setName(nn);
                    System.out.print("New Phone [" + curr.getPhone() + "]: ");
                    String np = sc.nextLine().trim(); if (!np.isEmpty()) curr.setPhone(np);
                    System.out.print("New Course [" + curr.getCourse() + "]: ");
                    String nc = sc.nextLine().trim(); if (!nc.isEmpty()) curr.setCourse(nc);
                    System.out.print("New Semester [" + curr.getSemester() + "]: ");
                    String ns = sc.nextLine().trim(); if (!ns.isEmpty()) {
                        try { curr.setSemester(Integer.parseInt(ns)); } catch (Exception ignored) {}
                    }

                    if (studentDAO.updateStudent(curr)) System.out.println("Student updated successfully!");
                    else System.out.println("Update failed.");
                    break;
                case "4":
                    System.out.print("\nEnter Student ID or GR No to Delete: ");
                    String did = sc.nextLine().trim();
                    if (studentDAO.deleteStudent(did)) System.out.println("Student record deleted successfully.");
                    else System.out.println("Failed to delete record.");
                    break;
                case "5":
                    searchSubMenu();
                    break;
                case "6":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // 1.2 PROFILE
    private static void profileSubMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------------------------------------");
            System.out.println("             STUDENT PROFILE              ");
            System.out.println("------------------------------------------");
            System.out.println("1. View Complete Profiles (Show All)");
            System.out.println("2. Update Contact Information");
            System.out.println("3. View Admission Information (Show All)");
            System.out.println("4. View Current Academic Information (Show All)");
            System.out.println("5. Back");
            System.out.println("------------------------------------------");
            System.out.print("Enter choice (1-5): ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1":
                    List<Student> all = studentDAO.getAllStudents();
                    System.out.println("\n--- [ALL COMPLETE STUDENT PROFILES] ---");
                    for (Student s : all) {
                        System.out.println("ID: " + s.getId() + " | GR: " + s.getGrNo() + " | Roll: " + s.getRollNo() +
                                " | Name: " + s.getName() + " | Course: " + s.getCourse() + " | Sem: " + s.getSemester() +
                                " | Contact: " + s.getPhone() + " | City: " + s.getAddress());
                    }
                    break;
                case "2":
                    System.out.print("Enter Student ID or GR No to Update: ");
                    String id = sc.nextLine().trim();
                    Student s = studentDAO.getStudentById(id);
                    if (s != null) {
                        System.out.print("New Phone [" + s.getPhone() + "]: ");
                        String p = sc.nextLine().trim(); if (!p.isEmpty()) s.setPhone(p);
                        System.out.print("New Address [" + s.getAddress() + "]: ");
                        String a = sc.nextLine().trim(); if (!a.isEmpty()) s.setAddress(a);
                        studentDAO.updateStudent(s);
                        System.out.println("Profile updated successfully.");
                    } else System.out.println("Student not found.");
                    break;
                case "3":
                    System.out.println("\n--- [ALL ADMISSION RECORDS] ---");
                    studentDAO.getAllStudents().forEach(st -> {
                        System.out.printf("ID: %-8s | GR: %-8s | Name: %-24s | Admission Year: %d | Course: %s\n",
                                st.getId(), st.getGrNo(), st.getName(), st.getAdmissionYear(), st.getCourse());
                    });
                    break;
                case "4":
                    System.out.println("\n--- [ALL CURRENT ACADEMIC RECORDS] ---");
                    studentDAO.getAllStudents().forEach(st -> {
                        System.out.printf("Roll No: %-14s | Name: %-24s | Course: %-10s | Semester: %d | Division: %s\n",
                                st.getRollNo(), st.getName(), st.getCourse(), st.getSemester(), st.getDivision());
                    });
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // 1.3 ACADEMIC INFORMATION
    private static void academicInfoSubMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------------------------------------");
            System.out.println("       STUDENT ACADEMIC INFORMATION       ");
            System.out.println("------------------------------------------");
            System.out.println("1. View All Courses");
            System.out.println("2. View All Semesters & Divisions");
            System.out.println("3. View All Subjects");
            System.out.println("4. View All Academic Performance Records");
            System.out.println("5. Back");
            System.out.println("------------------------------------------");
            System.out.print("Enter choice (1-5): ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1":
                    System.out.println("\n--- [ALL COURSES] ---");
                    courseDAO.getAllCourses().forEach(c -> System.out.printf("Code: %-10s | Name: %-30s | Duration: %d Yrs\n", c.getCode(), c.getName(), c.getDurationYears()));
                    break;
                case "2":
                    System.out.println("\n--- [ALL STUDENT SEMESTERS & DIVISIONS] ---");
                    studentDAO.getAllStudents().forEach(s -> System.out.printf("Student: %-24s | Roll: %-14s | Course: %-8s | Sem: %d | Div: %s\n", s.getName(), s.getRollNo(), s.getCourse(), s.getSemester(), s.getDivision()));
                    break;
                case "3":
                    System.out.println("\n--- [ALL SUBJECTS] ---");
                    subjectDAO.getAllSubjects().forEach(sub -> System.out.printf("Code: %-8s | Name: %-30s | Course: %s | Sem: %d | Credits: %d\n", sub.getCode(), sub.getName(), sub.getCourseCode(), sub.getSemester(), sub.getCredits()));
                    break;
                case "4":
                    System.out.println("\n--- [ALL ACADEMIC PERFORMANCE & RESULTS] ---");
                    List<ExamResult> res = examResultDAO.getAllExamResults();
                    if (res.isEmpty()) System.out.println("No records found.");
                    else res.forEach(r -> System.out.printf("Student: %-22s (%s) | Subject: %-8s | Marks: %.1f/%.1f | Grade: %s\n", r.getStudentName(), r.getStudentId(), r.getSubjectCode(), r.getMarks(), r.getTotal(), r.getGrade()));
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // 1.4 SEARCH
    private static void searchSubMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------------------------------------");
            System.out.println("              STUDENT SEARCH              ");
            System.out.println("------------------------------------------");
            System.out.println("1. Search by Student ID / GR No / Roll No");
            System.out.println("2. Search by Name");
            System.out.println("3. Search by Course");
            System.out.println("4. Search by Semester");
            System.out.println("5. Search by Division");
            System.out.println("6. Back");
            System.out.println("------------------------------------------");
            System.out.print("Enter choice (1-6): ");

            String ch = sc.nextLine().trim();
            if ("6".equals(ch)) { back = true; continue; }

            System.out.print("Enter keyword: ");
            String q = sc.nextLine().trim();
            List<Student> list = new ArrayList<>();

            switch (ch) {
                case "1": list = studentDAO.searchStudents("id", q); break;
                case "2": list = studentDAO.searchStudents("name", q); break;
                case "3": list = studentDAO.searchStudents("course", q); break;
                case "4": list = studentDAO.searchStudents("semester", q); break;
                case "5": list = studentDAO.searchStudents("division", q); break;
                default: System.out.println("Invalid option!"); continue;
            }
            if (list.isEmpty()) System.out.println("No matching records found.");
            else displayStudentsTable(list);
        }
    }

    // 2. ACADEMIC MANAGEMENT
    private static void academicMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------------------------------------");
            System.out.println("           ACADEMIC MANAGEMENT            ");
            System.out.println("------------------------------------------");
            System.out.println("1. Course Management");
            System.out.println("2. Subject Management");
            System.out.println("3. Class Management");
            System.out.println("4. Timetable Management");
            System.out.println("5. Back");
            System.out.println("------------------------------------------");
            System.out.print("Enter choice (1-5): ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1":
                    System.out.println("\n--- [ALL REGISTERED COURSES] ---");
                    courseDAO.getAllCourses().forEach(c -> System.out.printf("Code: %-10s | Name: %-30s | Duration: %d Yrs | Semesters: %d\n", c.getCode(), c.getName(), c.getDurationYears(), c.getTotalSemesters()));
                    System.out.print("\nOptions: [1] Add New Course | [2] Delete Course | [3] Return: ");
                    String copt = sc.nextLine().trim();
                    if ("1".equals(copt)) {
                        System.out.print("Course Code: "); String c = sc.nextLine().trim();
                        System.out.print("Course Name: "); String n = sc.nextLine().trim();
                        System.out.print("Duration (Years): "); int y = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Total Semesters: "); int s = Integer.parseInt(sc.nextLine().trim());
                        courseDAO.addCourse(new Course(c, n, y, s));
                        System.out.println("Course added successfully!");
                    } else if ("2".equals(copt)) {
                        System.out.print("Course Code to delete: ");
                        courseDAO.deleteCourse(sc.nextLine().trim());
                        System.out.println("Course deleted.");
                    }
                    break;
                case "2":
                    System.out.println("\n--- [ALL SUBJECTS] ---");
                    subjectDAO.getAllSubjects().forEach(s -> System.out.printf("Code: %-8s | Name: %-30s | Course: %s | Sem: %d | Credits: %d\n", s.getCode(), s.getName(), s.getCourseCode(), s.getSemester(), s.getCredits()));
                    System.out.print("\nOptions: [1] Add Subject | [2] Delete Subject | [3] Return: ");
                    String sopt = sc.nextLine().trim();
                    if ("1".equals(sopt)) {
                        System.out.print("Subject Code: "); String code = sc.nextLine().trim();
                        System.out.print("Subject Name: "); String name = sc.nextLine().trim();
                        System.out.print("Course Code: "); String cc = sc.nextLine().trim();
                        System.out.print("Semester: "); int sem = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Credits: "); int cr = Integer.parseInt(sc.nextLine().trim());
                        subjectDAO.addSubject(new Subject(code, name, cc, sem, cr));
                        System.out.println("Subject added successfully!");
                    } else if ("2".equals(sopt)) {
                        System.out.print("Subject Code to delete: ");
                        subjectDAO.deleteSubject(sc.nextLine().trim());
                        System.out.println("Subject deleted.");
                    }
                    break;
                case "3":
                    System.out.println("\n--- [ALL CLASSES] ---");
                    classDAO.getAllClasses().forEach(c -> System.out.printf("Class ID: %-10s | Course: %-8s | Sem: %d | Div: %s | Room: %s\n", c.getClassId(), c.getCourseCode(), c.getSemester(), c.getDivision(), c.getRoomNo()));
                    System.out.print("\nOptions: [1] Add Class | [2] Return: ");
                    if ("1".equals(sc.nextLine().trim())) {
                        System.out.print("Class ID: "); String cid = sc.nextLine().trim();
                        System.out.print("Course Code: "); String cc = sc.nextLine().trim();
                        System.out.print("Semester: "); int sem = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Division: "); String div = sc.nextLine().trim();
                        System.out.print("Room No: "); String rm = sc.nextLine().trim();
                        classDAO.addClass(new ClassModel(cid, cc, sem, div, rm));
                        System.out.println("Class registered successfully!");
                    }
                    break;
                case "4":
                    System.out.println("\n--- [ALL TIMETABLES] ---");
                    List<Timetable> tt = timetableDAO.getAllTimetables();
                    if (tt.isEmpty()) System.out.println("No timetable entries found.");
                    else tt.forEach(t -> System.out.printf("Class: %-10s | Day: %-10s | Slot: %-18s | Subject: %s\n", t.getClassId(), t.getDay(), t.getTimeSlot(), t.getSubjectCode()));
                    System.out.print("\nOptions: [1] Add Timetable Slot | [2] Return: ");
                    if ("1".equals(sc.nextLine().trim())) {
                        System.out.print("Class ID: "); String cid = sc.nextLine().trim();
                        System.out.print("Day: "); String d = sc.nextLine().trim();
                        System.out.print("Time Slot: "); String ts = sc.nextLine().trim();
                        System.out.print("Subject Code: "); String sub = sc.nextLine().trim();
                        timetableDAO.addTimetable(new Timetable(0, cid, d, ts, sub));
                        System.out.println("Timetable slot added!");
                    }
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // 3. ATTENDANCE MANAGEMENT
    private static void attendanceMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------------------------------------");
            System.out.println("          ATTENDANCE MANAGEMENT           ");
            System.out.println("------------------------------------------");
            System.out.println("1. View All Attendance Records");
            System.out.println("2. Mark Student Attendance");
            System.out.println("3. Back");
            System.out.println("------------------------------------------");
            System.out.print("Enter choice (1-3): ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1":
                    System.out.println("\n--- [ALL ATTENDANCE RECORDS] ---");
                    List<AttendanceRecord> att = attendanceDAO.getAllAttendance();
                    if (att.isEmpty()) System.out.println("No attendance logs recorded.");
                    else att.forEach(a -> System.out.printf("Student: %-22s (%s) | Date: %-12s | Status: %s\n", a.getStudentName(), a.getStudentId(), a.getDate(), a.getStatus()));
                    break;
                case "2":
                    System.out.print("Enter Student ID: "); String id = sc.nextLine().trim();
                    System.out.print("Enter Date (YYYY-MM-DD): "); String d = sc.nextLine().trim();
                    System.out.print("Enter Status [PRESENT / ABSENT]: "); String st = sc.nextLine().trim().toUpperCase();
                    if (attendanceDAO.markAttendance(id, d, st)) System.out.println("Attendance recorded successfully!");
                    else System.out.println("Failed to record attendance.");
                    break;
                case "3":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // 4. EXAMINATION MANAGEMENT
    private static void examMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------------------------------------");
            System.out.println("     EXAMINATION & RESULT MANAGEMENT      ");
            System.out.println("------------------------------------------");
            System.out.println("1. View All Examination Results");
            System.out.println("2. Enter New Marks");
            System.out.println("3. Back");
            System.out.println("------------------------------------------");
            System.out.print("Enter choice (1-3): ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1":
                    System.out.println("\n--- [ALL EXAMINATION RESULTS] ---");
                    List<ExamResult> allResults = examResultDAO.getAllExamResults();
                    if (allResults.isEmpty()) System.out.println("No examination results found.");
                    else allResults.forEach(r -> System.out.printf("Student: %-22s (%s) | Subject: %-8s | Marks: %.1f/%.1f | Grade: %s\n", r.getStudentName(), r.getStudentId(), r.getSubjectCode(), r.getMarks(), r.getTotal(), r.getGrade()));
                    break;
                case "2":
                    System.out.print("Enter Student ID: "); String id = sc.nextLine().trim();
                    System.out.print("Enter Subject Code: "); String sub = sc.nextLine().trim();
                    System.out.print("Enter Marks Obtained: "); double m = Double.parseDouble(sc.nextLine().trim());
                    System.out.print("Enter Total Marks: "); double t = Double.parseDouble(sc.nextLine().trim());
                    if (examResultDAO.addExamResult(id, sub, m, t)) System.out.println("Exam result saved successfully!");
                    else System.out.println("Failed to save result.");
                    break;
                case "3":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // 5. REPORTS & SEARCH
    private static void reportsMenu() {
        System.out.println("\n==========================================");
        System.out.println("         SYSTEM SUMMARY REPORT            ");
        System.out.println("==========================================");
        System.out.println("Total Students Enrolled : " + studentDAO.count());
        System.out.println("Total Courses Offered   : " + courseDAO.count());
        System.out.println("Total Active Subjects   : " + subjectDAO.count());
        System.out.println("Total Classes Defined   : " + classDAO.count());
        System.out.println("Total Exam Records      : " + examResultDAO.count());
        System.out.println("Total Attendance Logs   : " + attendanceDAO.count());
        System.out.println("==========================================");
    }

    private static void displayStudentsTable(List<Student> list) {
        System.out.println("\n========================================================================================================");
        System.out.printf("%-8s | %-8s | %-14s | %-26s | %-10s | %-4s | %-4s | %-10s\n",
                "ID", "GR NO", "ROLL NO", "STUDENT NAME", "COURSE", "SEM", "DIV", "CONTACT");
        System.out.println("========================================================================================================");
        for (Student s : list) {
            System.out.printf("%-8s | %-8s | %-14s | %-26s | %-10s | %-4d | %-4s | %-10s\n",
                    s.getId(), s.getGrNo(), s.getRollNo(), s.getName(),
                    s.getCourse(), s.getSemester(), s.getDivision(), s.getPhone());
        }
        System.out.println("========================================================================================================");
        System.out.println("Total Records: " + list.size());
    }
}
