const BASE_URL = "http://localhost:8080/Sunrise_Dental_Clinic/resources";

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('login-form');
    const errorDiv = document.getElementById('error-msg');
    const loginBtn = document.getElementById('login-btn');
    const usernameField = document.getElementById('username');
    const passwordField = document.getElementById('password');
    const rememberBox = document.getElementById('remember-me');
    const toggleEye = document.getElementById('toggle-eye');
    const forgotLink = document.getElementById('forgot-link');

    function showError(message) {
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
        }
    }

    // ---- Remember Me: prefill saved username on page load ----
    const savedUsername = localStorage.getItem('sunrise_remember_username');
    if (savedUsername && usernameField) {
        usernameField.value = savedUsername;
        if (rememberBox) rememberBox.checked = true;
    }

    // ---- Show / hide password ----
    if (toggleEye && passwordField) {
        toggleEye.addEventListener('click', function () {
            const isHidden = passwordField.type === 'password';
            passwordField.type = isHidden ? 'text' : 'password';
            toggleEye.style.color = isHidden ? '#17608a' : '#33404a';
        });
    }

    // ---- Forgot password (no backend flow yet - simple hint only) ----
    if (forgotLink) {
        forgotLink.addEventListener('click', function (e) {
            e.preventDefault();
            alert('Please contact the clinic administrator to reset your password.');
        });
    }

    if (form) {
        form.addEventListener('submit', function (e) {
            e.preventDefault();

            errorDiv.style.display = 'none';
            loginBtn.disabled = true;
            loginBtn.textContent = 'Logging in...';

            const username = usernameField.value.trim();
            const password = passwordField.value;

            // Save or clear the remembered username
            if (rememberBox && rememberBox.checked) {
                localStorage.setItem('sunrise_remember_username', username);
            } else {
                localStorage.removeItem('sunrise_remember_username');
            }

            const body = new URLSearchParams();
            body.append('username', username);
            body.append('password', password);

            fetch(`${BASE_URL}/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: body.toString()
            })
            .then(response => response.json().then(data => ({ ok: response.ok, data })))
            .then(({ ok, data }) => {
                if (ok && data.status === 'success') {
                    if (data.role === 'DENTIST') {
                        window.location.href = `dentist_dash.html?id=${data.dentistId}&username=${encodeURIComponent(data.username)}`;
                    } else if (data.role) {
                        window.location.href = `staff_dash.html?user=${encodeURIComponent(data.username)}&role=${encodeURIComponent(data.role)}`;
                    } else {
                        showError('Unknown role returned from server.');
                    }
                } else {
                    showError(data.message || 'Invalid username or password.');
                }
            })
            .catch(error => {
                console.error('Login error:', error);
                showError('Could not reach the server. Please try again.');
            })
            .finally(() => {
                loginBtn.disabled = false;
                loginBtn.textContent = 'Login';
            });
        });
    }

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        showError(urlParams.get('error') || 'Invalid login details!');
    }
});

const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        showError(urlParams.get('error') || 'Invalid login details!');
    }
});