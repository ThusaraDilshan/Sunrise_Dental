const BASE_URL = "http://localhost:8080/Sunrise_Dental_Clinic/resources";

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('login-form');
    const errorDiv = document.getElementById('error-msg');
    const loginBtn = document.getElementById('login-btn');

    function showError(message) {
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
        }
    }

    if (form) {
        form.addEventListener('submit', function (e) {
            e.preventDefault(); // stop normal form submission

            errorDiv.style.display = 'none';
            loginBtn.disabled = true;
            loginBtn.textContent = 'Logging in...';

            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

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

    // Check URL parameters for error message
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        showError(urlParams.get('error') || 'Invalid login details!');
    }
});