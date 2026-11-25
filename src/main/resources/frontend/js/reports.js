/**
 * reports.js - Reports & Global Search Module
 * System summary metrics, course enrollment analytics, semester and division distribution
 */

const ReportsModule = {
    async loadReports() {
        const container = document.getElementById('reports-content-area');
        if (container) {
            container.innerHTML = `<div class="loading-state"><div class="spinner"></div><span>Compiling real-time database reports...</span></div>`;
        }

        try {
            const [stats, coursesReport, semReport, divReport] = await Promise.all([
                API.reports.getDashboardStats(),
                API.reports.getCoursesReport(),
                API.reports.getSemestersReport(),
                API.reports.getDivisionsReport()
            ]);

            let courseRows = '';
            if (Array.isArray(coursesReport)) {
                courseRows = coursesReport.map(c => `
                    <tr>
                        <td><strong>${escapeHtml(c.name)}</strong></td>
                        <td><span class="badge badge-primary">${escapeHtml(c.code)}</span></td>
                        <td><strong>${c.enrolled_count}</strong> students</td>
                        <td>${stats.total_students > 0 ? Math.round((c.enrolled_count / stats.total_students) * 100) : 0}%</td>
                    </tr>
                `).join('');
            }

            let semCards = '';
            if (semReport && typeof semReport === 'object') {
                for (const [sem, count] of Object.entries(semReport)) {
                    semCards += `
                        <div style="flex:1;min-width:100px;background:var(--bg-app);border:1px solid var(--border-color);border-radius:var(--radius-md);padding:0.75rem;text-align:center;">
                            <div style="font-size:0.75rem;color:var(--text-muted);font-weight:600;">${escapeHtml(sem)}</div>
                            <div style="font-size:1.4rem;font-weight:700;margin-top:0.25rem;">${count}</div>
                        </div>
                    `;
                }
            }

            let divCards = '';
            if (divReport && typeof divReport === 'object') {
                for (const [div, count] of Object.entries(divReport)) {
                    divCards += `
                        <div style="flex:1;min-width:100px;background:var(--bg-app);border:1px solid var(--border-color);border-radius:var(--radius-md);padding:0.75rem;text-align:center;">
                            <div style="font-size:0.75rem;color:var(--text-muted);font-weight:600;">Division ${escapeHtml(div)}</div>
                            <div style="font-size:1.4rem;font-weight:700;margin-top:0.25rem;">${count}</div>
                        </div>
                    `;
                }
            }

            container.innerHTML = `
                <!-- System Executive Summary -->
                <div class="card">
                    <div class="card-header"><h3>Institutional Summary KPI Overview</h3></div>
                    <div class="card-body">
                        <div class="stats-grid">
                            <div class="stat-card">
                                <div class="stat-info"><span>Total Students</span><h3>${stats.total_students || 0}</h3></div>
                                <div class="stat-icon blue"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-width="2" d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4" stroke-width="2"/></svg></div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-info"><span>Courses Offered</span><h3>${stats.total_courses || 0}</h3></div>
                                <div class="stat-icon cyan"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-width="2" d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path stroke-width="2" d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg></div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-info"><span>Attendance Rate</span><h3>${stats.attendance_percentage || 0}%</h3></div>
                                <div class="stat-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-width="2" d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline stroke-width="2" points="22 4 12 14.01 9 11.01"/></svg></div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-info"><span>Average Exam Score</span><h3>${stats.average_exam_score || 0}%</h3></div>
                                <div class="stat-icon amber"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="8" r="7" stroke-width="2"/><polyline stroke-width="2" points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/></svg></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Course Breakdown Table -->
                <div class="card">
                    <div class="card-header"><h3>Course Enrollment Breakdown</h3></div>
                    <div class="card-body" style="padding:0;">
                        <div class="table-container" style="border:none;">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Course Name</th>
                                        <th>Course Code</th>
                                        <th>Enrolled Students</th>
                                        <th>Share of Student Body</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${courseRows || '<tr><td colspan="4" style="text-align:center;padding:1.5rem;">No courses.</td></tr>'}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Semester & Division Distribution -->
                <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(360px, 1fr));gap:1.5rem;">
                    <div class="card">
                        <div class="card-header"><h3>Student Distribution by Semester</h3></div>
                        <div class="card-body">
                            <div style="display:flex;flex-wrap:wrap;gap:0.75rem;">
                                ${semCards}
                            </div>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-header"><h3>Student Distribution by Division</h3></div>
                        <div class="card-body">
                            <div style="display:flex;flex-wrap:wrap;gap:0.75rem;">
                                ${divCards}
                            </div>
                        </div>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = `<div class="empty-state"><h4>Error Generating Reports</h4><p>${escapeHtml(err.message)}</p></div>`;
        }
    }
};

window.ReportsModule = ReportsModule;
