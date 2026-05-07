const API = '';

const Auth = {
    getToken:   ()      => localStorage.getItem('token'),
    setToken:   (token) => localStorage.setItem('token', token),
    getUser:    ()      => JSON.parse(localStorage.getItem('user') || '{}'),
    setUser:    (user)  => localStorage.setItem('user', JSON.stringify(user)),
    isLoggedIn: ()      => !!localStorage.getItem('token'),
    getRole:    ()      => Auth.getUser().role || 'USER',
    isOrganizer:()      => Auth.getRole() === 'ORGANIZER',

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login.html';
    },

    requireAuth() {
        if (!this.isLoggedIn()) window.location.href = '/login.html';
    },

    requireOrganizer() {
        this.requireAuth();
        if (!this.isOrganizer()) window.location.href = '/';
    }
};

function escapeHTML(str) {
    if (!str) return '';
    return str.replace(/[&<>'"]/g, 
        tag => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            "'": '&#39;',
            '"': '&quot;'
        }[tag] || tag)
    );
}

async function api(endpoint, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = Auth.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(`${API}${endpoint}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null
    });

    if (res.status === 401 || res.status === 403) {
        Auth.logout();
        throw new Error('Session expired. Please log in again.');
    }

    if (res.status === 204) return null;

    const text = await res.text();
    const data = text ? JSON.parse(text) : null;
    
    if (!res.ok) throw new Error((data && (data.detail || data.message)) || 'Request failed');
    return data;
}

function showError(elementId, message) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.textContent = message;
    el.classList.remove('hidden');
}

function hideError(elementId) {
    const el = document.getElementById(elementId);
    if (el) el.classList.add('hidden');
}

function showSuccess(elementId, message) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.textContent = message;
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 3000);
}

function setLoading(buttonId, loading) {
    const btn = document.getElementById(buttonId);
    if (!btn) return;
    btn.disabled = loading;
    btn.textContent = loading ? 'Loading...' : btn.dataset.label;
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString();
}

function statusBadge(status) {
    const colors = {
        DRAFT: 'badge-draft',
        PUBLISHED: 'badge-published',
        CANCELLED: 'badge-cancelled',
        COMPLETED: 'badge-completed',
        CONFIRMED: 'badge-published',
        PENDING: 'badge-draft',
        SENT: 'badge-published',
        FAILED: 'badge-cancelled'
    };
    return `<span class="badge ${colors[status] || ''}">${escapeHTML(status)}</span>`;
}

function buildNav(activePage = '') {
    const user = Auth.getUser();
    const isLoggedIn = Auth.isLoggedIn();

    const navEl = document.getElementById('nav');
    if (!navEl) return;

    let links = `<a href="/" class="${activePage === 'home' ? 'active' : ''}">Events</a>`;

    if (isLoggedIn) {
        links += `<a href="/dashboard.html" class="${activePage === 'dashboard' ? 'active' : ''}">My Bookings</a>`;

        if (Auth.isOrganizer()) {
            links += `<a href="/organizer.html" class="${activePage === 'organizer' ? 'active' : ''}">Manage Events</a>`;
        }

        navEl.innerHTML = `
            <div class="nav-left">${links}</div>
            <div class="nav-right">
                <span class="nav-user">
                    ${escapeHTML(user.firstName || user.username)}
                    <span class="role-badge">${escapeHTML(user.role)}</span>
                </span>
                <button class="btn-danger btn-sm" onclick="Auth.logout()">Logout</button>
            </div>
        `;
    } else {
        navEl.innerHTML = `
            <div class="nav-left">${links}</div>
            <div class="nav-right">
                <a href="/login.html" class="btn-outline">Login</a>
                <a href="/register.html" class="btn-primary">Register</a>
            </div>
        `;
    }
}
