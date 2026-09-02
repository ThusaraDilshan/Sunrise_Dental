const BASE_URL = "http://localhost:8080/Sunrise_Dental_Clinic/resources";

var loggedDentistId = null;
var loggedDentistName = "Dentist";
var loggedDentistUsername = "";
var loggedDentistFee = 0;
var lastFetchedAppointments = [];
var lastReportDayList = [];
var lastReportDate = '';
var lastReportStats = null;
var currentStatusFilter = 'ALL';

// --- Helper function to format 24hr time (HH:mm:ss) to 12hr time with AM/PM ---
function formatTimeTo12Hour(timeString) {
    if (!timeString) return '-';
    var parts = timeString.toString().split(':');
    if (parts.length < 2) return timeString;

    var hours = parseInt(parts[0], 10);
    var minutes = parts[1];
    var ampm = hours >= 12 ? 'PM' : 'AM';

    hours = hours % 12;
    hours = hours ? hours : 12;
    var formattedHours = hours < 10 ? '0' + hours : hours;

    return formattedHours + ':' + minutes + ' ' + ampm;
}

function sortAppointmentsByDateTime(list) {
    return (list || []).slice().sort((a, b) => {
        var aDate = a.appointmentDate || a.appointment_date || '';
        var bDate = b.appointmentDate || b.appointment_date || '';
        if (aDate !== bDate) return aDate < bDate ? -1 : 1;
        var aTime = a.appointmentTime || a.appointment_time || '';
        var bTime = b.appointmentTime || b.appointment_time || '';
        if (aTime === bTime) return 0;
        return aTime < bTime ? -1 : 1;
    });
}

function assignLocalAppointmentNumbers(list) {
    var sorted = sortAppointmentsByDateTime(list);
    sorted.forEach((a, idx) => {
        a.localAptNo = 'APT' + String(idx + 1).padStart(4, '0');
    });
    return list;
}

// Separate, day-scoped numbering used ONLY for the "Today's Schedule" table,
// so that view shows appt numbers relative to that day (1, 2, 3...) instead
// of the global appt number from the full appointments list.
function assignTodayLocalNumbers(list) {
    var sorted = sortAppointmentsByDateTime(list);
    sorted.forEach((a, idx) => {
        a.todayLocalAptNo = 'APT' + String(idx + 1).padStart(4, '0');
    });
    return list;
}

document.addEventListener("DOMContentLoaded", function () {
    var params = new URLSearchParams(window.location.search);
    var idParam = params.get('id');
    var nameParam = params.get('name');
    var usernameParam = params.get('username');

    var savedId = sessionStorage.getItem('loggedDentistId');
    if (savedId) {
        // Existing session already in sessionStorage (e.g. after a profile update)
        // Always prefer this over the URL params, which may be stale on refresh.
        loggedDentistId = parseInt(savedId, 10);
        loggedDentistName = sessionStorage.getItem('loggedDentistName') || "Dentist";
        loggedDentistUsername = sessionStorage.getItem('loggedDentistUsername') || "";
    } else if (idParam) {
        // First-time load coming from login redirect: seed sessionStorage from URL params.
        loggedDentistId = parseInt(idParam, 10);
        loggedDentistName = nameParam || "Dentist";
        loggedDentistUsername = usernameParam || "";
        sessionStorage.setItem('loggedDentistId', loggedDentistId);
        sessionStorage.setItem('loggedDentistName', loggedDentistName);
        sessionStorage.setItem('loggedDentistUsername', loggedDentistUsername);
    }

    var headerName = document.getElementById('headerName');
    var headerAvatar = document.getElementById('headerAvatar');
    if (headerName) headerName.textContent = loggedDentistName;
    if (headerAvatar) headerAvatar.textContent = loggedDentistName.charAt(0).toUpperCase();

    var pfId = document.getElementById('pf-id');
    var pfUsername = document.getElementById('pf-username');
    if (pfId) pfId.textContent = loggedDentistId || '-';
    if (pfUsername) pfUsername.value = loggedDentistUsername;

    // Always confirm the real name from the server, in case the URL param
    // (e.g. right after login) was missing/stale.
    syncHeaderNameFromServer();

    // Sidebar navigation Event Listeners (Safe binding)
    setupNavigation();

    loadDentistAppointments();
    refreshNoticeUnreadBadge();

    // Logout Button listener check
    var logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            sessionStorage.clear();
            window.location.href = 'index.html';
        });
    }
});

function syncHeaderNameFromServer() {
    if (!loggedDentistId) return;

    fetch(BASE_URL + '/dentists')
        .then(res => res.json())
        .then(list => {
            var me = (list || []).find(d => (d.dentistId || d.dentist_id) === loggedDentistId);
            if (!me) return;

            var realName = me.dentistName || me.dentist_name;
            if (!realName) return;

            loggedDentistName = realName;
            sessionStorage.setItem('loggedDentistName', realName);

            var hName = document.getElementById('headerName');
            var hAvatar = document.getElementById('headerAvatar');
            if (hName) hName.textContent = realName;
            if (hAvatar) hAvatar.textContent = realName.charAt(0).toUpperCase();
        })
        .catch(err => console.error('Failed to sync dentist name from server:', err));
}

function setupNavigation() {
    var navs = document.querySelectorAll('.nav-item');
    navs.forEach(function(n) {
        n.addEventListener('click', function(e) {
            e.preventDefault();
            var target = this.getAttribute('data-target');
            if (target) {
                showSection(target);
            }
        });
    });
}

function showSection(targetId) {
    var sections = document.querySelectorAll('.section');
    sections.forEach(function(s) { s.classList.remove('active'); });
    
    var targetSec = document.getElementById(targetId);
    if (targetSec) {
        targetSec.classList.add('active');
    }

    var navs = document.querySelectorAll('.nav-item');
    navs.forEach(function(n) { n.classList.remove('active'); });
    
    var navBtn = document.querySelector('.nav-item[data-target="' + targetId + '"]');
    if (navBtn) {
        navBtn.classList.add('active');
    }

    if (targetId === 'sec-overview' || targetId === 'sec-appointments' || targetId === 'sec-today') {
        loadDentistAppointments();
    }
    if (targetId === 'sec-profile') {
        loadDentistProfileIntoForm();
    }
    if (targetId === 'sec-notices') {
        loadDentistNotices();
    }
    if (targetId === 'sec-reports') {
        var dateInput = document.getElementById('reportDateInput');
        if (dateInput && !dateInput.value) dateInput.value = getTodayStr();
        loadDentistAppointments(generateDentistReport);
    }
}

function getTodayStr() {
    var d = new Date();
    var y = d.getFullYear();
    var m = String(d.getMonth() + 1).padStart(2, '0');
    var day = String(d.getDate()).padStart(2, '0');
    return y + '-' + m + '-' + day;
}

var notifDismissed = false;

function dismissNotifBanner() {
    notifDismissed = true;
    var banner = document.getElementById('todayNotifBanner');
    if (banner) banner.style.display = 'none';
}

function updateTodayNotification(data) {
    var banner = document.getElementById('todayNotifBanner');
    if (!banner || notifDismissed) return;

    var todayStr = getTodayStr();
    var todays = (data || []).filter(a => {
        var aptDate = a.appointmentDate || a.appointment_date || '';
        var status = (a.status || '').toUpperCase();
        return aptDate === todayStr && status !== 'CANCELLED';
    });

    if (todays.length === 0) {
        banner.className = 'all-clear';
        banner.innerHTML = '<div class="notif-text"><span class="notif-icon">✅</span> No appointments scheduled for today.</div>'
            + '<button class="notif-dismiss" onclick="dismissNotifBanner()">✕</button>';
    } else {
        var pending = todays.filter(a => (a.status || '').toUpperCase() === 'PENDING').length;
        var completed = todays.filter(a => (a.status || '').toUpperCase() === 'COMPLETED').length;
        banner.className = '';
        banner.innerHTML = '<div class="notif-text"><span class="notif-icon">🔔</span> You have <b>' + todays.length + '</b> appointment'
            + (todays.length > 1 ? 's' : '') + ' today (' + pending + ' pending, ' + completed + ' completed).</div>'
            + '<button class="notif-dismiss" onclick="dismissNotifBanner()">✕</button>';
    }
    banner.style.display = 'flex';
}

function loadDentistAppointments(onDone) {
    var tbody = document.getElementById('dentistAppointmentsTableBody');

    if (!loggedDentistId) {
        if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#d33; padding:24px;">Could not identify the logged-in dentist. Please log out and log in again.</td></tr>';
        return;
    }

    if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#9aa0ab; padding:24px;">Loading schedule...</td></tr>';

    fetch(BASE_URL + '/appointment/by-dentist/' + loggedDentistId)
        .then(res => res.json())
        .then(data => {
            lastFetchedAppointments = data || [];
            assignLocalAppointmentNumbers(lastFetchedAppointments);

            lastFetchedAppointments = sortAppointmentsByDateTime(lastFetchedAppointments);
            updateTodayNotification(lastFetchedAppointments);
            renderTodayAppointments(lastFetchedAppointments);
            renderOverviewCards(lastFetchedAppointments);

            var allInput = document.getElementById('allSearchInput');
            if (allInput) allInput.value = '';
            var todayInput = document.getElementById('todaySearchInput');
            if (todayInput) todayInput.value = '';
            currentStatusFilter = 'ALL';
            updateStatusFilterUI();

            if (tbody) {
                if (!lastFetchedAppointments || lastFetchedAppointments.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#9aa0ab; padding:24px;">No appointments found.</td></tr>';
                } else {
                    tbody.innerHTML = buildAppointmentRowsHTML(lastFetchedAppointments, true);
                }
            }
            if (typeof onDone === 'function') onDone();
        })
        .catch(() => {
            if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#d33; padding:24px;">Failed to load appointments.</td></tr>';
            if (typeof onDone === 'function') onDone();
        });
}

function filterAppointmentsTable(scope) {
    var term = '';
    var tbody, sourceList, colspan, includeApptNo, numberField;

    if (scope === 'today') {
        var todayEl = document.getElementById('todaySearchInput');
        term = (todayEl ? todayEl.value : '').trim().toLowerCase();
        tbody = document.getElementById('todayAppointmentsTableBody');
        var todayStr = getTodayStr();
        sourceList = (lastFetchedAppointments || []).filter(a => (a.appointmentDate || a.appointment_date || '') === todayStr);
        assignTodayLocalNumbers(sourceList);
        numberField = 'todayLocalAptNo';
        colspan = 8;
        includeApptNo = true;
    } else {
        var allEl = document.getElementById('allSearchInput');
        term = (allEl ? allEl.value : '').trim().toLowerCase();
        tbody = document.getElementById('dentistAppointmentsTableBody');
        sourceList = lastFetchedAppointments || [];
        if (currentStatusFilter && currentStatusFilter !== 'ALL') {
            sourceList = sourceList.filter(a => (a.status || 'PENDING').toUpperCase() === currentStatusFilter);
        }
        numberField = 'localAptNo';
        colspan = 8;
        includeApptNo = true;
    }

    if (!tbody) return;

    var filtered = !term ? sourceList : sourceList.filter(a => {
        var displayAptNo = (a[numberField] || '').toLowerCase();
        var name = (a.patientName || '').toLowerCase();
        var contact = (a.contactNo || '').toLowerCase();
        return displayAptNo.includes(term) || name.includes(term) || contact.includes(term);
    });

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="' + colspan + '" style="text-align:center; color:#9aa0ab; padding:24px;">No matching appointments found.</td></tr>';
        return;
    }
    tbody.innerHTML = buildAppointmentRowsHTML(filtered, includeApptNo, numberField);
}

function toggleFilterDropdown() {
    var dropdown = document.getElementById('statusFilterDropdown');
    if (!dropdown) return;
    dropdown.classList.toggle('show');
}

function setStatusFilter(status) {
    currentStatusFilter = status;
    updateStatusFilterUI();
    var dropdown = document.getElementById('statusFilterDropdown');
    if (dropdown) dropdown.classList.remove('show');
    filterAppointmentsTable('all');
}

function updateStatusFilterUI() {
    var label = document.getElementById('statusFilterLabel');
    var labels = { ALL: 'Filter', PENDING: 'Pending', COMPLETED: 'Completed', CANCELLED: 'Cancelled' };
    if (label) label.textContent = labels[currentStatusFilter] || 'Filter';

    var options = document.querySelectorAll('#statusFilterDropdown .filter-option');
    options.forEach(function (opt) {
        opt.classList.toggle('active', opt.getAttribute('data-status') === currentStatusFilter);
    });
}

document.addEventListener('click', function (e) {
    var wrap = document.querySelector('.filter-wrap');
    var dropdown = document.getElementById('statusFilterDropdown');
    if (!wrap || !dropdown) return;
    if (!wrap.contains(e.target)) {
        dropdown.classList.remove('show');
    }
});

function buildAppointmentRowsHTML(list, includeApptNo, numberField) {
    if (includeApptNo === undefined) includeApptNo = true;
    var field = numberField || 'localAptNo';
    var rowsHTML = '';
    (list || []).forEach(a => {
        var realAptNo = a.appointmentNo || '-';
        var displayAptNo = a[field] || a.localAptNo || realAptNo;
        var status = a.status || 'PENDING';
        var actionButtons = (status === 'PENDING') ?
            `<button class="btn-action btn-complete" onclick="updateAppointmentStatus('${realAptNo}', 'COMPLETED')">Complete</button>
             <button class="btn-action btn-cancel" onclick="updateAppointmentStatus('${realAptNo}', 'CANCELLED')">Cancel</button>` :
            `<button class="btn-action btn-delete" onclick="deleteAppointment('${realAptNo}')">Delete</button>`;

        rowsHTML += `<tr>
            ${includeApptNo ? `<td><b>${displayAptNo}</b></td>` : ''}
            <td class="align-left">${escapeHtml(a.patientName || '-')}</td>
            <td>${escapeHtml(a.contactNo || '-')}</td>
            <td class="align-left">${escapeHtml(a.treatmentName || '-')}</td>
            <td>${a.appointmentDate || '-'}</td>
            <td>${a.appointmentTime || '-'}</td>
            <td><span class="badge ${status}">${status}</span></td>
            <td>${actionButtons}</td>
        </tr>`;
    });
    return rowsHTML;
}

function getMonthStr() {
    var d = new Date();
    var y = d.getFullYear();
    var m = String(d.getMonth() + 1).padStart(2, '0');
    return y + '-' + m;
}

function renderOverviewCards(list) {
    var total = document.getElementById('ov-total');
    var todayEl = document.getElementById('ov-today');
    var pending = document.getElementById('ov-pending');
    var completed = document.getElementById('ov-completed');
    var cancelled = document.getElementById('ov-cancelled');
    var earningsDailyEl = document.getElementById('ov-earnings-daily');
    var earningsMonthlyEl = document.getElementById('ov-earnings-monthly');
    if (!total || !pending || !completed || !cancelled) return;

    var data = list || [];
    var todayStr = getTodayStr();
    var monthStr = getMonthStr();

    var todayCount = data.filter(a => {
        var aptDate = a.appointmentDate || a.appointment_date || '';
        var status = (a.status || '').toUpperCase();
        return aptDate === todayStr && status !== 'CANCELLED';
    }).length;
    var pendingCount = data.filter(a => (a.status || 'PENDING').toUpperCase() === 'PENDING').length;
    var completedCount = data.filter(a => (a.status || '').toUpperCase() === 'COMPLETED').length;
    var cancelledCount = data.filter(a => (a.status || '').toUpperCase() === 'CANCELLED').length;

    var completedTodayCount = data.filter(a => {
        var aptDate = a.appointmentDate || a.appointment_date || '';
        return aptDate === todayStr && (a.status || '').toUpperCase() === 'COMPLETED';
    }).length;
    var completedThisMonthCount = data.filter(a => {
        var aptDate = a.appointmentDate || a.appointment_date || '';
        return aptDate.slice(0, 7) === monthStr && (a.status || '').toUpperCase() === 'COMPLETED';
    }).length;

    total.textContent = data.length;
    if (todayEl) todayEl.textContent = todayCount;
    pending.textContent = pendingCount;
    completed.textContent = completedCount;
    cancelled.textContent = cancelledCount;

    if (earningsDailyEl || earningsMonthlyEl) {
        loadLoggedDentistFee(function () {
            var fee = loggedDentistFee || 0;
            if (earningsDailyEl) earningsDailyEl.textContent = 'LKR ' + (completedTodayCount * fee).toFixed(2);
            if (earningsMonthlyEl) earningsMonthlyEl.textContent = 'LKR ' + (completedThisMonthCount * fee).toFixed(2);
        });
    }
}

function renderTodayAppointments(data) {
    var tbody = document.getElementById('todayAppointmentsTableBody');
    if (!tbody) return;

    var todayStr = getTodayStr();
    var todays = (data || []).filter(a => {
        var aptDate = a.appointmentDate || a.appointment_date || '';
        return aptDate === todayStr;
    });

    if (todays.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#9aa0ab; padding:24px;">No appointments for today.</td></tr>';
        return;
    }
    assignTodayLocalNumbers(todays);
    tbody.innerHTML = buildAppointmentRowsHTML(todays, true, 'todayLocalAptNo');
}

function loadDentistNotices() {
    var container = document.getElementById('noticesListContainer');
    if (!container) return;

    if (!loggedDentistId) {
        container.innerHTML = '<div style="text-align:center; color:#d33; padding:24px;">Could not identify the logged-in dentist. Please log out and log in again.</div>';
        return;
    }

    container.innerHTML = '<div style="text-align:center; color:#9aa0ab; padding:24px;">Loading notices...</div>';

    fetch(BASE_URL + '/notice/dentist/' + loggedDentistId)
        .then(res => res.json())
        .then(data => {
            renderNoticesList(data || []);
            updateNoticeBadgeCount(data || []);
        })
        .catch(() => {
            container.innerHTML = '<div style="text-align:center; color:#d33; padding:24px;">Failed to load notices.</div>';
        });
}

function renderNoticesList(notices) {
    var container = document.getElementById('noticesListContainer');
    if (!container) return;

    if (!notices || notices.length === 0) {
        container.innerHTML = '<div style="text-align:center; color:#9aa0ab; padding:24px;">No notices yet.</div>';
        return;
    }

    var html = '';
    notices.forEach(n => {
        var noticeId = n.noticeId !== undefined ? n.noticeId : n.notice_id;
        var isRead = n.read !== undefined ? n.read : (n.isRead !== undefined ? n.isRead : n.is_read);
        var sentBy = n.sentBy || n.sent_by || 'Staff';
        var createdAt = n.createdAt || n.created_at || '';

        html += '<div class="notice-item' + (isRead ? '' : ' unread') + '">'
            + (isRead ? '' : '<span class="notice-dot"></span>')
            + '<div class="notice-body">'
            + '<div class="notice-desc">' + escapeHtml(n.description || '') + '</div>'
            + '<div class="notice-meta">From <b>' + escapeHtml(sentBy) + '</b> &middot; ' + escapeHtml(createdAt) + '</div>'
            + '</div>'
            + (isRead ? '' : '<button class="btn-mark-read" onclick="markNoticeRead(' + noticeId + ')">Mark as read</button>')
            + '</div>';
    });
    container.innerHTML = html;
}

function markNoticeRead(noticeId) {
    fetch(BASE_URL + '/notice/' + noticeId + '/read', { method: 'PUT' })
        .then(res => res.ok ? res.json() : Promise.reject('Failed to update notice.'))
        .then(() => loadDentistNotices())
        .catch(err => console.error('Failed to mark notice as read:', err));
}

function updateNoticeBadgeCount(notices) {
    var badge = document.getElementById('noticeUnreadBadge');
    if (!badge) return;
    var unreadCount = (notices || []).filter(n => {
        var isRead = n.read !== undefined ? n.read : (n.isRead !== undefined ? n.isRead : n.is_read);
        return !isRead;
    }).length;

    if (unreadCount > 0) {
        badge.textContent = unreadCount;
        badge.style.display = 'inline-block';
    } else {
        badge.style.display = 'none';
    }
}

function refreshNoticeUnreadBadge() {
    if (!loggedDentistId) return;
    fetch(BASE_URL + '/notice/dentist/' + loggedDentistId)
        .then(res => res.json())
        .then(data => updateNoticeBadgeCount(data || []))
        .catch(() => {});
}

function loadLoggedDentistFee(callback) {
    if (loggedDentistFee > 0) { if (typeof callback === 'function') callback(); return; }
    fetch(BASE_URL + '/dentists')
        .then(res => res.json())
        .then(list => {
            var me = (list || []).find(d => (d.dentistId || d.dentist_id) === loggedDentistId);
            var rawFee = me ? (me.consultationFee !== undefined ? me.consultationFee : me.consultation_fee) : 0;
            loggedDentistFee = parseFloat(rawFee) || 0;
        })
        .catch(() => { loggedDentistFee = 0; })
        .finally(() => { if (typeof callback === 'function') callback(); });
}

function generateDentistReport() {
    loadLoggedDentistFee(function () {
        var dateInput = document.getElementById('reportDateInput');
        if (dateInput && !dateInput.value) dateInput.value = getTodayStr();
        var selectedDate = dateInput ? dateInput.value : getTodayStr();
        renderReportForDate(selectedDate);
    });
}

function renderReportForDate(dateStr) {
    var grid = document.getElementById('reportSummaryGrid');
    var tbody = document.getElementById('reportTableBody');
    if (!grid || !tbody) return;

    var dayList = (lastFetchedAppointments || []).filter(a => {
        var aptDate = a.appointmentDate || a.appointment_date || '';
        return aptDate === dateStr;
    });

    var total = dayList.length;
    var completed = dayList.filter(a => (a.status || '').toUpperCase() === 'COMPLETED').length;
    var pending = dayList.filter(a => (a.status || '').toUpperCase() === 'PENDING').length;
    var cancelled = dayList.filter(a => (a.status || '').toUpperCase() === 'CANCELLED').length;
    var earnings = completed * (loggedDentistFee || 0);

    lastReportDayList = dayList;
    lastReportDate = dateStr;
    lastReportStats = { total: total, completed: completed, pending: pending, cancelled: cancelled, earnings: earnings };

    grid.innerHTML =
        '<div class="stat-card"><div class="stat-value">' + total + '</div><div class="stat-label">Total</div></div>' +
        '<div class="stat-card"><div class="stat-value">' + completed + '</div><div class="stat-label">Completed</div></div>' +
        '<div class="stat-card"><div class="stat-value">' + pending + '</div><div class="stat-label">Pending</div></div>' +
        '<div class="stat-card"><div class="stat-value">' + cancelled + '</div><div class="stat-label">Cancelled</div></div>' +
        '<div class="stat-card earnings"><div class="stat-value">LKR ' + earnings.toFixed(2) + '</div><div class="stat-label">Est. Earnings</div></div>';

    if (dayList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#9aa0ab; padding:24px;">No appointments found for ' + dateStr + '.</td></tr>';
        return;
    }

    var rowsHTML = '';
    dayList.forEach(a => {
        var status = a.status || 'PENDING';
        rowsHTML += '<tr>'
            + '<td><b>' + (a.appointmentNo || '-') + '</b></td>'
            + '<td class="align-left">' + escapeHtml(a.patientName || '-') + '</td>'
            + '<td class="align-left">' + escapeHtml(a.treatmentName || '-') + '</td>'
            + '<td>' + (a.appointmentTime || '-') + '</td>'
            + '<td><span class="badge ' + status + '">' + status + '</span></td>'
            + '</tr>';
    });
    tbody.innerHTML = rowsHTML;
}

function downloadDentistReportPdf() {
    var msg = document.getElementById('updateStatusMsg');
    if (!lastReportDate || !lastReportStats) {
        if (msg) {
            msg.className = 'error'; msg.style.display = 'flex';
            msg.innerHTML = '⚠️ Please click Generate first to build a report before downloading.';
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        }
        return;
    }
    if (!window.jspdf) {
        if (msg) {
            msg.className = 'error'; msg.style.display = 'flex';
            msg.innerHTML = '⚠️ PDF library failed to load. Check your internet connection and try again.';
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        }
        return;
    }

    var jsPDF = window.jspdf.jsPDF;
    var doc = new jsPDF({ orientation: 'portrait', unit: 'pt', format: 'a4' });
    var pageWidth = doc.internal.pageSize.getWidth();
    var margin = 40;
    var contentWidth = pageWidth - margin * 2;

    var periodLabel = formatReportDateLabel(lastReportDate);

    // ---------- Header band ----------
    var headerHeight = 96;
    doc.setFillColor(37, 99, 227);
    doc.rect(0, 0, pageWidth, headerHeight, 'F');
    // subtle darker accent strip along the very top for a bit of depth
    doc.setFillColor(29, 84, 199);
    doc.rect(0, 0, pageWidth, 4, 'F');

    // Brand badge (rounded square monogram)
    var badgeSize = 38;
    var badgeX = margin;
    var badgeY = (headerHeight - badgeSize) / 2 - 4;
    doc.setFillColor(255, 255, 255);
    doc.roundedRect(badgeX, badgeY, badgeSize, badgeSize, 10, 10, 'F');
    doc.setTextColor(37, 99, 227);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(18);
    doc.text('S', badgeX + badgeSize / 2, badgeY + badgeSize / 2 + 6.5, { align: 'center' });

    // Clinic name + tagline
    var textX = badgeX + badgeSize + 14;
    doc.setTextColor(255, 255, 255);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(19);
    doc.text('Sunrise Dental Clinic', textX, badgeY + 17);
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10.5);
    doc.setTextColor(219, 234, 254);
    doc.text('Professional Dental Care', textX, badgeY + 33);

    // Report type pill + period, right aligned
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(9);
    var pillLabel = 'DAILY REPORT';
    var pillPaddingX = 10;
    var pillWidth = doc.getTextWidth(pillLabel) + pillPaddingX * 2;
    var pillHeight = 18;
    var pillX = pageWidth - margin - pillWidth;
    var pillY = badgeY + 1;
    doc.setFillColor(255, 255, 255);
    doc.roundedRect(pillX, pillY, pillWidth, pillHeight, 9, 9, 'F');
    doc.setTextColor(37, 99, 227);
    doc.text(pillLabel, pillX + pillWidth / 2, pillY + 12.5, { align: 'center' });

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(11.5);
    doc.setTextColor(255, 255, 255);
    doc.text(String(periodLabel), pageWidth - margin, pillY + pillHeight + 16, { align: 'right' });

    // ---------- Meta info card ----------
    var metaTop = headerHeight + 18;
    var metaHeight = 44;
    doc.setFillColor(247, 250, 255);
    doc.setDrawColor(230, 236, 250);
    doc.roundedRect(margin, metaTop, contentWidth, metaHeight, 8, 8, 'FD');

    var metaMidX = margin + contentWidth / 2;
    doc.setDrawColor(224, 230, 245);
    doc.line(metaMidX, metaTop + 10, metaMidX, metaTop + metaHeight - 10);

    var metaLabelY = metaTop + 18;
    var metaValueY = metaTop + 32;
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(8.5);
    doc.setTextColor(148, 158, 179);
    doc.text('DENTIST', margin + 18, metaLabelY);
    doc.text('GENERATED ON', metaMidX + 18, metaLabelY);

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(11);
    doc.setTextColor(31, 36, 48);
    var dentistLabel = loggedDentistName ? (loggedDentistName.startsWith('Dr.') ? loggedDentistName : 'Dr. ' + loggedDentistName) : '-';
    doc.text(String(dentistLabel), margin + 18, metaValueY);
    doc.text(new Date().toLocaleString('en-GB', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }), metaMidX + 18, metaValueY);

    // ---------- Stat cards ----------
    var stats = lastReportStats;
    var summaryData = [
        { label: 'Total', value: String(stats.total), accent: [37, 99, 227] },
        { label: 'Completed', value: String(stats.completed), accent: [30, 158, 76] },
        { label: 'Pending', value: String(stats.pending), accent: [217, 143, 15] },
        { label: 'Cancelled', value: String(stats.cancelled), accent: [221, 51, 51] },
        { label: 'Est. Earnings', value: 'LKR ' + stats.earnings.toFixed(2), accent: [30, 158, 76] }
    ];
    var boxTop = metaTop + metaHeight + 20;
    var boxGap = 10;
    var boxWidth = (contentWidth - boxGap * 4) / 5;
    var boxHeight = 58;
    summaryData.forEach(function (item, i) {
        var x = margin + i * (boxWidth + boxGap);
        var isEarnings = item.label === 'Est. Earnings';
        doc.setFillColor(255, 255, 255);
        doc.setDrawColor(232, 236, 245);
        doc.roundedRect(x, boxTop, boxWidth, boxHeight, 7, 7, 'FD');
        doc.setFillColor(item.accent[0], item.accent[1], item.accent[2]);
        doc.roundedRect(x, boxTop, boxWidth, 4, 2, 2, 'F');
        // cover the bottom two corners of the accent strip so it doesn't poke out rounded
        doc.rect(x, boxTop + 2, boxWidth, 2, 'F');

        doc.setTextColor(item.accent[0], item.accent[1], item.accent[2]);
        doc.setFont('helvetica', 'bold');
        doc.setFontSize(isEarnings ? 12.5 : 16);
        doc.text(item.value, x + boxWidth / 2, boxTop + 32, { align: 'center' });
        doc.setTextColor(140, 148, 165);
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(7.8);
        doc.text(item.label.toUpperCase(), x + boxWidth / 2, boxTop + 46, { align: 'center' });
    });

    // ---------- Appointment table ----------
    var tableRows = (lastReportDayList || []).map(function (a) {
        return [
            a.appointmentNo || '-',
            a.patientName || '-',
            a.treatmentName || '-',
            formatTimeTo12Hour(a.appointmentTime || ''),
            (a.status || 'PENDING').toUpperCase()
        ];
    });

    doc.autoTable({
        head: [['Appt No', 'Patient Name', 'Treatment', 'Time', 'Status']],
        body: tableRows.length ? tableRows : [['-', 'No appointments found for ' + lastReportDate, '-', '-', '-']],
        startY: boxTop + boxHeight + 26,
        margin: { left: margin, right: margin },
        styles: { font: 'helvetica', fontSize: 9, cellPadding: { top: 9, bottom: 9, left: 8, right: 8 }, textColor: [31, 36, 48], lineColor: [238, 240, 244], lineWidth: 0.5, valign: 'middle' },
        headStyles: { fillColor: [37, 99, 227], textColor: [255, 255, 255], fontStyle: 'bold', fontSize: 9, halign: 'center', cellPadding: { top: 10, bottom: 10, left: 8, right: 8 } },
        alternateRowStyles: { fillColor: [249, 251, 255] },
        columnStyles: {
            0: { halign: 'center', cellWidth: 70, fontStyle: 'bold' },
            1: { halign: 'left', cellWidth: 150 },
            2: { halign: 'left' },
            3: { halign: 'center', cellWidth: 70 },
            4: { halign: 'center', cellWidth: 85 }
        },
        didParseCell: function (data) {
            if (data.section === 'body' && data.column.index === 4) {
                var status = String(data.cell.raw).toUpperCase();
                if (status === 'COMPLETED') { data.cell.styles.textColor = [30, 158, 76]; data.cell.styles.fontStyle = 'bold'; }
                else if (status === 'CANCELLED') { data.cell.styles.textColor = [221, 51, 51]; data.cell.styles.fontStyle = 'bold'; }
                else { data.cell.styles.textColor = [217, 143, 15]; data.cell.styles.fontStyle = 'bold'; }
            }
        },
        didDrawPage: function () {
            var pageCount = doc.internal.getNumberOfPages();
            var footerY = doc.internal.pageSize.getHeight() - 26;
            doc.setDrawColor(238, 240, 244);
            doc.line(margin, footerY - 10, pageWidth - margin, footerY - 10);
            doc.setFont('helvetica', 'normal');
            doc.setFontSize(8);
            doc.setTextColor(154, 160, 171);
            doc.text('Sunrise Dental Clinic — Confidential · Generated electronically', margin, footerY);
            doc.text(
                'Page ' + doc.internal.getCurrentPageInfo().pageNumber + ' of ' + pageCount,
                pageWidth - margin,
                footerY,
                { align: 'right' }
            );
        }
    });

    doc.save('DentistReport_' + lastReportDate + '.pdf');
}

function formatReportDateLabel(dateStr) {
    if (!dateStr) return '—';
    var d = new Date(dateStr + 'T00:00:00');
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'long', year: 'numeric' });
}

function escapeHtml(str) {
    var div = document.createElement('div');
    div.textContent = str == null ? '' : String(str);
    return div.innerHTML;
}

function togglePasswordVisibility() {
    var input = document.getElementById('pf-password');
    var btn = document.getElementById('pf-password-toggle');
    if (!input || !btn) return;
    if (input.type === 'password') {
        input.type = 'text';
        btn.textContent = 'Hide';
    } else {
        input.type = 'password';
        btn.textContent = 'Show';
    }
}

function loadDentistProfileIntoForm() {
    if (!loggedDentistId) return;

    fetch(BASE_URL + '/dentists')
        .then(res => res.json())
        .then(list => {
            var me = (list || []).find(d => (d.dentistId || d.dentist_id) === loggedDentistId);
            if (!me) return;

            var nameEl = document.getElementById('pf-name');
            var phoneEl = document.getElementById('pf-phone');
            var specEl = document.getElementById('pf-specialization');
            var feeEl = document.getElementById('pf-fee');

            if (nameEl) nameEl.value = me.dentistName || me.dentist_name || '';
            if (phoneEl) phoneEl.value = me.contactNo || me.contact_no || '';
            if (specEl) specEl.value = me.specialization || '';
            
            var rawFee = me.consultationFee !== undefined ? me.consultationFee : me.consultation_fee;
            if (feeEl) feeEl.value = rawFee !== undefined && rawFee !== null ? rawFee : '';
        })
        .catch(err => console.error('Failed to load profile data:', err));
}

function updateAppointmentStatus(appointmentNo, newStatus) {
    if (!confirm('Are you sure you want to mark appointment ' + appointmentNo + ' as ' + newStatus + '?')) return;
    var msg = document.getElementById('updateStatusMsg');
    fetch(BASE_URL + '/appointment/' + encodeURIComponent(appointmentNo) + '/status', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
    })
    .then(res => res.ok ? res.json() : res.text().then(t => { throw new Error(t); }))
    .then(() => {
        if (msg) {
            msg.className = 'success'; msg.style.display = 'flex';
            msg.innerHTML = '✅ Appointment <b>' + appointmentNo + '</b> updated to ' + newStatus;
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        }
        loadDentistAppointments();
    })
    .catch(err => {
        if (msg) {
            msg.className = 'error'; msg.style.display = 'flex';
            msg.innerHTML = '⚠️ Status update failed. ' + err.message;
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        }
    });
}

function deleteAppointment(appointmentNo) {
    var apt = (lastFetchedAppointments || []).find(a => a.appointmentNo === appointmentNo);
    var status = apt ? (apt.status || 'PENDING').toUpperCase() : '';
    if (status === 'PENDING') return; // safety guard: only COMPLETED/CANCELLED appointments can be deleted

    if (!confirm('Are you sure you want to delete appointment ' + appointmentNo + '? This cannot be undone.')) return;

    var msg = document.getElementById('updateStatusMsg');
    fetch(BASE_URL + '/appointment/' + encodeURIComponent(appointmentNo) + '?actingUsername=' + encodeURIComponent(loggedDentistUsername), { method: 'DELETE' })
        .then(res => res.ok ? true : res.text().then(t => { throw new Error(t); }))
        .then(() => {
            if (msg) {
                msg.className = 'success'; msg.style.display = 'flex';
                msg.innerHTML = '✅ Appointment <b>' + appointmentNo + '</b> deleted.';
                setTimeout(() => { msg.style.display = 'none'; }, 4000);
            }
            loadDentistAppointments();
        })
        .catch(err => {
            if (msg) {
                msg.className = 'error'; msg.style.display = 'flex';
                msg.innerHTML = '⚠️ Delete failed. ' + err.message;
                setTimeout(() => { msg.style.display = 'none'; }, 4000);
            }
        });
}

function searchAppointment() {
    var searchEl = document.getElementById('searchAptNo');
    var aptNo = searchEl ? searchEl.value.trim() : '';
    var msg = document.getElementById('updateStatusMsg');
    var box = document.getElementById('searchDetailBox');
    if (box) box.style.display = 'none'; 
    if (msg) msg.style.display = 'none';
    if (!aptNo) return;

    var matched = (lastFetchedAppointments || []).find(a =>
        (a.localAptNo || '').toLowerCase() === aptNo.toLowerCase() ||
        (a.appointmentNo || '').toLowerCase() === aptNo.toLowerCase()
    );
    var lookupNo = matched ? matched.appointmentNo : aptNo;

    fetch(BASE_URL + '/appointment/' + encodeURIComponent(lookupNo))
        .then(res => res.status === 200 ? res.json() : Promise.reject('No appointment found with number ' + aptNo))
        .then(a => {
            var displayNo = (matched && matched.localAptNo) || a.appointmentNo || '-';
            document.getElementById('d-apptNo').textContent = displayNo;
            document.getElementById('d-patientName').textContent = a.patientName || '-';
            document.getElementById('d-contact').textContent = a.contactNo || '-';
            document.getElementById('d-address').textContent = a.address || '-';
            document.getElementById('d-dentist').textContent = a.dentistName || '-';
            document.getElementById('d-treatment').textContent = a.treatmentName || '-';
            document.getElementById('d-date').textContent = a.appointmentDate || '-';
            document.getElementById('d-time').textContent = a.appointmentTime || '-';
            
            var statusBadge = document.getElementById('d-statusBadge');
            if (statusBadge) {
                statusBadge.textContent = a.status || 'PENDING';
                statusBadge.className = 'badge ' + (a.status || 'PENDING');
            }
            if (box) box.style.display = 'block';
        })
        .catch(err => { 
            if (msg) {
                msg.className = 'error'; msg.style.display = 'flex'; msg.innerHTML = '⚠️ ' + err;
                setTimeout(() => { msg.style.display = 'none'; }, 4000);
            }
        });
}

function updateDentistProfile() {
    var msg = document.getElementById('updateStatusMsg');
    var nameEl = document.getElementById('pf-name');
    var name = nameEl ? nameEl.value.trim() : '';
    var formData = new URLSearchParams();
    
    formData.append('dentistName', name);
    formData.append('contactNo', document.getElementById('pf-phone').value.trim());
    formData.append('specialization', document.getElementById('pf-specialization').value.trim());
    formData.append('consultationFee', document.getElementById('pf-fee').value);
    formData.append('password', document.getElementById('pf-password').value);

    fetch(BASE_URL + '/dentists/' + loggedDentistId + '/profile', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData.toString()
    })
    .then(res => res.ok ? res.json() : Promise.reject('Failed to update profile.'))
    .then(() => {
        if (msg) {
            msg.className = 'success'; msg.style.display = 'flex'; msg.innerHTML = '✅ Profile updated successfully!';
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        }
        if (name) {
            loggedDentistName = name;
            sessionStorage.setItem('loggedDentistName', name);
            var hName = document.getElementById('headerName');
            var hAvatar = document.getElementById('headerAvatar');
            if (hName) hName.textContent = name;
            if (hAvatar) hAvatar.textContent = name.charAt(0).toUpperCase();
        }
        var passEl = document.getElementById('pf-password');
        if (passEl) passEl.value = '';
    })
    .catch(err => { 
        if (msg) {
            msg.className = 'error'; msg.style.display = 'flex'; msg.innerHTML = '⚠️ ' + err;
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        }
    });
}