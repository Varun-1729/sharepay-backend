// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

// DOM Elements
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const switchLink = document.getElementById('switchLink');
const switchText = document.getElementById('switchText');
const loadingOverlay = document.getElementById('loadingOverlay');
const toast = document.getElementById('toast');

let isLoginMode = true;

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
    checkExistingSession();
});

function setupEventListeners() {
    loginForm.addEventListener('submit', handleLogin);
    registerForm.addEventListener('submit', handleRegister);
    switchLink.addEventListener('click', toggleAuthMode);
}

function toggleAuthMode(e) {
    e.preventDefault();
    isLoginMode = !isLoginMode;
    
    if (isLoginMode) {
        loginForm.style.display = 'flex';
        registerForm.style.display = 'none';
        switchText.textContent = "Don't have an account?";
        switchLink.textContent = 'Register here';
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'flex';
        switchText.textContent = 'Already have an account?';
        switchLink.textContent = 'Login here';
    }
}

async function handleLogin(e) {
    e.preventDefault();
    
    const formData = new FormData(loginForm);
    const loginData = {
        email: formData.get('email'),
        password: formData.get('password')
    };
    
    showLoading();
    
    try {
        console.log('Attempting login with:', loginData);
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData),
            credentials: 'include' // Important for session cookies
        });

        console.log('Login response status:', response.status);
        const result = await response.json();
        console.log('Login result:', result);

        if (result.success) {
            showToast('Login successful! Redirecting...', 'success');
            localStorage.setItem('currentUser', JSON.stringify(result.user));
            console.log('About to redirect to index.html');
            setTimeout(() => {
                console.log('Redirecting now...');
                window.location.href = 'index.html';
            }, 1000);
        } else {
            showToast(result.message, 'error');
        }
    } catch (error) {
        showToast('Login failed. Please try again.', 'error');
        console.error('Login error:', error);
    } finally {
        hideLoading();
    }
}

async function handleRegister(e) {
    e.preventDefault();
    
    const formData = new FormData(registerForm);
    const registerData = {
        name: formData.get('name'),
        email: formData.get('email'),
        password: formData.get('password'),
        phone: formData.get('phone') || ''
    };
    
    showLoading();
    
    try {
        console.log('Attempting registration with:', registerData);
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(registerData),
            credentials: 'include' // Important for session cookies
        });

        console.log('Registration response status:', response.status);
        const result = await response.json();
        console.log('Registration result:', result);

        if (result.success) {
            showToast('Registration successful! Redirecting...', 'success');
            localStorage.setItem('currentUser', JSON.stringify(result.user));
            console.log('About to redirect to index.html');
            setTimeout(() => {
                console.log('Redirecting now...');
                window.location.href = 'index.html';
            }, 1000);
        } else {
            showToast(result.message, 'error');
        }
    } catch (error) {
        showToast('Registration failed. Please try again.', 'error');
        console.error('Registration error:', error);
    } finally {
        hideLoading();
    }
}

async function checkExistingSession() {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/current`, {
            credentials: 'include'
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.success) {
                // User is already logged in
                localStorage.setItem('currentUser', JSON.stringify(result.user));
                window.location.href = 'index.html';
            }
        }
    } catch (error) {
        // Ignore errors - user is not logged in
        console.log('No existing session');
    }
}

function showLoading() {
    loadingOverlay.style.display = 'flex';
}

function hideLoading() {
    loadingOverlay.style.display = 'none';
}

function showToast(message, type = 'info') {
    toast.textContent = message;
    toast.className = `toast show ${type}`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}
