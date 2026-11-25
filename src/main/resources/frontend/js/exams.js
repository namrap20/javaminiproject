/**
 * exams.js - Examination & Results Management Module
 * Result Entry, Real-time Grade Preview, Marksheet Viewer, Editing & Deleting
 */

const ExamsModule = {
    results: [],

    init() {
        this.loadExams();
    },

    async loadExams() {
        await this.loadStats();
        await this.loadResultsTable();
        await this.initFormDropdowns();
    },

    async loadStats() {
        try {
            const stats = await API.exams.getStats();
            document.getElementById('exam-kpi-total').textContent = stats.total_exams || 0;
            document.getElementById('exam-kpi-avg').textContent = (stats.average_score || 0) + '%';
        } catch (err) {
            console.error('Error loading exam stats:', err);
        }
    },

    async loadResultsTable() {
        const tbody = document.getElementById('exams-table-body');
        if (tbody) tbody.innerHTML = `<tr><td colspan="7"><div class="loading-state"><div class="spinner"></div><span>Loading exam results from database...</span></div></td></tr>`;
        try {
            const list = await API.exams.getAll();
            this.results = list || [];
            if (this.results.length === 0) {
                tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:2rem;color:var(--text-muted);">No exam records found.</td></tr>`;
                return;
            }

            tbody.innerHTML = this.results.map(r => {
                const pct = r.total > 0 ? Math.round((r.marks / r.total) * 1000) / 10 : 0;
                let bClass = 'badge-primary';
                if (r.grade === 'A+' || r.grade === 'A') bClass = 'badge-success';
                else if (r.grade === 'D' || r.grade === 'E') bClass = 'badge-warning';
                else if (r.grade === 'F') bClass = 'badge-danger';

                return `
                    <tr>
                        <td><strong>${escapeHtml(r.student_id)}</strong></td>
                        <td>${escapeHtml(r.name)}</td>
                        <td><span class="badge badge-primary">${escapeHtml(r.subject)}</span></td>
                        <td><strong>${r.marks}</strong></td>
                        <td>${r.total}</td>
                        <td>${pct}%</td>
                        <td><span class="badge ${bClass}">${escapeHtml(r.grade)}</span></td>
                        <td>
                            <div class="action-buttons">
                                <button class="action-btn" title="View Marksheet" onclick="ExamsModule.viewMarksheet('${escapeHtml(r.student_id)}')">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-width="2" d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline stroke-width="2" points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13" stroke-width="2"/><line x1="16" y1="17" x2="8" y2="17" stroke-width="2"/></svg>
                                </button>
                                <button class="action-btn" title="Edit Result" onclick="ExamsModule.openEditModal(${r.id}, '${escapeHtml(r.student_id)}', '${escapeHtml(r.subject)}', ${r.marks}, ${r.total})">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-width="2" d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path stroke-width="2" d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                                </button>
                                <button class="action-btn delete" title="Delete Result" onclick="ExamsModule.deleteResult(${r.id})">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline stroke-width="2" points="3 6 5 6 21 6"/><path stroke-width="2" d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--danger);padding:2rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
        }
    },

    async initFormDropdowns() {
        try {
            const [students, subjects] = await Promise.all([
                API.students.getAll(),
                API.academic.getSubjects()
            ]);

            const stSelect = document.getElementById('exam-form-student');
            if (stSelect && students) {
                stSelect.innerHTML = '<option value="">-- Select Student --</option>' +
                    students.map(s => `<option value="${escapeHtml(s.id)}">${escapeHtml(s.name)} (${escapeHtml(s.id)})</option>`).join('');
            }

            const subSelect = document.getElementById('exam-form-subject');
            if (subSelect && subjects) {
                subSelect.innerHTML = '<option value="">-- Select Subject --</option>' +
                    subjects.map(s => `<option value="${escapeHtml(s.code)}">${escapeHtml(s.name)} (${escapeHtml(s.code)})</option>`).join('');
            }
        } catch (err) {
            console.error('Error loading exam form dropdowns:', err);
        }
    },

    calculateGrade(marks, total) {
        if (total <= 0) return 'F';
        const pct = (marks / total) * 100;
        if (pct >= 90) return 'A+';
        if (pct >= 80) return 'A';
        if (pct >= 70) return 'B';
        if (pct >= 60) return 'C';
        if (pct >= 50) return 'D';
        return 'F';
    },

    updateGradePreview() {
        const m = parseFloat(document.getElementById('exam-form-marks')?.value) || 0;
        const t = parseFloat(document.getElementById('exam-form-total')?.value) || 100;
        const gradeBadge = document.getElementById('exam-grade-preview');
        if (gradeBadge) {
            const g = this.calculateGrade(m, t);
            gradeBadge.textContent = `Grade: ${g}`;
        }
    },

    openAddModal() {
        this.initFormDropdowns();
        const form = document.getElementById('exam-crud-form');
        if (form) form.reset();
        document.getElementById('exam-modal-title').textContent = 'Enter Student Exam Marks';
        document.getElementById('exam-form-mode').value = 'add';
        document.getElementById('exam-form-id').value = '';
        document.getElementById('exam-form-student').disabled = false;
        document.getElementById('exam-form-subject').disabled = false;
        document.getElementById('exam-form-total').value = '100';
        this.updateGradePreview();
        showModal('modal-exam-form');
    },

    openEditModal(id, studentId, subject, marks, total) {
        this.initFormDropdowns();
        document.getElementById('exam-modal-title').textContent = `Edit Result for ${studentId} - ${subject}`;
        document.getElementById('exam-form-mode').value = 'edit';
        document.getElementById('exam-form-id').value = id;

        const stSelect = document.getElementById('exam-form-student');
        if (stSelect) { stSelect.value = studentId; stSelect.disabled = true; }
        const subSelect = document.getElementById('exam-form-subject');
        if (subSelect) { subSelect.value = subject; subSelect.disabled = true; }

        document.getElementById('exam-form-marks').value = marks;
        document.getElementById('exam-form-total').value = total;
        this.updateGradePreview();

        showModal('modal-exam-form');
    },

    async handleFormSubmit(e) {
        e.preventDefault();
        const mode = document.getElementById('exam-form-mode').value;
        const id = document.getElementById('exam-form-id').value;
        const studentId = document.getElementById('exam-form-student').value;
        const subject = document.getElementById('exam-form-subject').value;
        const marks = parseFloat(document.getElementById('exam-form-marks').value);
        const total = parseFloat(document.getElementById('exam-form-total').value);

        if (isNaN(marks) || isNaN(total) || total <= 0) {
            showToast('Validation Error', 'Please enter valid numbers for marks and total.', 'warning');
            return;
        }

        try {
            if (mode === 'add') {
                const res = await API.exams.addResult({ student_id: studentId, subject_code: subject, marks, total });
                if (res.success) {
                    showToast('Success', `Exam score saved! Grade: ${res.grade}`, 'success');
                    closeModal('modal-exam-form');
                    this.loadExams();
                } else {
                    showToast('Failed', res.message, 'danger');
                }
            } else {
                const res = await API.exams.updateResult(id, { marks, total });
                if (res.success) {
                    showToast('Success', 'Exam result updated.', 'success');
                    closeModal('modal-exam-form');
                    this.loadExams();
                } else {
                    showToast('Failed', res.message, 'danger');
                }
            }
        } catch (err) {
            showToast('Error', err.message, 'danger');
        }
    },

    deleteResult(id) {
        showConfirmDialog('Delete Result', 'Permanently delete this exam result?', async () => {
            try {
                const res = await API.exams.deleteResult(id);
                if (res.success) {
                    showToast('Deleted', 'Result removed from database.', 'success');
                    this.loadExams();
                } else {
                    showToast('Error', res.message, 'danger');
                }
            } catch (err) {
                showToast('Error', err.message, 'danger');
            }
        });
    },

    async viewMarksheet(studentId) {
        StudentsModule.viewProfile(studentId);
    }
};

window.ExamsModule = ExamsModule;
