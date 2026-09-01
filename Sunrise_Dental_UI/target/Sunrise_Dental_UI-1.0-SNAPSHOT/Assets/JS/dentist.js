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
    var sorted = (list || []).slice().sort((a, b) => {
        var aNum = parseInt((a.appointmentNo || '').toString().replace(/[^0-9]/g, ''), 10) || 0;
        var bNum = parseInt((b.appointmentNo || '').toString().replace(/[^0-9]/g, ''), 10) || 0;
        return aNum - bNum;
    });
    sorted.forEach((a, idx) => {
        a.localAptNo = 'APT' + String(idx + 1).padStart(4, '0');
    });
    return list;
}

document.addEventListener("DOMContentLoaded", function () {
    var params = new URLSearchParams(window.location.search);
    var idParam = params.get('id');
    var nameParam = params.get('name');
    var usernameParam = params.get('username');

    if (idParam) {
        loggedDentistId = parseInt(idParam, 10);
        loggedDentistName = nameParam || "Dentist";
        loggedDentistUsername = usernameParam || "";
        sessionStorage.setItem('loggedDentistId', loggedDentistId);
        sessionStorage.setItem('loggedDentistName', loggedDentistName);
        sessionStorage.setItem('loggedDentistUsername', loggedDentistUsername);
    } else {
        var savedId = sessionStorage.getItem('loggedDentistId');
        if (savedId) {
            loggedDentistId = parseInt(savedId, 10);
            loggedDentistName = sessionStorage.getItem('loggedDentistName') || "Dentist";
            loggedDentistUsername = sessionStorage.getItem('loggedDentistUsername') || "";
        }
    }

    var headerName = document.getElementById('headerName');
    var headerAvatar = document.getElementById('headerAvatar');
    if (headerName) headerName.textContent = loggedDentistName;
    if (headerAvatar) headerAvatar.textContent = loggedDentistName.charAt(0).toUpperCase();

    var pfId = document.getElementById('pf-id');
    var pfUsername = document.getElementById('pf-username');
    if (pfId) pfId.textContent = loggedDentistId || '-';
    if (pfUsername) pfUsername.value = loggedDentistUsername;

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
        if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#d33; padding:24px;">Could not identify the logged-in dentist. Please log out and log in again.</td></tr>';
        return;
    }

    if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#9aa0ab; padding:24px;">Loading schedule...</td></tr>';

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
                    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#9aa0ab; padding:24px;">No appointments found.</td></tr>';
                } else {
                    tbody.innerHTML = buildAppointmentRowsHTML(lastFetchedAppointments, false);
                }
            }
            if (typeof onDone === 'function') onDone();
        })
        .catch(() => {
            if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#d33; padding:24px;">Failed to load appointments.</td></tr>';
            if (typeof onDone === 'function') onDone();
        });
}

function filterAppointmentsTable(scope) {
    var term = '';
    var tbody, sourceList, colspan, includeApptNo;

    if (scope === 'today') {
        var todayEl = document.getElementById('todaySearchInput');
        term = (todayEl ? todayEl.value : '').trim().toLowerCase();
        tbody = document.getElementById('todayAppointmentsTableBody');
        var todayStr = getTodayStr();
        sourceList = (lastFetchedAppointments || []).filter(a => (a.appointmentDate || a.appointment_date || '') === todayStr);
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
        colspan = 7;
        includeApptNo = false;
    }

    if (!tbody) return;

    var filtered = !term ? sourceList : sourceList.filter(a => {
        var aptNo = (a.appointmentNo || '').toLowerCase();
        var localAptNo = (a.localAptNo || '').toLowerCase();
        var name = (a.patientName || '').toLowerCase();
        return aptNo.includes(term) || localAptNo.includes(term) || name.includes(term);
    });

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="' + colspan + '" style="text-align:center; color:#9aa0ab; padding:24px;">No matching appointments found.</td></tr>';
        return;
    }
    tbody.innerHTML = buildAppointmentRowsHTML(filtered, includeApptNo);
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

function buildAppointmentRowsHTML(list, includeApptNo) {
    if (includeApptNo === undefined) includeApptNo = true;
    var rowsHTML = '';
    (list || []).forEach(a => {
        var realAptNo = a.appointmentNo || '-';
        var displayAptNo = a.localAptNo || realAptNo;
        var status = a.status || 'PENDING';
        var actionButtons = (status === 'PENDING') ?
            `<button class="btn-action btn-complete" onclick="updateAppointmentStatus('${realAptNo}', 'COMPLETED')">Complete</button>
             <button class="btn-action btn-cancel" onclick="updateAppointmentStatus('${realAptNo}', 'CANCELLED')">Cancel</button>` : '-';

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

function renderOverviewCards(list) {
    var total = document.getElementById('ov-total');
    var pending = document.getElementById('ov-pending');
    var completed = document.getElementById('ov-completed');
    if (!total || !pending || !completed) return;

    var data = list || [];
    var pendingCount = data.filter(a => (a.status || 'PENDING').toUpperCase() === 'PENDING').length;
    var completedCount = data.filter(a => (a.status || '').toUpperCase() === 'COMPLETED').length;

    total.textContent = data.length;
    pending.textContent = pendingCount;
    completed.textContent = completedCount;
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
    tbody.innerHTML = buildAppointmentRowsHTML(todays);
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
            msg.className = 'error'; msg.style.display = 'block';
            msg.textContent = 'Please click Generate first to build a report before downloading.';
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        }
        return;
    }
    if (!window.jspdf) {
        if (msg) {
            msg.className = 'error'; msg.style.display = 'block';
            msg.textContent = 'PDF library failed to load. Check your internet connection and try again.';
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        }
        return;
    }

    var jsPDF = window.jspdf.jsPDF;
    var doc = new jsPDF({ orientation: 'portrait', unit: 'pt', format: 'a4' });
    var pageWidth = doc.internal.pageSize.getWidth();
    var margin = 40;

    doc.setFillColor(47, 111, 237);
    doc.rect(0, 0, pageWidth, 70, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(18);
    doc.text('Sunrise Dental Clinic', margin, 30);
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(11);
    doc.text('Daily Appointment & Earnings Report', margin, 48);

    doc.setTextColor(31, 36, 48);
    doc.setFontSize(10);
    var metaY = 90;
    doc.setFont('helvetica', 'bold');
    doc.text('Dentist:', margin, metaY);
    doc.setFont('helvetica', 'normal');
    doc.text(String(loggedDentistName || '-'), margin + 55, metaY);

    doc.setFont('helvetica', 'bold');
    doc.text('Report Date:', margin + 260, metaY);
    doc.setFont('helvetica', 'normal');
    doc.text(String(lastReportDate), margin + 335, metaY);

    var genY = metaY + 16;
    doc.setFont('helvetica', 'bold');
    doc.text('Generated:', margin, genY);
    doc.setFont('helvetica', 'normal');
    doc.text(new Date().toLocaleString(), margin + 55, genY);

    var stats = lastReportStats;
    var summaryData = [
        ['Total', String(stats.total)],
        ['Completed', String(stats.completed)],
        ['Pending', String(stats.pending)],
        ['Cancelled', String(stats.cancelled)],
        ['Est. Earnings', 'LKR ' + stats.earnings.toFixed(2)]
    ];
    var boxTop = genY + 20;
    var boxGap = 10;
    var boxWidth = (pageWidth - margin * 2 - boxGap * 4) / 5;
    var boxHeight = 46;
    summaryData.forEach(function (item, i) {
        var x = margin + i * (boxWidth + boxGap);
        doc.setFillColor(247, 250, 255);
        doc.setDrawColor(234, 241, 253);
        doc.roundedRect(x, boxTop, boxWidth, boxHeight, 4, 4, 'FD');
        doc.setTextColor(item[0] === 'Est. Earnings' ? 30 : 31, item[0] === 'Est. Earnings' ? 158 : 36, item[0] === 'Est. Earnings' ? 76 : 48);
        doc.setFont('helvetica', 'bold');
        doc.setFontSize(item[0] === 'Est. Earnings' ? 11 : 14);
        doc.text(item[1], x + boxWidth / 2, boxTop + 22, { align: 'center' });
        doc.setTextColor(154, 160, 171);
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(8);
        doc.text(item[0].toUpperCase(), x + boxWidth / 2, boxTop + 36, { align: 'center' });
    });

    var tableRows = (lastReportDayList || []).map(function (a) {
        return [
            a.appointmentNo || '-',
            a.patientName || '-',
            a.treatmentName || '-',
            a.appointmentTime || '-',
            a.status || 'PENDING'
        ];
    });

    doc.autoTable({
        head: [['Appt No', 'Patient Name', 'Treatment', 'Time', 'Status']],
        body: tableRows.length ? tableRows : [['-', 'No appointments found for ' + lastReportDate, '-', '-', '-']],
        startY: boxTop + boxHeight + 24,
        margin: { left: margin, right: margin },
        styles: { font: 'helvetica', fontSize: 9.5, cellPadding: 6, textColor: [31, 36, 48], lineColor: [238, 240, 244], lineWidth: 0.5 },
        headStyles: { fillColor: [47, 111, 237], textColor: [255, 255, 255], fontStyle: 'bold', halign: 'center' },
        alternateRowStyles: { fillColor: [247, 250, 255] },
        columnStyles: {
            0: { halign: 'center', cellWidth: 65 },
            1: { halign: 'left' },
            2: { halign: 'left' },
            3: { halign: 'center', cellWidth: 60 },
            4: { halign: 'center', cellWidth: 75 }
        },
        didParseCell: function (data) {
            if (data.section === 'body' && data.column.index === 4) {
                var status = String(data.cell.raw).toUpperCase();
                if (status === 'COMPLETED') { data.cell.styles.textColor = [30, 158, 76]; data.cell.styles.fontStyle = 'bold'; }
                else if (status === 'CANCELLED') { data.cell.styles.textColor = [221, 51, 51]; data.cell.styles.fontStyle = 'bold'; }
                else { data.cell.styles.textColor = [47, 111, 237]; data.cell.styles.fontStyle = 'bold'; }
            }
        },
        didDrawPage: function () {
            var pageCount = doc.internal.getNumberOfPages();
            doc.setFontSize(8);
            doc.setTextColor(154, 160, 171);
            doc.text(
                'Sunrise Dental Clinic - Confidential',
                margin,
                doc.internal.pageSize.getHeight() - 20
            );
            doc.text(
                'Page ' + doc.internal.getCurrentPageInfo().pageNumber + ' of ' + pageCount,
                pageWidth - margin,
                doc.internal.pageSize.getHeight() - 20,
                { align: 'right' }
            );
        }
    });

    doc.save('DentistReport_' + lastReportDate + '.pdf');
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
            msg.className = 'success'; msg.style.display = 'block';
            msg.innerHTML = 'Appointment <b>' + appointmentNo + '</b> updated to ' + newStatus;
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        }
        loadDentistAppointments();
    })
    .catch(err => {
        if (msg) {
            msg.className = 'error'; msg.style.display = 'block';
            msg.innerHTML = 'Status update failed. ' + err.message;
        }
    });
}

function searchAppointment() {
    var searchEl = document.getElementById('searchAptNo');
    var aptNo = searchEl ? searchEl.value.trim() : '';
    var msg = document.getElementById('searchResultMsg');
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
                msg.className = 'error'; msg.style.display = 'block'; msg.textContent = err; 
            }
        });
}

function updateDentistProfile() {
    var msg = document.getElementById('profileMsg');
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
            msg.className = 'success'; msg.style.display = 'block'; msg.textContent = 'Profile updated successfully!';
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        }
        if (name) {
            loggedDentistName = name;
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
            msg.className = 'error'; msg.style.display = 'block'; msg.textContent = err; 
        }
    });
}