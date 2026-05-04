const API = 'http://localhost:8080/';


const Auth = {
    getToken: () => localStorage.getItem('token'),
    setToken: (token) => localStorage.setItem('token', token),
    removeToken: () => localStorage.removeItem('token'),
    isLoggedIn: () => !!localStorage.getItem('token'),
    getUser: () => JSON.parse(localStorage.getItem('user')  '{}'),
    setUser: (user) => localStorage.setItem('user', JSON.stringify(user)),
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login.html';
    }
};


async function apiCall(endpoint, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = Auth.getToken();
    if (token) headers['Authorization'] = Bearer ${token};

    const res = await fetch(${API}${endpoint}, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.detail  'Request failed');
    return data;
}