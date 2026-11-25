/**
 * attendance.js - Attendance Management Module
 * Mark Attendance by Class & Date, View Attendance Logs, and Overall Attendance KPIs
 */

const AttendanceModule = {
    attendanceList: [],
    markingStudents: [],

    init() {
        this.loadAttendance();
    },

    async loadAttendance() {
        await this.loadStats();
        await this.loadLogs();
        await this.initMarkingDropdowns();
    },

    async loadStats() {
        try {
            const stats = await API.attendance.getStats();
            document.getElementById('att-kpi-total').textContent = stats.total || 0;
            document.getElementById('att-kpi-present').textContent = stats.present || 0;
            document.getElementById('att-kpi-absent').textContent = stats.absent || 0;
            document.getElementById('att-kpi-pct').textContent = (stats.percentage || 0) + '%';
        } catch (err) {
            console.error('Error loading attendance stats:', err);
        }
    },

    async loadLogs() {
        const tbody = document.getElementById('attendance-logs-tbody');
        if (tbody) tbody.innerHTML = `<tr><td colspan="5"><div class="loading-state"><div class="spinner"></div><span>Loading attendance logs...</span></div></td></tr>`;
        try {
            const logs = await API.attendance.getAll();
            this.attendanceList = logs || [];
            if (this.attendanceList.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--text-muted);">No attendance records found.</td></tr>`;
                return;
            }
            tbody.innerHTML = this.attendanceList.map(a => `
                <tr>
                    <td><strong>${escapeHtml(a.student_id)}</strong></td>
                    <td>${escapeHtml(a.name)}</td>
                    <td>${escapeHtml(a.date)}</td>
                    <td><span class="badge ${a.status === 'PRESENT' ? 'badge-success' : 'badge-danger'}">${escapeHtml(a.status)}</span></td>
                    <td>
                        <button class="action-btn delete" title="Delete Log" onclick="AttendanceModule.deleteLog(${a.id})">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline stroke-width="2" points="3 6 5 6 21 6"/><path stroke-width="2" d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:var(--danger);padding:2rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
        }
    },

    async initMarkingDropdowns() {
        const courseSelect = document.getElementById('att-mark-course');
        if (courseSelect && StudentsModule.coursesList) {
            courseSelect.innerHTML = '<option value="">-- All Courses --</option>' +
                StudentsModule.coursesList.map(c => `<option value="${escapeHtml(c.code)}">${escapeHtml(c.name)}</option>`).join('');
        }

        // Set default date to today
        const dateInput = document.getElementById('att-mark-date');
        if (dateInput && !dateInput.value) {
            dateInput.value = new Date().toISOString().slice(0, 10);
        }
    },

    async filterStudentsForMarking() {
        const course = document.getElementById('att-mark-course')?.value || '';
        const semester = document.getElementById('att-mark-semester')?.value || '';
        const division = document.getElementById('att-mark-division')?.value || '';

        try {
            const all = await API.students.getAll();
            this.markingStudents = (all || []).filter(s => {
                const matchCourse = !course || s.course === course;
                const matchSem = !semester || String(s.semester) === String(semester);
                const matchDiv = !division || (s.division && s.division.toUpperCase() === division.toUpperCase());
                return matchCourse && matchSem && matchDiv;
            });

            this.renderMarkingTable();
        } catch (err) {
            showToast('Error', 'Failed to load students for attendance: ' + err.message, 'danger');
        }
    },

    renderMarkingTable() {
        const tbody = document.getElementById('att-marking-tbody');
        const countSpan = document.getElementById('att-marking-count');
        if (!tbody) return;

        if (countSpan) countSpan.textContent = `${this.markingStudents.length} Students found`;

        if (this.markingStudents.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--text-muted);">No students match selected class criteria. Adjust filters above.</td></tr>`;
            return;
        }

        tbody.innerHTML = this.markingStudents.map((s, idx) => `
            <tr>
                <td><strong>${escapeHtml(s.id)}</strong></td>
                <td>${escapeHtml(s.roll_no || '-')}</td>
                <td><strong>${escapeHtml(s.name)}</strong></td>
                <td>${escapeHtml(s.course)} - Sem ${s.semester} (${escapeHtml(s.division)})</td>
                <td>
                    <div style="display:flex;gap:0.5rem;align-items:center;">
                        <label style="display:flex;align-items:center;gap:0.35rem;cursor:pointer;font-size:0.85rem;">
                            <input type="radio" name="att_status_${idx}" value="PRESENT" checked>
                            <span class="badge badge-success">Present</span>
                        </label>
                        <label style="display:flex;align-items:center;gap:0.35rem;cursor:pointer;font-size:0.85rem;">
                            <input type="radio" name="att_status_${idx}" value="ABSENT">
                            <span class="badge badge-danger">Absent</span>
                        </label>
                    </div>
                </td>
            </tr>
        `).join('');
    },

    setAllMarking(status) {
        this.markingStudents.forEach((_, idx) => {
            const radio = document.querySelector(`input[name="att_status_${idx}"][value="${status}"]`);
            if (radio) radio.checked = true;
        });
    },

    async saveClassAttendance() {
        const date = document.getElementById('att-mark-date').value;
        if (!date) {
            showToast('Validation Error', 'Please select a valid attendance date.', 'warning');
            return;
        }

        if (this.markingStudents.length === 0) {
            showToast('Warning', 'No students to record attendance for.', 'warning');
            return;
        }

        const saveBtn = document.getElementById('att-save-btn');
        if (saveBtn) saveBtn.disabled = true;

        let savedCount = 0;
        let failCount = 0;

        for (let i = 0; i < this.markingStudents.length; i++) {
            const student = this.markingStudents[i];
            const checkedRadio = document.querySelector(`input[name="att_status_${i}"]:checked`);
            const status = checkedRadio ? checkedRadio.value : 'PRESENT';

            try {
                const res = await API.attendance.mark(student.id, date, status);
                if (res.success) savedCount++;
                else failCount++;
            } catch (err) {
                failCount++;
            }
        }

        if (saveBtn) saveBtn.disabled = false;

        showToast('Attendance Saved', `Recorded attendance for ${savedCount} students (${failCount} errors).`, savedCount > 0 ? 'success' : 'danger');
        await this.loadAttendance();
    },

    deleteLog(id) {
        showConfirmDialog('Delete Record', 'Permanently remove this attendance log?', async () => {
            try {
                const res = await API.attendance.delete(id);
                if (res.success) {
                    showToast('Deleted', 'Record removed.', 'success');
                    this.loadAttendance();
                } else {
                    showToast('Error', res.message, 'danger');
                }
            } catch (err) {
                showToast('Error', err.message, 'danger');
            }
        });
    }
};

window.AttendanceModule = AttendanceModule;
