/**
 * academic.js - Academic Management Module
 * Courses, Subjects, Classes, and Timetable tabs with full CRUD
 */

const AcademicModule = {
    currentTab: 'courses',

    init() {
        this.switchTab('courses');
    },

    loadCurrentTab() {
        this.switchTab(this.currentTab);
    },

    switchTab(tabName) {
        this.currentTab = tabName;
        document.querySelectorAll('.academic-tab-btn').forEach(btn => {
            btn.classList.remove('active');
            if (btn.getAttribute('data-tab') === tabName) btn.classList.add('active');
        });

        document.querySelectorAll('.academic-tab-content').forEach(c => c.style.display = 'none');
        const target = document.getElementById(`academic-tab-${tabName}`);
        if (target) target.style.display = 'block';

        switch (tabName) {
            case 'courses': this.loadCourses(); break;
            case 'subjects': this.loadSubjects(); break;
            case 'classes': this.loadClasses(); break;
            case 'timetables': this.loadTimetables(); break;
        }
    },

    // 1. Courses
    async loadCourses() {
        const tbody = document.getElementById('academic-courses-tbody');
        if (tbody) tbody.innerHTML = `<tr><td colspan="5"><div class="loading-state"><div class="spinner"></div><span>Loading courses...</span></div></td></tr>`;
        try {
            const list = await API.academic.getCourses();
            if (!list || list.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--text-muted);">No courses found.</td></tr>`;
                return;
            }
            tbody.innerHTML = list.map(c => `
                <tr>
                    <td><strong>${escapeHtml(c.code)}</strong></td>
                    <td>${escapeHtml(c.name)}</td>
                    <td>${c.duration_years} Years</td>
                    <td>${c.total_semesters} Semesters</td>
                    <td>
                        <button class="action-btn delete" title="Delete Course" onclick="AcademicModule.deleteCourse('${escapeHtml(c.code)}')">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline stroke-width="2" points="3 6 5 6 21 6"/><path stroke-width="2" d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:var(--danger);padding:2rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
        }
    },

    openAddCourseModal() {
        const form = document.getElementById('add-course-form');
        if (form) form.reset();
        showModal('modal-add-course');
    },

    async handleAddCourse(e) {
        e.preventDefault();
        const code = document.getElementById('course-code-input').value.trim();
        const name = document.getElementById('course-name-input').value.trim();
        const duration = parseInt(document.getElementById('course-duration-input').value) || 3;
        const semesters = parseInt(document.getElementById('course-semesters-input').value) || 6;

        try {
            const res = await API.academic.addCourse({ code, name, duration_years: duration, total_semesters: semesters });
            if (res.success) {
                showToast('Success', 'Course created successfully.', 'success');
                closeModal('modal-add-course');
                this.loadCourses();
                StudentsModule.loadCoursesDropdown();
            } else {
                showToast('Failed', res.message, 'danger');
            }
        } catch (err) {
            showToast('Error', err.message, 'danger');
        }
    },

    deleteCourse(code) {
        showConfirmDialog('Delete Course', `Permanently delete course ${code}?`, async () => {
            try {
                const res = await API.academic.deleteCourse(code);
                if (res.success) {
                    showToast('Deleted', 'Course removed.', 'success');
                    this.loadCourses();
                    StudentsModule.loadCoursesDropdown();
                } else {
                    showToast('Failed', res.message, 'danger');
                }
            } catch (err) {
                showToast('Error', err.message, 'danger');
            }
        });
    },

    // 2. Subjects
    async loadSubjects() {
        const tbody = document.getElementById('academic-subjects-tbody');
        if (tbody) tbody.innerHTML = `<tr><td colspan="6"><div class="loading-state"><div class="spinner"></div><span>Loading subjects...</span></div></td></tr>`;
        try {
            const list = await API.academic.getSubjects();
            if (!list || list.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--text-muted);">No subjects found.</td></tr>`;
                return;
            }
            tbody.innerHTML = list.map(s => `
                <tr>
                    <td><strong>${escapeHtml(s.code)}</strong></td>
                    <td>${escapeHtml(s.name)}</td>
                    <td><span class="badge badge-primary">${escapeHtml(s.course_code)}</span></td>
                    <td>Semester ${s.semester}</td>
                    <td><span class="badge badge-neutral">${s.credits} Credits</span></td>
                    <td>
                        <button class="action-btn delete" title="Delete Subject" onclick="AcademicModule.deleteSubject('${escapeHtml(s.code)}')">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline stroke-width="2" points="3 6 5 6 21 6"/><path stroke-width="2" d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--danger);padding:2rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
        }
    },

    openAddSubjectModal() {
        const form = document.getElementById('add-subject-form');
        if (form) form.reset();
        
        // Populate course dropdown in subject form
        const select = document.getElementById('subject-course-input');
        if (select && StudentsModule.coursesList) {
            select.innerHTML = '<option value="">-- Select Course --</option>' +
                StudentsModule.coursesList.map(c => `<option value="${escapeHtml(c.code)}">${escapeHtml(c.name)}</option>`).join('');
        }

        showModal('modal-add-subject');
    },

    async handleAddSubject(e) {
        e.preventDefault();
        const code = document.getElementById('subject-code-input').value.trim();
        const name = document.getElementById('subject-name-input').value.trim();
        const course = document.getElementById('subject-course-input').value.trim();
        const semester = parseInt(document.getElementById('subject-semester-input').value) || 1;
        const credits = parseInt(document.getElementById('subject-credits-input').value) || 3;

        try {
            const res = await API.academic.addSubject({ code, name, course_code: course, semester, credits });
            if (res.success) {
                showToast('Success', 'Subject registered successfully.', 'success');
                closeModal('modal-add-subject');
                this.loadSubjects();
            } else {
                showToast('Failed', res.message, 'danger');
            }
        } catch (err) {
            showToast('Error', err.message, 'danger');
        }
    },

    deleteSubject(code) {
        showConfirmDialog('Delete Subject', `Permanently delete subject ${code}?`, async () => {
            try {
                const res = await API.academic.deleteSubject(code);
                if (res.success) {
                    showToast('Deleted', 'Subject removed.', 'success');
                    this.loadSubjects();
                } else {
                    showToast('Failed', res.message, 'danger');
                }
            } catch (err) {
                showToast('Error', err.message, 'danger');
            }
        });
    },

    // 3. Classes
    async loadClasses() {
        const tbody = document.getElementById('academic-classes-tbody');
        if (tbody) tbody.innerHTML = `<tr><td colspan="6"><div class="loading-state"><div class="spinner"></div><span>Loading classes...</span></div></td></tr>`;
        try {
            const list = await API.academic.getClasses();
            if (!list || list.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--text-muted);">No classes configured.</td></tr>`;
                return;
            }
            tbody.innerHTML = list.map(c => `
                <tr>
                    <td><strong>${escapeHtml(c.class_id)}</strong></td>
                    <td><span class="badge badge-primary">${escapeHtml(c.course_code)}</span></td>
                    <td>Semester ${c.semester}</td>
                    <td><span class="badge badge-neutral">Division ${escapeHtml(c.division)}</span></td>
                    <td>${escapeHtml(c.room_no || 'TBA')}</td>
                    <td>
                        <button class="action-btn delete" title="Delete Class" onclick="AcademicModule.deleteClass('${escapeHtml(c.class_id)}')">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline stroke-width="2" points="3 6 5 6 21 6"/><path stroke-width="2" d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--danger);padding:2rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
        }
    },

    openAddClassModal() {
        const form = document.getElementById('add-class-form');
        if (form) form.reset();

        const select = document.getElementById('class-course-input');
        if (select && StudentsModule.coursesList) {
            select.innerHTML = '<option value="">-- Select Course --</option>' +
                StudentsModule.coursesList.map(c => `<option value="${escapeHtml(c.code)}">${escapeHtml(c.name)}</option>`).join('');
        }

        showModal('modal-add-class');
    },

    async handleAddClass(e) {
        e.preventDefault();
        const classId = document.getElementById('class-id-input').value.trim();
        const course = document.getElementById('class-course-input').value.trim();
        const semester = parseInt(document.getElementById('class-semester-input').value) || 1;
        const division = document.getElementById('class-division-input').value.trim();
        const room = document.getElementById('class-room-input').value.trim();

        try {
            const res = await API.academic.addClass({ class_id: classId, course_code: course, semester, division, room_no: room });
            if (res.success) {
                showToast('Success', 'Class added successfully.', 'success');
                closeModal('modal-add-class');
                this.loadClasses();
            } else {
                showToast('Failed', res.message, 'danger');
            }
        } catch (err) {
            showToast('Error', err.message, 'danger');
        }
    },

    deleteClass(classId) {
        showConfirmDialog('Delete Class', `Permanently delete class ${classId}?`, async () => {
            try {
                const res = await API.academic.deleteClass(classId);
                if (res.success) {
                    showToast('Deleted', 'Class removed.', 'success');
                    this.loadClasses();
                } else {
                    showToast('Failed', res.message, 'danger');
                }
            } catch (err) {
                showToast('Error', err.message, 'danger');
            }
        });
    },

    // 4. Timetables
    async loadTimetables() {
        const tbody = document.getElementById('academic-timetables-tbody');
        if (tbody) tbody.innerHTML = `<tr><td colspan="5"><div class="loading-state"><div class="spinner"></div><span>Loading timetable entries...</span></div></td></tr>`;
        try {
            const list = await API.academic.getTimetables();
            if (!list || list.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--text-muted);">No timetable slots found.</td></tr>`;
                return;
            }
            tbody.innerHTML = list.map(t => `
                <tr>
                    <td><strong>${escapeHtml(t.class_id)}</strong></td>
                    <td><span class="badge badge-primary">${escapeHtml(t.day)}</span></td>
                    <td>${escapeHtml(t.time_slot)}</td>
                    <td><strong>${escapeHtml(t.subject_code)}</strong></td>
                    <td>
                        <button class="action-btn delete" title="Delete Slot" onclick="AcademicModule.deleteTimetable(${t.id})">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline stroke-width="2" points="3 6 5 6 21 6"/><path stroke-width="2" d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:var(--danger);padding:2rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
        }
    },

    openAddTimetableModal() {
        const form = document.getElementById('add-timetable-form');
        if (form) form.reset();
        showModal('modal-add-timetable');
    },

    async handleAddTimetable(e) {
        e.preventDefault();
        const classId = document.getElementById('tt-class-input').value.trim();
        const day = document.getElementById('tt-day-input').value.trim();
        const slot = document.getElementById('tt-slot-input').value.trim();
        const subject = document.getElementById('tt-subject-input').value.trim();

        try {
            const res = await API.academic.addTimetable({ class_id: classId, day, time_slot: slot, subject_code: subject });
            if (res.success) {
                showToast('Success', 'Timetable slot added.', 'success');
                closeModal('modal-add-timetable');
                this.loadTimetables();
            } else {
                showToast('Failed', res.message, 'danger');
            }
        } catch (err) {
            showToast('Error', err.message, 'danger');
        }
    },

    deleteTimetable(id) {
        showConfirmDialog('Delete Slot', 'Permanently delete this timetable entry?', async () => {
            try {
                const res = await API.academic.deleteTimetable(id);
                if (res.success) {
                    showToast('Deleted', 'Slot removed.', 'success');
                    this.loadTimetables();
                } else {
                    showToast('Failed', res.message, 'danger');
                }
            } catch (err) {
                showToast('Error', err.message, 'danger');
            }
        });
    }
};

window.AcademicModule = AcademicModule;
