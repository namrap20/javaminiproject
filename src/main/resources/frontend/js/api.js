/**
 * api.js - Java Backend REST API Client
 * Centralized, robust HTTP communication with error handling and JSON parsing.
 */

const API_BASE = '';

const API = {
    async request(endpoint, options = {}) {
        const url = `${API_BASE}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...(options.headers || {})
            },
            ...options
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const message = (data && data.message) ? data.message : `HTTP Error ${response.status}: ${response.statusText}`;
                throw new Error(message);
            }
            return data;
        } catch (error) {
            console.error(`API Call failed: ${endpoint}`, error);
            throw error;
        }
    },

    // 1. Authentication
    auth: {
        login(username, password) {
            return API.request('/api/auth/login', {
                method: 'POST',
                body: JSON.stringify({ username, password })
            });
        },
        logout() {
            return API.request('/api/auth/logout', { method: 'POST' });
        },
        changePassword(username, password) {
            return API.request('/api/auth/change-password', {
                method: 'POST',
                body: JSON.stringify({ username, password })
            });
        }
    },

    // 2. Students CRUD & Search
    students: {
        getAll(params = {}) {
            const query = new URLSearchParams(params).toString();
            return API.request(`/api/students${query ? '?' + query : ''}`);
        },
        getById(id) {
            return API.request(`/api/students/${encodeURIComponent(id)}`);
        },
        add(studentData) {
            return API.request('/api/students', {
                method: 'POST',
                body: JSON.stringify(studentData)
            });
        },
        update(id, studentData) {
            return API.request(`/api/students/${encodeURIComponent(id)}`, {
                method: 'PUT',
                body: JSON.stringify(studentData)
            });
        },
        delete(id) {
            return API.request(`/api/students/${encodeURIComponent(id)}`, {
                method: 'DELETE'
            });
        },
        search(query) {
            return API.request(`/api/students?search=${encodeURIComponent(query)}`);
        }
    },

    // 3. Academic (Courses, Subjects, Classes, Timetable)
    academic: {
        // Courses
        getCourses() {
            return API.request('/api/courses');
        },
        addCourse(courseData) {
            return API.request('/api/courses', {
                method: 'POST',
                body: JSON.stringify(courseData)
            });
        },
        deleteCourse(code) {
            return API.request(`/api/courses/${encodeURIComponent(code)}`, {
                method: 'DELETE'
            });
        },

        // Subjects
        getSubjects() {
            return API.request('/api/subjects');
        },
        addSubject(subjectData) {
            return API.request('/api/subjects', {
                method: 'POST',
                body: JSON.stringify(subjectData)
            });
        },
        deleteSubject(code) {
            return API.request(`/api/subjects/${encodeURIComponent(code)}`, {
                method: 'DELETE'
            });
        },

        // Classes
        getClasses() {
            return API.request('/api/classes');
        },
        addClass(classData) {
            return API.request('/api/classes', {
                method: 'POST',
                body: JSON.stringify(classData)
            });
        },
        deleteClass(classId) {
            return API.request(`/api/classes/${encodeURIComponent(classId)}`, {
                method: 'DELETE'
            });
        },

        // Timetable
        getTimetables() {
            return API.request('/api/timetables');
        },
        addTimetable(slotData) {
            return API.request('/api/timetables', {
                method: 'POST',
                body: JSON.stringify(slotData)
            });
        },
        deleteTimetable(id) {
            return API.request(`/api/timetables/${id}`, {
                method: 'DELETE'
            });
        }
    },

    // 4. Attendance
    attendance: {
        getAll(studentId = null) {
            const q = studentId ? `?studentId=${encodeURIComponent(studentId)}` : '';
            return API.request(`/api/attendance${q}`);
        },
        getStats() {
            return API.request('/api/attendance/stats');
        },
        mark(studentId, date, status) {
            return API.request('/api/attendance', {
                method: 'POST',
                body: JSON.stringify({ student_id: studentId, date, status })
            });
        },
        delete(id) {
            return API.request(`/api/attendance/${id}`, {
                method: 'DELETE'
            });
        }
    },

    // 5. Examination & Results
    exams: {
        getAll(studentId = null) {
            const q = studentId ? `?studentId=${encodeURIComponent(studentId)}` : '';
            return API.request(`/api/results${q}`);
        },
        getMarksheet(studentId) {
            return API.request(`/api/results/marksheet/${encodeURIComponent(studentId)}`);
        },
        getStats() {
            return API.request('/api/results/stats');
        },
        addResult(resultData) {
            return API.request('/api/results', {
                method: 'POST',
                body: JSON.stringify(resultData)
            });
        },
        updateResult(id, resultData) {
            return API.request(`/api/results/${id}`, {
                method: 'PUT',
                body: JSON.stringify(resultData)
            });
        },
        deleteResult(id) {
            return API.request(`/api/results/${id}`, {
                method: 'DELETE'
            });
        }
    },

    // 6. Reports & System Stats
    reports: {
        getDashboardStats() {
            return API.request('/api/stats');
        },
        getCoursesReport() {
            return API.request('/api/reports/courses');
        },
        getSemestersReport() {
            return API.request('/api/reports/semesters');
        },
        getDivisionsReport() {
            return API.request('/api/reports/divisions');
        }
    }
};

window.API = API;
