/**
 * students.js - Complete Student Management Module
 * Data Table, CRUD, Filters, Sorting, Pagination, CSV Export, Profile View
 */

const StudentsModule = {
    students: [],
    filteredStudents: [],
    currentPage: 1,
    pageSize: 10,
    sortField: 'id',
    sortAsc: true,
    coursesList: [],

    async init() {
        await this.loadCoursesDropdown();
        await this.loadStudents();
    },

    async loadCoursesDropdown() {
        try {
            const courses = await API.academic.getCourses();
            this.coursesList = courses || [];
            
            // Populate Add/Edit student form select
            const courseSelect = document.getElementById('student-form-course');
            const filterSelect = document.getElementById('student-filter-course');
            
            if (courseSelect) {
                courseSelect.innerHTML = '<option value="">-- Select Course --</option>' +
                    this.coursesList.map(c => `<option value="${escapeHtml(c.code)}">${escapeHtml(c.name)} (${escapeHtml(c.code)})</option>`).join('');
            }
            if (filterSelect) {
                filterSelect.innerHTML = '<option value="">All Courses</option>' +
                    this.coursesList.map(c => `<option value="${escapeHtml(c.code)}">${escapeHtml(c.name)}</option>`).join('');
            }
        } catch (err) {
            console.error('Failed to load courses for dropdown:', err);
        }
    },

    async loadStudents() {
        const tableBody = document.getElementById('students-table-body');
        const emptyState = document.getElementById('students-empty-state');
        if (tableBody) {
            tableBody.innerHTML = `<tr><td colspan="13"><div class="loading-state"><div class="spinner"></div><span>Loading students from database...</span></div></td></tr>`;
        }

        try {
            const data = await API.students.getAll();
            this.students = data || [];
            this.applyFiltersAndRender();
        } catch (err) {
            if (tableBody) {
                tableBody.innerHTML = `<tr><td colspan="13" style="text-align:center;padding:2rem;color:var(--danger);">Error loading students: ${escapeHtml(err.message)}</td></tr>`;
            }
            showToast('Database Error', 'Unable to load students from database.', 'danger');
        }
    },

    searchStudents(query) {
        const searchInput = document.getElementById('students-search-input');
        if (searchInput) searchInput.value = query;
        this.applyFiltersAndRender();
    },

    applyFiltersAndRender() {
        const query = (document.getElementById('students-search-input')?.value || '').toLowerCase().trim();
        const courseFilter = document.getElementById('student-filter-course')?.value || '';
        const semFilter = document.getElementById('student-filter-semester')?.value || '';
        const divFilter = document.getElementById('student-filter-division')?.value || '';

        this.filteredStudents = this.students.filter(s => {
            // Text Search
            const matchesQuery = !query ||
                (s.id && s.id.toLowerCase().includes(query)) ||
                (s.gr_no && s.gr_no.toLowerCase().includes(query)) ||
                (s.roll_no && s.roll_no.toLowerCase().includes(query)) ||
                (s.name && s.name.toLowerCase().includes(query)) ||
                (s.email && s.email.toLowerCase().includes(query)) ||
                (s.phone && s.phone.toLowerCase().includes(query)) ||
                (s.address && s.address.toLowerCase().includes(query));

            // Filters
            const matchesCourse = !courseFilter || s.course === courseFilter;
            const matchesSem = !semFilter || String(s.semester) === String(semFilter);
            const matchesDiv = !divFilter || (s.division && s.division.toUpperCase() === divFilter.toUpperCase());

            return matchesQuery && matchesCourse && matchesSem && matchesDiv;
        });

        // Sort
        this.filteredStudents.sort((a, b) => {
            let valA = a[this.sortField] || '';
            let valB = b[this.sortField] || '';
            if (typeof valA === 'number' && typeof valB === 'number') {
                return this.sortAsc ? valA - valB : valB - valA;
            }
            return this.sortAsc ? String(valA).localeCompare(String(valB)) : String(valB).localeCompare(String(valA));
        });

        this.currentPage = 1;
        this.renderTable();
    },

    renderTable() {
        const tableBody = document.getElementById('students-table-body');
        const emptyState = document.getElementById('students-empty-state');
        const countDisplay = document.getElementById('students-count-display');

        if (!tableBody) return;

        const total = this.filteredStudents.length;
        if (countDisplay) {
            countDisplay.textContent = `Showing ${total} of ${this.students.length} students in database`;
        }

        if (total === 0) {
            tableBody.innerHTML = '';
            if (emptyState) emptyState.style.display = 'flex';
            this.renderPagination(0);
            return;
        }

        if (emptyState) emptyState.style.display = 'none';

        // Pagination slice
        const start = (this.currentPage - 1) * this.pageSize;
        const end = Math.min(start + this.pageSize, total);
        const pageItems = this.filteredStudents.slice(start, end);

        tableBody.innerHTML = pageItems.map(s => `
            <tr>
                <td><strong>${escapeHtml(s.id)}</strong></td>
                <td>${escapeHtml(s.gr_no || '-')}</td>
                <td>${escapeHtml(s.roll_no || '-')}</td>
                <td><strong>${escapeHtml(s.name)}</strong></td>
                <td>${escapeHtml(s.dob || '-')}</td>
                <td><span class="badge ${s.gender === 'Female' ? 'badge-primary' : 'badge-neutral'}">${escapeHtml(s.gender || '-')}</span></td>
                <td><span style="font-size:0.8rem;color:var(--text-muted);">${escapeHtml(s.email || '-')}</span></td>
                <td>${escapeHtml(s.phone || '-')}</td>
                <td><span class="badge badge-primary">${escapeHtml(s.course || '-')}</span></td>
                <td>Sem ${escapeHtml(s.semester)}</td>
                <td><span class="badge badge-neutral">${escapeHtml(s.division || '-')}</span></td>
                <td>${escapeHtml(s.admission_year)}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn" title="View Profile" onclick="StudentsModule.viewProfile('${escapeHtml(s.id)}')">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-width="2" d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3" stroke-width="2"/></svg>
                        </button>
                        <button class="action-btn" title="Edit Student" onclick="StudentsModule.openEditModal('${escapeHtml(s.id)}')">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-width="2" d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path stroke-width="2" d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                        </button>
                        <button class="action-btn delete" title="Delete Student" onclick="StudentsModule.confirmDelete('${escapeHtml(s.id)}', '${escapeHtml(s.name)}')">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline stroke-width="2" points="3 6 5 6 21 6"/><path stroke-width="2" d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17" stroke-width="2"/><line x1="14" y1="11" x2="14" y2="17" stroke-width="2"/></svg>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

        this.renderPagination(total);
    },

    renderPagination(total) {
        const container = document.getElementById('students-pagination');
        if (!container) return;

        const totalPages = Math.ceil(total / this.pageSize) || 1;
        let html = `
            <div style="display:flex;align-items:center;gap:0.5rem;font-size:0.825rem;color:var(--text-muted);">
                <span>Page ${this.currentPage} of ${totalPages}</span>
                <button class="btn btn-secondary btn-sm" ${this.currentPage <= 1 ? 'disabled' : ''} onclick="StudentsModule.changePage(${this.currentPage - 1})">Prev</button>
                <button class="btn btn-secondary btn-sm" ${this.currentPage >= totalPages ? 'disabled' : ''} onclick="StudentsModule.changePage(${this.currentPage + 1})">Next</button>
            </div>
        `;
        container.innerHTML = html;
    },

    changePage(page) {
        this.currentPage = page;
        this.renderTable();
    },

    openAddModal() {
        this.loadCoursesDropdown();
        const form = document.getElementById('student-crud-form');
        if (form) form.reset();
        
        document.getElementById('student-modal-title').textContent = 'Add New Student';
        document.getElementById('student-form-id').disabled = false;
        document.getElementById('student-form-mode').value = 'add';
        
        // Suggest Next STU ID
        if (this.students.length > 0) {
            const nextNum = this.students.length + 101;
            document.getElementById('student-form-id').value = 'STU' + nextNum;
        }

        showModal('modal-student-form');
    },

    async openEditModal(studentId) {
        await this.loadCoursesDropdown();
        try {
            const s = await API.students.getById(studentId);
            if (!s) {
                showToast('Error', 'Student record not found.', 'danger');
                return;
            }

            document.getElementById('student-modal-title').textContent = `Edit Student: ${s.name}`;
            document.getElementById('student-form-mode').value = 'edit';
            
            const idInput = document.getElementById('student-form-id');
            idInput.value = s.id;
            idInput.disabled = true; // Cannot alter Primary Key ID

            document.getElementById('student-form-name').value = s.name || '';
            document.getElementById('student-form-gr').value = s.gr_no || '';
            document.getElementById('student-form-roll').value = s.roll_no || '';
            document.getElementById('student-form-dob').value = s.dob || '';
            document.getElementById('student-form-gender').value = s.gender || 'Male';
            document.getElementById('student-form-email').value = s.email || '';
            document.getElementById('student-form-phone').value = s.phone || '';
            document.getElementById('student-form-address').value = s.address || '';
            document.getElementById('student-form-course').value = s.course || '';
            document.getElementById('student-form-semester').value = s.semester || 1;
            document.getElementById('student-form-division').value = s.division || 'A';
            document.getElementById('student-form-year').value = s.admission_year || 2024;

            showModal('modal-student-form');
        } catch (err) {
            showToast('Error', 'Failed to retrieve student: ' + err.message, 'danger');
        }
    },

    async handleFormSubmit(e) {
        e.preventDefault();
        const mode = document.getElementById('student-form-mode').value;
        const id = document.getElementById('student-form-id').value.trim();
        const name = document.getElementById('student-form-name').value.trim();
        const grNo = document.getElementById('student-form-gr').value.trim();
        const rollNo = document.getElementById('student-form-roll').value.trim();
        const dob = document.getElementById('student-form-dob').value.trim();
        const gender = document.getElementById('student-form-gender').value;
        const email = document.getElementById('student-form-email').value.trim();
        const phone = document.getElementById('student-form-phone').value.trim();
        const address = document.getElementById('student-form-address').value.trim();
        const course = document.getElementById('student-form-course').value.trim();
        const semester = parseInt(document.getElementById('student-form-semester').value) || 1;
        const division = document.getElementById('student-form-division').value.trim();
        const admissionYear = parseInt(document.getElementById('student-form-year').value) || 2024;

        if (!id || !name || !grNo || !rollNo) {
            showToast('Validation Error', 'ID, Name, GR Number, and Roll Number are required.', 'warning');
            return;
        }

        const studentData = {
            id, name, gr_no: grNo, roll_no: rollNo, dob, gender,
            email, phone, address, course, semester, division, admission_year: admissionYear
        };

        const submitBtn = document.getElementById('student-form-submit-btn');
        submitBtn.disabled = true;

        try {
            if (mode === 'add') {
                const res = await API.students.add(studentData);
                if (res.success) {
                    showToast('Success', 'Student added successfully to database.', 'success');
                    closeModal('modal-student-form');
                    await this.loadStudents();
                } else {
                    showToast('Add Failed', res.message, 'danger');
                }
            } else {
                const res = await API.students.update(id, studentData);
                if (res.success) {
                    showToast('Success', 'Student updated successfully.', 'success');
                    closeModal('modal-student-form');
                    await this.loadStudents();
                } else {
                    showToast('Update Failed', res.message, 'danger');
                }
            }
        } catch (err) {
            showToast('Database Error', err.message, 'danger');
        } finally {
            submitBtn.disabled = false;
        }
    },

    confirmDelete(id, name) {
        showConfirmDialog(
            'Delete Student?',
            `Are you sure you want to permanently delete student ${name} (${id})? This will also remove associated exam and attendance records in accordance with database cascade constraints.`,
            async () => {
                try {
                    const res = await API.students.delete(id);
                    if (res.success) {
                        showToast('Deleted', `Student ${id} has been permanently removed from database.`, 'success');
                        await this.loadStudents();
                    } else {
                        showToast('Delete Failed', res.message, 'danger');
                    }
                } catch (err) {
                    showToast('Error', err.message, 'danger');
                }
            }
        );
    },

    async viewProfile(studentId) {
        navigateTo('student-profile', { studentId });
    },

    async loadProfile(studentId) {
        const container = document.getElementById('student-profile-content');
        if (!container) return;

        container.innerHTML = `<div class="loading-state"><div class="spinner"></div><span>Loading student profile and marksheet...</span></div>`;

        try {
            const student = await API.students.getById(studentId);
            if (!student) {
                container.innerHTML = `<div class="empty-state"><h4>Student not found</h4><p>Unable to locate student with ID ${escapeHtml(studentId)}.</p></div>`;
                return;
            }

            // Fetch marksheet & attendance for this student
            const marksheet = await API.exams.getMarksheet(studentId).catch(() => null);
            const attendanceLogs = await API.attendance.getAll(studentId).catch(() => []);

            let presentCount = 0;
            if (Array.isArray(attendanceLogs)) {
                presentCount = attendanceLogs.filter(a => a.status === 'PRESENT').length;
            }
            const attPct = attendanceLogs && attendanceLogs.length > 0 ?
                Math.round((presentCount / attendanceLogs.length) * 100) : 0;

            let initials = student.name ? student.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() : 'ST';

            let marksRows = '';
            if (marksheet && marksheet.results && marksheet.results.length > 0) {
                marksRows = marksheet.results.map(r => `
                    <tr>
                        <td><strong>${escapeHtml(r.subject)}</strong></td>
                        <td>${r.marks}</td>
                        <td>${r.total}</td>
                        <td><span class="badge ${r.grade === 'A+' || r.grade === 'A' ? 'badge-success' : r.grade === 'F' ? 'badge-danger' : 'badge-primary'}">${escapeHtml(r.grade)}</span></td>
                    </tr>
                `).join('');
            } else {
                marksRows = `<tr><td colspan="4" style="text-align:center;color:var(--text-muted);padding:1.5rem;">No exam results recorded for this student yet.</td></tr>`;
            }

            container.innerHTML = `
                <div class="card">
                    <div class="profile-hero">
                        <div class="profile-avatar-lg">${initials}</div>
                        <div style="flex:1;">
                            <div style="display:flex;align-items:center;gap:0.75rem;flex-wrap:wrap;">
                                <h2 style="font-size:1.6rem;font-weight:700;">${escapeHtml(student.name)}</h2>
                                <span class="badge badge-primary">${escapeHtml(student.id)}</span>
                                <span class="badge badge-neutral">GR: ${escapeHtml(student.gr_no || '-')}</span>
                                <span class="badge badge-neutral">Roll: ${escapeHtml(student.roll_no || '-')}</span>
                            </div>
                            <p style="color:var(--text-muted);margin-top:0.35rem;">
                                Course: <strong>${escapeHtml(student.course)}</strong> | Semester: <strong>${escapeHtml(student.semester)}</strong> | Division: <strong>${escapeHtml(student.division)}</strong>
                            </p>
                        </div>
                        <div style="display:flex;gap:0.5rem;">
                            <button class="btn btn-secondary" onclick="navigateTo('students')">Back to List</button>
                            <button class="btn btn-primary" onclick="StudentsModule.openEditModal('${escapeHtml(student.id)}')">Edit Profile</button>
                        </div>
                    </div>

                    <div class="profile-details-grid">
                        <!-- Personal Info -->
                        <div class="card" style="box-shadow:none;">
                            <div class="card-header"><h3>Personal Information</h3></div>
                            <div class="card-body">
                                <ul class="info-list">
                                    <li class="info-item"><span class="label">Date of Birth</span><span class="val">${escapeHtml(student.dob || '-')}</span></li>
                                    <li class="info-item"><span class="label">Gender</span><span class="val">${escapeHtml(student.gender || '-')}</span></li>
                                    <li class="info-item"><span class="label">Email Address</span><span class="val">${escapeHtml(student.email || '-')}</span></li>
                                    <li class="info-item"><span class="label">Phone Number</span><span class="val">${escapeHtml(student.phone || '-')}</span></li>
                                    <li class="info-item"><span class="label">City / Address</span><span class="val">${escapeHtml(student.address || '-')}</span></li>
                                </ul>
                            </div>
                        </div>

                        <!-- Academic & Admission Info -->
                        <div class="card" style="box-shadow:none;">
                            <div class="card-header"><h3>Admission & Academic Info</h3></div>
                            <div class="card-body">
                                <ul class="info-list">
                                    <li class="info-item"><span class="label">General Register (GR)</span><span class="val">${escapeHtml(student.gr_no || '-')}</span></li>
                                    <li class="info-item"><span class="label">Class Roll Number</span><span class="val">${escapeHtml(student.roll_no || '-')}</span></li>
                                    <li class="info-item"><span class="label">Enrolled Course</span><span class="val">${escapeHtml(student.course || '-')}</span></li>
                                    <li class="info-item"><span class="label">Current Semester</span><span class="val">Semester ${escapeHtml(student.semester)}</span></li>
                                    <li class="info-item"><span class="label">Division / Section</span><span class="val">Division ${escapeHtml(student.division || '-')}</span></li>
                                    <li class="info-item"><span class="label">Admission Year</span><span class="val">${escapeHtml(student.admission_year)}</span></li>
                                    <li class="info-item"><span class="label">Attendance Rate</span><span class="val"><span class="badge ${attPct >= 75 ? 'badge-success' : 'badge-warning'}">${attPct}% (${presentCount}/${attendanceLogs ? attendanceLogs.length : 0} Days)</span></span></li>
                                </ul>
                            </div>
                        </div>
                    </div>

                    <!-- Academic Performance / Marksheet -->
                    <div style="padding:0 1.5rem 1.5rem 1.5rem;">
                        <div class="card" style="box-shadow:none;">
                            <div class="card-header">
                                <h3>Academic Marksheet & Examination Performance</h3>
                                ${marksheet && marksheet.percentage ? `<span class="badge badge-success" style="font-size:0.875rem;">Overall: ${marksheet.percentage}% (${marksheet.overall_grade})</span>` : ''}
                            </div>
                            <div class="card-body" style="padding:0;">
                                <div class="table-container" style="border:none;">
                                    <table class="data-table">
                                        <thead>
                                            <tr>
                                                <th>Subject Code</th>
                                                <th>Marks Obtained</th>
                                                <th>Max Marks</th>
                                                <th>Grade</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            ${marksRows}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = `<div class="empty-state"><h4>Error loading profile</h4><p>${escapeHtml(err.message)}</p></div>`;
        }
    },

    exportCSV() {
        if (!this.students || this.students.length === 0) {
            showToast('Warning', 'No student records available to export.', 'warning');
            return;
        }

        const headers = ["Student ID", "GR No", "Roll No", "Name", "DOB", "Gender", "Email", "Phone", "Address", "Course", "Semester", "Division", "Admission Year"];
        let csv = headers.join(",") + "\n";

        for (const s of this.students) {
            const row = [
                `"${s.id || ''}"`,
                `"${s.gr_no || ''}"`,
                `"${s.roll_no || ''}"`,
                `"${(s.name || '').replace(/"/g, '""')}"`,
                `"${s.dob || ''}"`,
                `"${s.gender || ''}"`,
                `"${s.email || ''}"`,
                `"${s.phone || ''}"`,
                `"${(s.address || '').replace(/"/g, '""')}"`,
                `"${s.course || ''}"`,
                s.semester || 1,
                `"${s.division || ''}"`,
                s.admission_year || 2024
            ];
            csv += row.join(",") + "\n";
        }

        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement("a");
        const url = URL.createObjectURL(blob);
        link.setAttribute("href", url);
        link.setAttribute("download", `Students_Export_${new Date().toISOString().slice(0, 10)}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        showToast('Export Complete', 'Exported student records to CSV.', 'success');
    }
};

window.StudentsModule = StudentsModule;
