-- ========================================================
-- SMS: Student Management System
-- Database Schema for MySQL 8 (InnoDB + utf8mb4)
-- Database Name: student_management_system
-- ========================================================

CREATE DATABASE IF NOT EXISTS student_management_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE student_management_system;

-- 1. Users Table (Authentication)
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(100) NOT NULL PRIMARY KEY,
    password VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Courses Table
CREATE TABLE IF NOT EXISTS courses (
    code VARCHAR(50) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    duration_years INT DEFAULT 3,
    total_semesters INT DEFAULT 6
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Subjects Table
CREATE TABLE IF NOT EXISTS subjects (
    code VARCHAR(50) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    course_code VARCHAR(50),
    semester INT DEFAULT 1,
    credits INT DEFAULT 3,
    CONSTRAINT fk_subjects_course FOREIGN KEY (course_code) 
        REFERENCES courses(code) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Classes Table
CREATE TABLE IF NOT EXISTS classes (
    class_id VARCHAR(50) NOT NULL PRIMARY KEY,
    course_code VARCHAR(50),
    semester INT DEFAULT 1,
    division VARCHAR(10),
    room_no VARCHAR(50),
    CONSTRAINT fk_classes_course FOREIGN KEY (course_code) 
        REFERENCES courses(code) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Timetables Table
CREATE TABLE IF NOT EXISTS timetables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id VARCHAR(50),
    day VARCHAR(20) NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    subject_code VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Students Table
CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    gr_no VARCHAR(50) UNIQUE,
    roll_no VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    dob VARCHAR(50),
    gender VARCHAR(20),
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    course VARCHAR(50),
    semester INT DEFAULT 1,
    division VARCHAR(10),
    admission_year INT DEFAULT 2024
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Attendance Table
CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    date VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) 
        REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uq_student_date (student_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Exam Results Table
CREATE TABLE IF NOT EXISTS exam_results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    subject_code VARCHAR(50) NOT NULL,
    marks DOUBLE NOT NULL DEFAULT 0.0,
    total DOUBLE NOT NULL DEFAULT 100.0,
    grade VARCHAR(10) NOT NULL DEFAULT 'F',
    CONSTRAINT fk_exam_student FOREIGN KEY (student_id) 
        REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================
-- Seed Initial Baseline Records (Idempotent)
-- ========================================================

-- Baseline Administrator (admin / 1111 preserved from existing database)
INSERT INTO users (username, password) VALUES ('admin', '1111')
ON DUPLICATE KEY UPDATE password = VALUES(password);

-- Baseline Courses
INSERT INTO courses (code, name, duration_years, total_semesters) VALUES
('0obc6678', 'Computer Applications (BCA)', 3, 6),
('0oit4421', 'Information Technology (IT)', 4, 8),
('0bca525',  'BscDS', 3, 6)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Baseline Subjects
INSERT INTO subjects (code, name, course_code, semester, credits) VALUES
('CS301', 'Core Java Programming', '0obc6678', 3, 4),
('CS302', 'Database Management System', '0obc6678', 3, 4),
('CS303', 'Data Structures & Algorithms', '0obc6678', 3, 3),
('CS304', 'Web Technology', '0obc6678', 3, 3)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Baseline Classes
INSERT INTO classes (class_id, course_code, semester, division, room_no) VALUES
('BCA-3A', '0obc6678', 3, 'A', 'Room 101'),
('BCA-3B', '0obc6678', 3, 'B', 'Room 102'),
('BCA-3C', '0obc6678', 3, 'C', 'Room 103')
ON DUPLICATE KEY UPDATE room_no = VALUES(room_no);

-- Baseline Timetable
INSERT INTO timetables (id, class_id, day, time_slot, subject_code) VALUES
(1, 'BCA-3A', 'Monday', '09:00 - 10:00 AM', 'CS301'),
(2, 'BCA-3A', 'Monday', '10:00 - 11:00 AM', 'CS302'),
(3, 'BCA-3A', 'Tuesday', '09:00 - 10:00 AM', 'CS303'),
(4, 'BCA-3A', 'Tuesday', '10:00 - 11:00 AM', 'CS304'),
(5, 'BSC-003', 'Thursday', '11:10-01:20', 'CS001')
ON DUPLICATE KEY UPDATE time_slot = VALUES(time_slot);

-- Baseline Students (20 Preserved Records)
INSERT INTO students (id, gr_no, roll_no, name, dob, gender, email, phone, address, course, semester, division, admission_year) VALUES
('STU101', '136066', '92500527022', 'Kartik Rakeshbhai Pandya', '2003-05-14', 'Male', 'kartik.pandya@example.com', '9876543210', 'Ahmedabad', '0obc6678', 3, 'A', 2023),
('STU102', '136067', '92500527023', 'Rohan Suresh Patel', '2003-08-21', 'Male', 'rohan.patel@example.com', '9876543211', 'Surat', '0obc6678', 3, 'A', 2023),
('STU103', '136068', '92500527024', 'Priya Mukesh Sharma', '2004-01-10', 'Female', 'priya.sharma@example.com', '9876543212', 'Vadodara', '0obc6678', 3, 'A', 2023),
('STU104', '136069', '92500527025', 'Aman Dipak Joshi', '2003-11-19', 'Male', 'aman.joshi@example.com', '9876543213', 'Rajkot', '0obc6678', 3, 'A', 2023),
('STU105', '136070', '92500527026', 'Neha Pravin Desai', '2004-03-05', 'Female', 'neha.desai@example.com', '9876543214', 'Bhavnagar', '0obc6678', 3, 'A', 2023),
('STU106', '136071', '92500527027', 'Harsh Vijay Mehta', '2003-07-28', 'Male', 'harsh.mehta@example.com', '9876543215', 'Jamnagar', '0obc6678', 3, 'A', 2023),
('STU107', '136072', '92500527028', 'Drashti Chetan Shah', '2004-09-12', 'Female', 'drashti.shah@example.com', '9876543216', 'Gandhinagar', '0obc6678', 3, 'A', 2023),
('STU108', '136073', '92500527029', 'Smit Sanjay Vora', '2003-12-04', 'Male', 'smit.vora@example.com', '9876543217', 'Junagadh', '0obc6678', 3, 'B', 2023),
('STU109', '136074', '92500527030', 'Anjali Rajesh Bhatt', '2004-04-18', 'Female', 'anjali.bhatt@example.com', '9876543218', 'Anand', '0obc6678', 3, 'B', 2023),
('STU110', '136075', '92500527031', 'Darshan Manoj Dave', '2003-06-25', 'Male', 'darshan.dave@example.com', '9876543219', 'Nadiad', '0obc6678', 3, 'B', 2023),
('STU111', '136076', '92500527032', 'Kinjal Arvind Chauhan', '2004-02-14', 'Female', 'kinjal.c@example.com', '9876543220', 'Mehsana', '0obc6678', 3, 'B', 2023),
('STU112', '136077', '92500527033', 'Yash Bharat Solanki', '2003-10-30', 'Male', 'yash.solanki@example.com', '9876543221', 'Patan', '0obc6678', 3, 'B', 2023),
('STU113', '136078', '92500527034', 'Pooja Jayesh Trivedi', '2004-05-22', 'Female', 'pooja.trivedi@example.com', '9876543222', 'Navsari', '0obc6678', 3, 'C', 2023),
('STU114', '136079', '92500527035', 'Keval Hitesh Rathod', '2003-04-11', 'Male', 'keval.rathod@example.com', '9876543223', 'Valsad', '0obc6678', 3, 'C', 2023),
('STU115', '136080', '92500527036', 'Riddhi Nilesh Raval', '2004-08-16', 'Female', 'riddhi.raval@example.com', '9876543224', 'Morbi', '0obc6678', 3, 'C', 2023),
('STU116', '136081', '92500527037', 'Vivek Naresh Makwana', '2003-03-09', 'Male', 'vivek.makwana@example.com', '9876543225', 'Porbandar', '0obc6678', 3, 'C', 2023),
('STU117', '136082', '92500527038', 'Diya Sandip Modi', '2004-10-07', 'Female', 'diya.modi@example.com', '9876543226', 'Surendranagar', '0obc6678', 3, 'C', 2023),
('STU118', '136083', '92500527039', 'Jayesh Kirit Parekh', '2003-09-15', 'Male', 'jayesh.parekh@example.com', '9876543227', 'Bharuch', '0obc6678', 3, 'A', 2023),
('STU119', '136084', '92500527040', 'Khushi Paresh Soni', '2004-07-03', 'Female', 'khushi.soni@example.com', '9876543228', 'Amreli', '0obc6678', 3, 'A', 2023),
('STU120', '136085', '92500527041', 'Manan Jagdish Goswami', '2003-01-29', 'Male', 'manan.g@example.com', '9876543229', 'Godhra', '0obc6678', 3, 'B', 2023),
('STU22',  '136066', '92500527022', 'NAMRA K. POPAT', '2008-05-20', 'Male', 'namra.popat@gmail.com', '9099037370', 'ranchhod nagar rajkot', '0oit4421', 3, 'A', 2025)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Baseline Attendance Logs
INSERT INTO attendance (id, student_id, date, status) VALUES
(1, 'STU101', '2026-10-09', 'PRESENT'),
(2, 'STU101', '2026-10-10', 'PRESENT'),
(3, 'STU101', '2026-10-11', 'PRESENT'),
(4, 'STU102', '2026-10-11', 'PRESENT'),
(5, 'STU103', '2026-10-11', 'PRESENT'),
(6, 'STU104', '2026-10-11', 'ABSENT'),
(7, 'STU101', '2026-10-15', 'PRESENT'),
(8, 'STU101', '2026-10-22', 'PRESENT'),
(9, 'STU103', '2026-10-22', 'PRESENT'),
(10, 'STU104', '2026-10-22', 'PRESENT'),
(11, 'STU105', '2026-10-22', 'ABSENT'),
(12, 'STU106', '2026-10-22', 'PRESENT'),
(13, 'STU107', '2026-10-22', 'ABSENT'),
(14, 'STU118', '2026-10-22', 'PRESENT'),
(15, 'STU119', '2026-10-22', 'PRESENT'),
(16, 'STU22',  '2026-10-22', 'PRESENT')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- Baseline Exam Results
INSERT INTO exam_results (id, student_id, subject_code, marks, total, grade) VALUES
(1, 'STU101', 'CS301', 95.0, 100.0, 'A+'),
(2, 'STU101', 'CS302', 88.0, 100.0, 'A'),
(3, 'STU102', 'CS301', 91.0, 100.0, 'A+'),
(4, 'STU103', 'CS301', 89.5, 100.0, 'A'),
(5, 'STU104', 'CS301', 78.0, 100.0, 'B'),
(6, 'STU105', 'CS301', 84.0, 100.0, 'A'),
(7, 'STU101', 'CS303', 92.0, 100.0, 'A+'),
(8, 'STU101', 'CS301', 70.0, 100.0, 'B')
ON DUPLICATE KEY UPDATE marks = VALUES(marks);
