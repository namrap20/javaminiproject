/**
 * app.js - Application Lifecycle, Navigation, Modals, Toasts, and State Manager
 */

const AppState = {
    currentUser: null,
    currentPage: 'dashboard',
    theme: localStorage.getItem('sms_theme') || 'light'
};

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    checkAuthSession();
    bindEvents();
});

// ==========================================
// Theme Management
// ==========================================
function initTheme() {
    document.documentElement.setAttribute('data-theme', AppState.theme);
    updateThemeIcon();
}

function toggleTheme() {
    AppState.theme = AppState.theme === 'light' ? 'dark' : 'light';
    localStorage.setItem('sms_theme', AppState.theme);
    document.documentElement.setAttribute('data-theme', AppState.theme);
    updateThemeIcon();
    showToast('Theme Changed', `Switched to ${AppState.theme} mode.`, 'info');
}

function updateThemeIcon() {
    const btn = document.getElementById('theme-toggle-btn');
    if (!btn) return;
    if (AppState.theme === 'dark') {
        btn.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="5"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>`;
    } else {
        btn.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`;
    }
}

// ==========================================
// Authentication Session
// ==========================================
function checkAuthSession() {
    const savedUser = sessionStorage.getItem('sms_user');
    if (savedUser) {
        AppState.currentUser = JSON.parse(savedUser);
        showApp();
    } else {
        showLogin();
    }
}

function showLogin() {
    document.getElementById('login-screen').style.display = 'flex';
    document.getElementById('app-shell').style.display = 'none';
}

function showApp() {
    document.getElementById('login-screen').style.display = 'none';
    document.getElementById('app-shell').style.display = 'flex';
    const userDisplay = document.getElementById('current-username-display');
    if (userDisplay && AppState.currentUser) {
        userDisplay.textContent = AppState.currentUser.username || 'Administrator';
    }
    navigateTo('dashboard');
}

async function handleLogin(e) {
    if (e) e.preventDefault();
    const uInput = document.getElementById('login-username');
    const pInput = document.getElementById('login-password');
    const btn = document.getElementById('login-submit-btn');
    const errBox = document.getElementById('login-error-msg');

    const u = uInput.value.trim();
    const p = pInput.value.trim();

    if (!u || !p) {
        errBox.textContent = 'Please enter both username and password.';
        errBox.style.display = 'block';
        return;
    }

    btn.disabled = true;
    btn.innerHTML = `<div class="spinner" style="width:16px;height:16px;border-width:2px;display:inline-block;margin-right:8px;"></div> Authenticating...`;
    errBox.style.display = 'none';

    try {
        const res = await API.auth.login(u, p);
        if (res && res.success) {
            AppState.currentUser = { username: res.username || u, role: res.role || 'Admin' };
            sessionStorage.setItem('sms_user', JSON.stringify(AppState.currentUser));
            showToast('Welcome!', 'Logged in successfully.', 'success');
            showApp();
        } else {
            errBox.textContent = (res && res.message) ? res.message : 'Invalid credentials.';
            errBox.style.display = 'block';
        }
    } catch (err) {
        errBox.textContent = err.message || 'Unable to connect to server. Check database connection.';
        errBox.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Login to System';
    }
}

async function handleLogout() {
    showConfirmDialog(
        'Confirm Logout',
        'Are you sure you want to end your administrative session?',
        async () => {
            try {
                await API.auth.logout();
            } catch (ignored) {}
            sessionStorage.removeItem('sms_user');
            AppState.currentUser = null;
            showLogin();
            showToast('Logged Out', 'Your session has ended.', 'info');
        }
    );
}

// ==========================================
// Navigation & Page Routing
// ==========================================
function navigateTo(pageId, extraData = null) {
    AppState.currentPage = pageId;

    // Update active state in sidebar
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('data-page') === pageId) {
            link.classList.add('active');
        }
    });

    // Hide all pages, show target
    document.querySelectorAll('.page-view').forEach(view => {
        view.style.display = 'none';
    });

    const targetView = document.getElementById(`page-${pageId}`);
    if (targetView) {
        targetView.style.display = 'flex';
    }

    // Trigger page-specific loads
    switch (pageId) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'students':
            StudentsModule.loadStudents();
            break;
        case 'student-profile':
            if (extraData && extraData.studentId) {
                StudentsModule.loadProfile(extraData.studentId);
            }
            break;
        case 'academic':
            AcademicModule.loadCurrentTab();
            break;
        case 'attendance':
            AttendanceModule.loadAttendance();
            break;
        case 'exams':
            ExamsModule.loadExams();
            break;
        case 'reports':
            ReportsModule.loadReports();
            break;
        case 'settings':
            loadSettings();
            break;
    }
}

// ==========================================
// Dashboard Loader
// ==========================================
async function loadDashboard() {
    try {
        const stats = await API.reports.getDashboardStats();
        
        // Populate KPI Cards with real DB counts
        document.getElementById('stat-total-students').textContent = stats.total_students || 0;
        document.getElementById('stat-total-courses').textContent = stats.total_courses || 0;
        document.getElementById('stat-attendance-pct').textContent = (stats.attendance_percentage || 0) + '%';
        document.getElementById('stat-avg-score').textContent = (stats.average_exam_score || 0) + '%';

        // Additional counts
        const subCount = document.getElementById('stat-total-subjects');
        if (subCount) subCount.textContent = stats.total_subjects || 0;
        const clsCount = document.getElementById('stat-total-classes');
        if (clsCount) clsCount.textContent = stats.total_classes || 0;

        // Render Course breakdown cards
        const courseContainer = document.getElementById('dash-course-breakdown');
        if (courseContainer && stats.course_breakdown) {
            if (stats.course_breakdown.length === 0) {
                courseContainer.innerHTML = `<div class="empty-state"><p>No courses registered.</p></div>`;
            } else {
                let html = '<div style="display:flex;flex-direction:column;gap:0.75rem;">';
                for (const c of stats.course_breakdown) {
                    const pct = stats.total_students > 0 ? Math.round((c.enrolled_count / stats.total_students) * 100) : 0;
                    html += `
                        <div>
                            <div style="display:flex;justify-content:space-between;font-size:0.85rem;margin-bottom:0.25rem;">
                                <strong>${c.name} (${c.code})</strong>
                                <span class="badge badge-primary">${c.enrolled_count} Students (${pct}%)</span>
                            </div>
                            <div style="height:8px;background:var(--bg-app);border-radius:4px;overflow:hidden;">
                                <div style="height:100%;width:${pct}%;background:var(--primary);border-radius:4px;"></div>
                            </div>
                        </div>
                    `;
                }
                html += '</div>';
                courseContainer.innerHTML = html;
            }
        }

        // Grade distribution breakdown
        const gradeContainer = document.getElementById('dash-grade-distribution');
        if (gradeContainer && stats.grade_distribution) {
            let html = '<div style="display:flex;gap:0.75rem;flex-wrap:wrap;">';
            for (const [grade, count] of Object.entries(stats.grade_distribution)) {
                let bClass = 'badge-primary';
                if (grade === 'A+' || grade === 'A') bClass = 'badge-success';
                else if (grade === 'B' || grade === 'C') bClass = 'badge-primary';
                else if (grade === 'D') bClass = 'badge-warning';
                else if (grade === 'F') bClass = 'badge-danger';

                html += `
                    <div style="flex:1;min-width:70px;text-align:center;padding:0.75rem;background:var(--bg-app);border-radius:var(--radius-md);border:1px solid var(--border-color);">
                        <div style="font-size:0.75rem;color:var(--text-muted);font-weight:600;">Grade ${grade}</div>
                        <div style="font-size:1.25rem;font-weight:700;margin-top:0.25rem;"><span class="badge ${bClass}">${count}</span></div>
                    </div>
                `;
            }
            html += '</div>';
            gradeContainer.innerHTML = html;
        }

    } catch (err) {
        showToast('Dashboard Error', 'Unable to retrieve statistics from database: ' + err.message, 'danger');
    }
}

function loadSettings() {
    document.getElementById('sys-db-status').textContent = 'Connected (sms.db)';
    document.getElementById('sys-java-ver').textContent = 'Java 26 SE HotSpot';
    document.getElementById('sys-server-port').textContent = window.location.port || '8085';
}

// ==========================================
// Modal Dialog Controller
// ==========================================
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
    }
}

function showConfirmDialog(title, message, onConfirm) {
    document.getElementById('confirm-title').textContent = title;
    document.getElementById('confirm-message').textContent = message;
    
    const confirmBtn = document.getElementById('confirm-action-btn');
    // Remove previous listeners
    const newBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);

    newBtn.addEventListener('click', () => {
        closeModal('modal-confirm');
        if (typeof onConfirm === 'function') onConfirm();
    });

    showModal('modal-confirm');
}

// ==========================================
// Toast Notification Controller
// ==========================================
function showToast(title, message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let iconSvg = '';
    if (type === 'success') {
        iconSvg = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="20" height="20" color="var(--success)"><path stroke-width="2.5" d="M20 6L9 17l-5-5"/></svg>`;
    } else if (type === 'danger') {
        iconSvg = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="20" height="20" color="var(--danger)"><circle cx="12" cy="12" r="10" stroke-width="2"/><line x1="15" y1="9" x2="9" y2="15" stroke-width="2"/><line x1="9" y1="9" x2="15" y2="15" stroke-width="2"/></svg>`;
    } else if (type === 'warning') {
        iconSvg = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="20" height="20" color="var(--warning)"><path stroke-width="2" d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13" stroke-width="2"/><line x1="12" y1="17" x2="12.01" y2="17" stroke-width="2"/></svg>`;
    } else {
        iconSvg = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="20" height="20" color="var(--info)"><circle cx="12" cy="12" r="10" stroke-width="2"/><line x1="12" y1="16" x2="12" y2="12" stroke-width="2"/><line x1="12" y1="8" x2="12.01" y2="8" stroke-width="2"/></svg>`;
    }

    toast.innerHTML = `
        <div class="toast-icon">${iconSvg}</div>
        <div class="toast-content">
            <div class="toast-title">${escapeHtml(title)}</div>
            <div class="toast-message">${escapeHtml(message)}</div>
        </div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 250);
    }, 4000);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// ==========================================
// Event Binding
// ==========================================
function bindEvents() {
    // Navigation links
    document.querySelectorAll('[data-nav]').forEach(el => {
        el.addEventListener('click', (e) => {
            e.preventDefault();
            const target = el.getAttribute('data-nav');
            if (target) navigateTo(target);
        });
    });

    // Login Form Submit
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Password view toggle on login
    const togglePassBtn = document.getElementById('toggle-password-visibility');
    if (togglePassBtn) {
        togglePassBtn.addEventListener('click', () => {
            const passInput = document.getElementById('login-password');
            if (passInput.type === 'password') {
                passInput.type = 'text';
            } else {
                passInput.type = 'password';
            }
        });
    }

    // Theme toggle button
    const themeBtn = document.getElementById('theme-toggle-btn');
    if (themeBtn) {
        themeBtn.addEventListener('click', toggleTheme);
    }

    // Logout buttons
    document.querySelectorAll('[data-action="logout"]').forEach(btn => {
        btn.addEventListener('click', handleLogout);
    });

    // Global Topbar Search
    const searchInput = document.getElementById('global-search-input');
    if (searchInput) {
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                const val = searchInput.value.trim();
                if (val) {
                    navigateTo('students');
                    StudentsModule.searchStudents(val);
                }
            }
        });
    }

    // Settings Password Change
    const passForm = document.getElementById('change-password-form');
    if (passForm) {
        passForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const newP = document.getElementById('new-password-input').value;
            const confP = document.getElementById('confirm-new-password-input').value;
            if (newP !== confP) {
                showToast('Error', 'New passwords do not match.', 'danger');
                return;
            }
            try {
                const res = await API.auth.changePassword('admin', newP);
                if (res.success) {
                    showToast('Success', 'Password changed successfully.', 'success');
                    passForm.reset();
                } else {
                    showToast('Error', res.message, 'danger');
                }
            } catch (err) {
                showToast('Error', err.message, 'danger');
            }
        });
    }
}

window.AppState = AppState;
window.navigateTo = navigateTo;
window.showToast = showToast;
window.showModal = showModal;
window.closeModal = closeModal;
window.showConfirmDialog = showConfirmDialog;
window.escapeHtml = escapeHtml;
