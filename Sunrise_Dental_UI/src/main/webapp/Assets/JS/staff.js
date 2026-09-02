const BASE_URL = "http://localhost:8080/Sunrise_Dental_Clinic/resources";

    var currentStaffUsername = "";
    var currentUserRole = "STAFF";
    var cachedAppointments = [];
    var isEditingAppointment = false;
    var cachedDentists = [];
    var cachedTreatments = [];
    var cachedPatients = [];
    var cachedStaff = [];
    var cachedBillableAppointments = [];

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

    document.addEventListener("DOMContentLoaded", function () {
        loadCurrentUserFromUrl();
        loadDropdowns();

        var navItems = document.querySelectorAll('.nav-item[data-target]');
        navItems.forEach(item => {
            item.addEventListener('click', function () { showSection(this.getAttribute('data-target')); });
        });

        var aptForm = document.getElementById('appointmentForm');
        if (aptForm) {
            aptForm.addEventListener('submit', function (e) {
                e.preventDefault();
                registerAppointment();
            });
        }

        var dentistForm = document.getElementById('dentistForm');
        if (dentistForm) {
            dentistForm.addEventListener('submit', function (e) {
                e.preventDefault();
                saveDentist();
            });
        }

        var treatmentForm = document.getElementById('treatmentForm');
        if (treatmentForm) {
            treatmentForm.addEventListener('submit', function (e) {
                e.preventDefault();
                saveTreatment();
            });
        }

        var patientForm = document.getElementById('patientForm');
        if (patientForm) {
            patientForm.addEventListener('submit', function (e) {
                e.preventDefault();
                savePatient();
            });
        }

        var staffForm = document.getElementById('staffForm');
        if (staffForm) {
            staffForm.addEventListener('submit', function (e) {
                e.preventDefault();
                saveStaff();
            });
        }

        var noticeForm = document.getElementById('noticeForm');
        if (noticeForm) {
            noticeForm.addEventListener('submit', function (e) {
                e.preventDefault();
                sendDentistNotice();
            });
        }

        document.getElementById('logoutBtn').addEventListener('click', () => {
            sessionStorage.clear();
            window.location.href = 'index.html';
        });
        loadOverview();
        loadDentistsTable();
    });

    function loadCurrentUserFromUrl() {
        const params = new URLSearchParams(window.location.search);
        const user = params.get('user');
        const role = params.get('role');

        if (user) {
            currentStaffUsername = user;
            sessionStorage.setItem('currentStaffUsername', user);
        } else {
            const savedUser = sessionStorage.getItem('currentStaffUsername');
            if (savedUser) currentStaffUsername = savedUser;
        }

        if (role) {
            currentUserRole = role;
            sessionStorage.setItem('currentUserRole', role);
        } else {
            const savedRole = sessionStorage.getItem('currentUserRole');
            if (savedRole) currentUserRole = savedRole;
        }

        document.getElementById('staffName').textContent = currentStaffUsername || 'Staff Member';
        document.getElementById('staffRole').textContent = currentUserRole.toUpperCase();
        if (currentStaffUsername) {
            document.getElementById('staffAvatar').textContent = currentStaffUsername.charAt(0).toUpperCase();
        }

        if (currentUserRole.toUpperCase() === 'ADMIN') {
            document.getElementById('addStaffBtn').style.display = 'block';
            document.getElementById('addDentistBtn').style.display = 'block';
            document.getElementById('addTreatmentBtn').style.display = 'block';
        }

        window.history.replaceState({}, document.title, window.location.pathname);
    }

    function loadDropdowns() {
        const dSelect = document.getElementById('dentistId');
        const tSelect = document.getElementById('treatmentId');
        if (!dSelect || !tSelect) return;

        dSelect.innerHTML = '<option value="">-- Select Dentist --</option>';
        tSelect.innerHTML = '<option value="">-- Select Treatment --</option>';

        fetch(BASE_URL + '/dentists')
            .then(res => res.json())
            .then(data => {
                cachedDentists = data || [];
                data.forEach(d => {
                    const opt = document.createElement('option');
                    opt.value = d.dentistId || d.dentist_id;
                    const dName = d.dentistName || d.dentist_name;
                    opt.textContent = `${dName} (${d.specialization || ''})`;
                    dSelect.appendChild(opt);
                });
            }).catch(err => console.error("Error loading dentists:", err));

        fetch(BASE_URL + '/treatments')
            .then(res => res.json())
            .then(data => {
                cachedTreatments = data || [];
                data.forEach(t => {
                    const opt = document.createElement('option');
                    opt.value = t.treatmentId || t.treatment_id;
                    const tName = t.treatmentName || t.treatment_name;
                    const tCost = t.treatmentCost !== undefined ? t.treatmentCost : t.treatment_cost;
                    opt.textContent = `${tName} (Rs. ${tCost})`;
                    tSelect.appendChild(opt);
                });
            }).catch(err => console.error("Error loading treatments:", err));
    }

    function showSection(targetId) {
        var sections = document.querySelectorAll('.section');
        sections.forEach(s => s.classList.remove('active'));
        var targetSec = document.getElementById(targetId);
        if (targetSec) targetSec.classList.add('active');

        var navItems = document.querySelectorAll('.nav-item[data-target]');
        navItems.forEach(n => n.classList.remove('active'));
        var activeNav = document.querySelector('.nav-item[data-target="' + targetId + '"]');
        if (activeNav) activeNav.classList.add('active');

        if (targetId === 'sec-overview') {
            loadOverview();
        } else if (targetId === 'sec-reports') {
            var dateInput = document.getElementById('reportDateInput');
            var monthInput = document.getElementById('reportMonthInput');
            var today = new Date();
            if (dateInput && !dateInput.value) {
                dateInput.value = today.toISOString().slice(0, 10);
            }
            if (monthInput && !monthInput.value) {
                monthInput.value = today.toISOString().slice(0, 7);
            }
        } else if (targetId === 'sec-appointments') {
            loadAppointments();
        } else if (targetId === 'sec-register') {
            if (!isEditingAppointment) {
                document.getElementById('appointmentForm').reset();
                resetAppointmentTimeUI();
                document.getElementById('editAppointmentNo').value = '';
                resetAppointmentFormLabels();
                hideExistingPatientTag();
            }
            isEditingAppointment = false;
            loadDropdowns();
            if (!cachedPatients || cachedPatients.length === 0) loadPatientsForAutocomplete();
        } else if (targetId === 'sec-dentists') {
            loadDentistsTable();
        } else if (targetId === 'sec-treatments') {
            loadTreatmentsTable();
        } else if (targetId === 'sec-patients') {
            loadPatientsTable();
        } else if (targetId === 'sec-staff') {
            loadStaffTable();
        } else if (targetId === 'sec-bill') {
            backToBillList();
            loadBillableAppointments();
        } else if (targetId === 'sec-profile') {
            loadStaffProfileIntoForm();
        } else if (targetId === 'sec-notices') {
            loadNoticeDentistDropdown();
        }
    }

    /**
     * Computes and displays the 3 real-time Overview stat cards
     * (Total / Pending / Completed) from the clinic-wide appointment list.
     * Called whenever appointment data is freshly fetched anywhere in the
     * dashboard, so the numbers always stay in sync with the database.
     */
    function renderOverviewCards() {
        var totalEl = document.getElementById('ov-total');
        var pendingEl = document.getElementById('ov-pending');
        var completedEl = document.getElementById('ov-completed');
        var cancelledEl = document.getElementById('ov-cancelled');
        var dentistsEl = document.getElementById('ov-dentists');
        var treatmentsEl = document.getElementById('ov-treatments');
        var patientsEl = document.getElementById('ov-patients');
        var staffEl = document.getElementById('ov-staff');
        if (!totalEl) return;

        var appts = cachedAppointments || [];
        var pendingCount = appts.filter(a => (a.status || 'PENDING').toUpperCase() === 'PENDING').length;
        var completedCount = appts.filter(a => (a.status || '').toUpperCase() === 'COMPLETED').length;
        var cancelledCount = appts.filter(a => (a.status || '').toUpperCase() === 'CANCELLED').length;

        totalEl.textContent = appts.length;
        if (pendingEl) pendingEl.textContent = pendingCount;
        if (completedEl) completedEl.textContent = completedCount;
        if (cancelledEl) cancelledEl.textContent = cancelledCount;
        if (dentistsEl) dentistsEl.textContent = (cachedDentists || []).length;
        if (treatmentsEl) treatmentsEl.textContent = (cachedTreatments || []).length;
        if (patientsEl) patientsEl.textContent = (cachedPatients || []).length;
        if (staffEl) staffEl.textContent = (cachedStaff || []).length;
    }

    /**
     * Loads Overview by fetching appointments, dentists, treatments,
     * patients and staff in parallel and rendering all 8 real-time
     * stat cards as each dataset arrives.
     */
    function loadOverview() {
        loadAppointments();
        loadDentistsTable();
        loadTreatmentsTable();
        loadPatientsTable();
        loadStaffTable();
    }

    var currentReportType = 'daily';
    var lastReportList = [];
    var lastReportStats = null;
    var lastReportPeriodValue = '';
    var lastReportPeriodLabel = '';

    function setReportType(type) {
        currentReportType = type;
        document.getElementById('reportTypeDaily').classList.toggle('active', type === 'daily');
        document.getElementById('reportTypeMonthly').classList.toggle('active', type === 'monthly');
        document.getElementById('reportDateInput').style.display = type === 'daily' ? '' : 'none';
        document.getElementById('reportMonthInput').style.display = type === 'monthly' ? '' : 'none';
        document.getElementById('reportTitle').textContent = type === 'daily' ? 'Daily Report' : 'Monthly Report';
        document.getElementById('reportSubtitle').textContent = type === 'daily'
            ? 'Your appointments and earnings summary for a selected day'
            : 'Your appointments and earnings summary for a selected month';
        document.getElementById('reportOutput').style.display = 'none';
        document.getElementById('downloadReportBtn').style.display = 'none';
    }
    window.setReportType = setReportType;

    function generateReport() {
        var msg = document.getElementById('reportResultMsg');
        var output = document.getElementById('reportOutput');
        var downloadBtn = document.getElementById('downloadReportBtn');
        msg.style.display = 'none';
        output.style.display = 'none';
        downloadBtn.style.display = 'none';

        var periodValue;
        if (currentReportType === 'daily') {
            periodValue = document.getElementById('reportDateInput').value;
            if (!periodValue) {
                msg.className = 'error';
                msg.style.display = 'block';
                msg.textContent = 'Please select a date first.';
                return;
            }
        } else {
            periodValue = document.getElementById('reportMonthInput').value; // "yyyy-MM"
            if (!periodValue) {
                msg.className = 'error';
                msg.style.display = 'block';
                msg.textContent = 'Please select a month first.';
                return;
            }
        }

        Promise.all([
            fetch(BASE_URL + '/appointment').then(res => res.json()).catch(() => []),
            fetchDentistsData().catch(() => []),
            fetch(BASE_URL + '/treatments').then(res => res.json()).catch(() => [])
        ]).then(([appts, dentists, treatments]) => {
            var all = appts || [];
            var filtered = all.filter(a => {
                var d = a.appointmentDate || a.appointment_date || '';
                return currentReportType === 'daily' ? d === periodValue : d.startsWith(periodValue);
            });

            renderReport(filtered, dentists || [], treatments || []);
            lastReportList = filtered;
            lastReportPeriodValue = periodValue;
            updatePrintLetterhead(periodValue);
            output.style.display = 'block';
            downloadBtn.style.display = 'inline-flex';
        })
        .catch(() => {
            msg.className = 'error';
            msg.style.display = 'block';
            msg.textContent = 'Failed to load data for the report.';
        });
    }
    window.generateReport = generateReport;

    function updatePrintLetterhead(periodValue) {
        var titleEl = document.getElementById('printReportTitle');
        var periodEl = document.getElementById('printReportPeriod');
        var generatedEl = document.getElementById('printGeneratedDate');
        if (titleEl) titleEl.textContent = currentReportType === 'daily' ? 'Daily Report' : 'Monthly Report';
        if (periodEl) periodEl.textContent = formatReportPeriodLabel(currentReportType, periodValue);
        if (generatedEl) generatedEl.textContent = new Date().toLocaleString('en-GB', {
            day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
        });
    }

    function formatReportPeriodLabel(type, value) {
        if (!value) return '—';
        if (type === 'daily') {
            var d = new Date(value + 'T00:00:00');
            if (isNaN(d.getTime())) return value;
            return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'long', year: 'numeric' });
        }
        var parts = value.split('-'); // "yyyy-MM"
        if (parts.length !== 2) return value;
        var monthDate = new Date(Number(parts[0]), Number(parts[1]) - 1, 1);
        if (isNaN(monthDate.getTime())) return value;
        return monthDate.toLocaleDateString('en-GB', { month: 'long', year: 'numeric' });
    }

    function generateReportPDF() {
        var msg = document.getElementById('reportResultMsg');
        if (!lastReportPeriodValue || !lastReportStats) {
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
        var contentWidth = pageWidth - margin * 2;
        var reportTypeLabel = currentReportType === 'daily' ? 'Daily Report' : 'Monthly Report';
        var periodLabel = formatReportPeriodLabel(currentReportType, lastReportPeriodValue);

        var headerHeight = 96;
        doc.setFillColor(37, 99, 227);
        doc.rect(0, 0, pageWidth, headerHeight, 'F');
        doc.setFillColor(29, 84, 199);
        doc.rect(0, 0, pageWidth, 4, 'F');

        var badgeSize = 38;
        var badgeX = margin;
        var badgeY = (headerHeight - badgeSize) / 2 - 4;
        doc.setFillColor(255, 255, 255);
        doc.roundedRect(badgeX, badgeY, badgeSize, badgeSize, 10, 10, 'F');
        doc.setTextColor(37, 99, 227);
        doc.setFont('helvetica', 'bold');
        doc.setFontSize(18);
        doc.text('S', badgeX + badgeSize / 2, badgeY + badgeSize / 2 + 6.5, { align: 'center' });

        var textX = badgeX + badgeSize + 14;
        doc.setTextColor(255, 255, 255);
        doc.setFont('helvetica', 'bold');
        doc.setFontSize(19);
        doc.text('Sunrise Dental Clinic', textX, badgeY + 17);
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(10.5);
        doc.setTextColor(219, 234, 254);
        doc.text('Professional Dental Care', textX, badgeY + 33);

        doc.setFont('helvetica', 'bold');
        doc.setFontSize(9);
        var pillLabel = reportTypeLabel.toUpperCase();
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
        doc.text('GENERATED BY', margin + 18, metaLabelY);
        doc.text('GENERATED ON', metaMidX + 18, metaLabelY);

        doc.setFont('helvetica', 'bold');
        doc.setFontSize(11);
        doc.setTextColor(31, 36, 48);
        doc.text(String(currentStaffUsername || '-'), margin + 18, metaValueY);
        doc.text(new Date().toLocaleString('en-GB', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }), metaMidX + 18, metaValueY);

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

        var tableRows = (lastReportList || []).map(function (a) {
            var rawDentist = a.dentistName || a.dentist_name || '';
            var dentistLabel = rawDentist ? (rawDentist.startsWith('Dr.') ? rawDentist : 'Dr. ' + rawDentist) : '-';
            return [
                a.appointmentNo || a.appointment_no || '-',
                a.patientName || a.patient_name || '-',
                dentistLabel,
                a.treatmentName || a.treatment_name || '-',
                formatTimeTo12Hour(a.appointmentTime || a.appointment_time || ''),
                (a.status || 'PENDING').toUpperCase()
            ];
        });

        doc.autoTable({
            head: [['Appt No', 'Patient Name', 'Dentist', 'Treatment', 'Time', 'Status']],
            body: tableRows.length ? tableRows : [['-', 'No appointments found for this period', '-', '-', '-', '-']],
            startY: boxTop + boxHeight + 26,
            margin: { left: margin, right: margin },
            styles: { font: 'helvetica', fontSize: 9, cellPadding: { top: 9, bottom: 9, left: 8, right: 8 }, textColor: [31, 36, 48], lineColor: [238, 240, 244], lineWidth: 0.5, valign: 'middle' },
            headStyles: { fillColor: [37, 99, 227], textColor: [255, 255, 255], fontStyle: 'bold', fontSize: 9, halign: 'center', cellPadding: { top: 10, bottom: 10, left: 8, right: 8 } },
            alternateRowStyles: { fillColor: [249, 251, 255] },
            columnStyles: {
                0: { halign: 'center', cellWidth: 62, fontStyle: 'bold' },
                1: { halign: 'left', cellWidth: 118 },
                2: { halign: 'left', cellWidth: 100 },
                3: { halign: 'left' },
                4: { halign: 'center', cellWidth: 55 },
                5: { halign: 'center', cellWidth: 78 }
            },
            didParseCell: function (data) {
                if (data.section === 'body' && data.column.index === 5) {
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

        var filenameSafePeriod = lastReportPeriodValue.replace(/[^0-9a-zA-Z-]/g, '');
        doc.save((currentReportType === 'daily' ? 'DailyReport_' : 'MonthlyReport_') + filenameSafePeriod + '.pdf');
    }
    window.generateReportPDF = generateReportPDF;

    function renderReport(list, dentists, treatments) {
        var completed = list.filter(a => (a.status || '').toUpperCase() === 'COMPLETED').length;
        var pending = list.filter(a => (a.status || 'PENDING').toUpperCase() === 'PENDING').length;
        var cancelled = list.filter(a => (a.status || '').toUpperCase() === 'CANCELLED').length;

        var earnings = 0;
        list.forEach(a => {
            if ((a.status || '').toUpperCase() !== 'COMPLETED') return;
            var dentistId = a.dentistId || a.dentist_id;
            var treatmentId = a.treatmentId || a.treatment_id;
            var dentist = dentists.find(d => (d.dentistId || d.dentist_id) == dentistId);
            var treatment = treatments.find(t => (t.treatmentId || t.treatment_id) == treatmentId);
            var fee = dentist ? Number(dentist.consultationFee !== undefined ? dentist.consultationFee : dentist.consultation_fee) || 0 : 0;
            var cost = treatment ? Number(treatment.treatmentCost !== undefined ? treatment.treatmentCost : treatment.treatment_cost) || 0 : 0;
            earnings += fee + cost;
        });

        document.getElementById('rep-total').textContent = list.length;
        document.getElementById('rep-completed').textContent = completed;
        document.getElementById('rep-pending').textContent = pending;
        document.getElementById('rep-cancelled').textContent = cancelled;
        document.getElementById('rep-earnings').textContent = 'LKR ' + earnings.toFixed(2);

        lastReportStats = { total: list.length, completed: completed, pending: pending, cancelled: cancelled, earnings: earnings };

        var tbody = document.getElementById('reportTableBody');
        if (list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:20px; color:#9aa0ab;">No appointments found for this period.</td></tr>';
            return;
        }

        tbody.innerHTML = list.map(a => {
            var rawDentist = a.dentistName || a.dentist_name || '';
            var dentistLabel = rawDentist ? (rawDentist.startsWith('Dr.') ? rawDentist : 'Dr. ' + rawDentist) : '-';
            var status = (a.status || 'PENDING').toUpperCase();
            return `<tr>
                <td class="align-left"><b>${a.appointmentNo || a.appointment_no || '-'}</b></td>
                <td class="align-left">${a.patientName || a.patient_name || '-'}</td>
                <td class="align-left">${dentistLabel}</td>
                <td class="align-left">${a.treatmentName || a.treatment_name || '-'}</td>
                <td>${formatTimeTo12Hour(a.appointmentTime || a.appointment_time || '')}</td>
                <td><span class="badge ${status}">${status}</span></td>
            </tr>`;
        }).join('');
    }

    function renderTableData(tbodyId, data) {
        var tbody = document.getElementById(tbodyId);
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="10" style="text-align:center; padding:24px; color:#9aa0ab;">No appointments found.</td></tr>';
            return;
        }

        var isAdmin = currentUserRole.toUpperCase() === 'ADMIN';
        var rowsHTML = '';
        data.forEach(a => {
            var status = (a.status || 'PENDING').toUpperCase();
            var rawDentist = a.dentistName || a.dentist_name || '';
            var dentist = rawDentist ? (rawDentist.startsWith('Dr.') ? rawDentist : 'Dr. ' + rawDentist) : '-';
            var aptNo = a.appointmentNo || a.appointment_no || '-';
            var rawTime = a.appointmentTime || a.appointment_time || '';
            var formattedTime = formatTimeTo12Hour(rawTime);

            var actionButtons = (status === 'PENDING' ? `<button class="btn-action btn-edit" onclick="editAppointment('${aptNo}')">Edit</button>` : '')
                + (isAdmin ? `<button class="btn-action btn-delete" onclick="deleteAppointment('${aptNo}')">Delete</button>` : '');

            rowsHTML += `<tr>
                <td><b>${aptNo}</b></td>
                <td>${a.patientName || a.patient_name || '-'}</td>
                <td>${a.address || '-'}</td>
                <td>${a.contactNo || a.contact_no || '-'}</td>
                <td>${dentist}</td>
                <td>${a.treatmentName || a.treatment_name || '-'}</td>
                <td>${a.appointmentDate || a.appointment_date || '-'}</td>
                <td>${formattedTime}</td>
                <td><span class="badge ${status}">${status}</span></td>
                <td class="actions-cell">${actionButtons}</td>
            </tr>`;
        });
        tbody.innerHTML = rowsHTML;
    }

    function loadAppointments() {
        var tbody = document.getElementById('appointmentsTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="10" style="text-align:center; padding:24px; color:#9aa0ab;">Loading appointments...</td></tr>';

        fetch(BASE_URL + '/appointment')
            .then(res => res.json())
            .then(data => {
                cachedAppointments = (data || []).slice().sort((a, b) => {
                    var aNo = (a.appointmentNo || a.appointment_no || '').toString();
                    var bNo = (b.appointmentNo || b.appointment_no || '').toString();
                    var aNum = parseInt(aNo.replace(/[^0-9]/g, ''), 10) || 0;
                    var bNum = parseInt(bNo.replace(/[^0-9]/g, ''), 10) || 0;
                    return aNum - bNum;
                });
                renderTableData('appointmentsTableBody', cachedAppointments);
                renderOverviewCards();
            })
            .catch(err => {
                console.error("Error fetching appointments:", err);
                if (tbody) tbody.innerHTML = '<tr><td colspan="10" style="text-align:center; color:#d33; padding:24px;">Failed to load data</td></tr>';
            });
    }

    function filterSearchTable() {
        var query = document.getElementById('searchAptNo').value.toLowerCase().trim();
        if (!query) {
            renderTableData('appointmentsTableBody', cachedAppointments);
            return;
        }
        var filtered = cachedAppointments.filter(a => {
            var aptNo = (a.appointmentNo || a.appointment_no || '').toLowerCase();
            var pName = (a.patientName || a.patient_name || '').toLowerCase();
            var contact = (a.contactNo || a.contact_no || '').toLowerCase();
            return aptNo.includes(query) || pName.includes(query) || contact.includes(query);
        });
        renderTableData('appointmentsTableBody', filtered);
    }

    function searchAppointment() {
        var query = document.getElementById('searchAptNo').value.trim();
        if (!query) {
            loadAppointments();
            return;
        }

        fetch(BASE_URL + '/appointment/' + encodeURIComponent(query))
            .then(res => res.status === 200 ? res.json() : Promise.reject('No appointment found matching query'))
            .then(a => {
                renderTableData('appointmentsTableBody', [a]);
            })
            .catch(err => {
                filterSearchTable();
            });
    }

    function fetchDentistsData() {
        return fetch(BASE_URL + '/dentists')
            .then(res => {
                if (!res.ok) {
                    return fetch(BASE_URL + '/dentists').then(r => r.json());
                }
                return res.json();
            });
    }

    function loadDentistsTable() {
        var tbody = document.getElementById('dentistsTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:24px; color:#9aa0ab;">Loading dentists...</td></tr>';

        fetchDentistsData()
            .then(data => {
                cachedDentists = data || [];
                renderDentistsTableData(cachedDentists);
                renderOverviewCards();
            })
            .catch(err => {
                console.error("Error fetching dentists:", err);
                if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#d33; padding:24px;">Failed to load dentists data</td></tr>';
            });
    }

    function renderDentistsTableData(data) {
        var tbody = document.getElementById('dentistsTableBody');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:24px; color:#9aa0ab;">No dentists found.</td></tr>';
            return;
        }

        data.sort((a, b) => (a.dentistId || a.dentist_id || 0) - (b.dentistId || b.dentist_id || 0));

        var isAdmin = currentUserRole.toUpperCase() === 'ADMIN';
        var rowsHTML = '';
        data.forEach(d => {
            var id = d.dentistId || d.dentist_id || '-';
            var rawName = d.dentistName || d.dentist_name || '-';
            var name = rawName.startsWith('Dr.') ? rawName : 'Dr. ' + rawName;
            var spec = d.specialization || '-';
            var contact = d.contactNo || d.contact_no || d.contact || '-';
            
            var rawFee = d.consultationFee !== undefined ? d.consultationFee : d.consultation_fee;
            var feeText = (rawFee !== undefined && rawFee !== null && rawFee !== '') ? ('Rs. ' + Number(rawFee).toFixed(2)) : 'Rs. 0.00';

            var actionCell = isAdmin
                ? `<button class="btn-action btn-edit" onclick="editDentist(${id})">Edit</button>
                   <button class="btn-action btn-delete" onclick="deleteDentist(${id})">Delete</button>`
                : `<span style="color:#9aa0ab; font-size:12px;">Restricted</span>`;

            rowsHTML += `<tr>
                <td><b>${id}</b></td>
                <td>${name}</td>
                <td>${spec}</td>
                <td>${contact}</td>
                <td>${feeText}</td>
                <td class="actions-cell">${actionCell}</td>
            </tr>`;
        });
        tbody.innerHTML = rowsHTML;
    }

    function filterDentistTable() {
        var query = document.getElementById('searchDentistInput').value.toLowerCase().trim();
        if (!query) {
            renderDentistsTableData(cachedDentists);
            return;
        }
        var filtered = cachedDentists.filter(d => {
            var name = (d.dentistName || d.dentist_name || '').toLowerCase();
            var spec = (d.specialization || '').toLowerCase();
            var contact = (d.contactNo || d.contact_no || d.contact || '').toLowerCase();
            return name.includes(query) || spec.includes(query) || contact.includes(query);
        });
        renderDentistsTableData(filtered);
    }

    function openDentistModal() {
        document.getElementById('dentistForm').reset();
        document.getElementById('editDentistId').value = '';
        document.getElementById('dentistFormTitle').textContent = 'Add New Dentist';
        document.getElementById('saveDentistBtn').textContent = 'Save Dentist';
        document.getElementById('dentistsTableCard').style.display = 'none';
        document.getElementById('dentistFormCard').style.display = 'block';
        document.getElementById('dentistResultMsg').style.display = 'none';
        resetPasswordVisibility('dPassword', 'dPassword-toggle');
        window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
    }

    function closeDentistModal() {
        document.getElementById('dentistFormCard').style.display = 'none';
        document.getElementById('dentistsTableCard').style.display = 'block';
        resetPasswordVisibility('dPassword', 'dPassword-toggle');
    }

    function editDentist(id) {
        var dentist = cachedDentists.find(d => (d.dentistId || d.dentist_id) === id);
        if (!dentist) return;

        document.getElementById('editDentistId').value = dentist.dentistId || dentist.dentist_id;
        document.getElementById('dName').value = dentist.dentistName || dentist.dentist_name || '';
        document.getElementById('dSpecialization').value = dentist.specialization || '';
        document.getElementById('dContact').value = dentist.contactNo || dentist.contact_no || dentist.contact || '';
        
        var rawFee = dentist.consultationFee !== undefined ? dentist.consultationFee : dentist.consultation_fee;
        document.getElementById('dFee').value = rawFee !== undefined && rawFee !== null ? rawFee : '';
        
        document.getElementById('dUsername').value = dentist.username || '';
        document.getElementById('dPassword').value = dentist.password || '';

        document.getElementById('dentistFormTitle').textContent = 'Edit Dentist Profile';
        document.getElementById('saveDentistBtn').textContent = 'Update Dentist';
        document.getElementById('dentistsTableCard').style.display = 'none';
        document.getElementById('dentistFormCard').style.display = 'block';
        document.getElementById('dentistResultMsg').style.display = 'none';
        resetPasswordVisibility('dPassword', 'dPassword-toggle');
        window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
    }

    function saveDentist() {
        var id = document.getElementById('editDentistId').value;
        var resultMsg = document.getElementById('dentistResultMsg');

        var payload = {
            dentistName: document.getElementById('dName').value.trim(),
            specialization: document.getElementById('dSpecialization').value.trim(),
            contactNo: document.getElementById('dContact').value.trim(),
            consultationFee: parseFloat(document.getElementById('dFee').value || 0),
            username: document.getElementById('dUsername').value.trim(),
            password: document.getElementById('dPassword').value.trim()
        };

        var url = id ? (BASE_URL + '/dentists/' + id + '?actingUsername=' + encodeURIComponent(currentStaffUsername)) : BASE_URL + '/dentists';
        var method = id ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.ok ? res.text().then(t => t ? JSON.parse(t) : {}) : res.text().then(t => { throw new Error(t); }))
        .then(data => {
            resultMsg.className = 'success'; resultMsg.style.display = 'block';
            resultMsg.innerHTML = id ? '✅ Dentist profile updated successfully!' : '✅ New dentist added successfully!';
            loadDentistsTable();
            loadDropdowns();
            setTimeout(() => {
                closeDentistModal();
                resultMsg.style.display = 'none';
            }, 1500);
        })
        .catch(err => {
            resultMsg.className = 'error'; resultMsg.style.display = 'block';
            resultMsg.innerHTML = 'Operation failed. ' + err.message;
        });
    }

    function deleteDentist(id) {
        if (!confirm("Are you sure you want to delete this dentist?")) return;

        fetch(BASE_URL + '/dentists/delete/' + id + '?actingUsername=' + encodeURIComponent(currentStaffUsername), { method: 'POST' })
            .then(res => {
                if (res.ok) {
                    loadDentistsTable();
                    loadDropdowns();
                } else {
                    res.text().then(t => alert('Failed to delete dentist: ' + t));
                }
            })
            .catch(err => console.error("Error deleting dentist:", err));
    }

    function loadTreatmentsTable() {
        var tbody = document.getElementById('treatmentsTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; padding:24px; color:#9aa0ab;">Loading treatments...</td></tr>';

        fetch(BASE_URL + '/treatments')
            .then(res => res.json())
            .then(data => {
                cachedTreatments = data || [];
                renderTreatmentsTableData(cachedTreatments);
                renderOverviewCards();
            })
            .catch(err => {
                console.error("Error fetching treatments:", err);
                if (tbody) tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:#d33; padding:24px;">Failed to load treatments data</td></tr>';
            });
    }

    function renderTreatmentsTableData(data) {
        var tbody = document.getElementById('treatmentsTableBody');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; padding:24px; color:#9aa0ab;">No treatments found.</td></tr>';
            return;
        }

        data.sort((a, b) => (a.treatmentId || a.treatment_id || 0) - (b.treatmentId || b.treatment_id || 0));

        var isAdmin = currentUserRole.toUpperCase() === 'ADMIN';
        var rowsHTML = '';
        data.forEach(t => {
            var id = t.treatmentId || t.treatment_id || '-';
            var name = t.treatmentName || t.treatment_name || '-';
            var rawCost = t.treatmentCost !== undefined ? t.treatmentCost : t.treatment_cost;
            var costText = (rawCost !== undefined && rawCost !== null && rawCost !== '') ? ('Rs. ' + Number(rawCost).toFixed(2)) : 'Rs. 0.00';

            var actionCell = isAdmin
                ? `<button class="btn-action btn-edit" onclick="editTreatment(${id})">Edit</button>
                   <button class="btn-action btn-delete" onclick="deleteTreatment(${id})">Delete</button>`
                : `<span style="color:#9aa0ab; font-size:12px;">Restricted</span>`;

            rowsHTML += `<tr>
                <td><b>${id}</b></td>
                <td>${name}</td>
                <td>${costText}</td>
                <td class="actions-cell">${actionCell}</td>
            </tr>`;
        });
        tbody.innerHTML = rowsHTML;
    }

    function filterTreatmentTable() {
        var query = document.getElementById('searchTreatmentInput').value.toLowerCase().trim();
        if (!query) {
            renderTreatmentsTableData(cachedTreatments);
            return;
        }
        var filtered = cachedTreatments.filter(t => {
            var name = (t.treatmentName || t.treatment_name || '').toLowerCase();
            return name.includes(query);
        });
        renderTreatmentsTableData(filtered);
    }

    function openTreatmentModal() {
        document.getElementById('treatmentForm').reset();
        document.getElementById('editTreatmentId').value = '';
        document.getElementById('treatmentFormTitle').textContent = 'Add Treatment';
        document.getElementById('saveTreatmentBtn').textContent = 'Save Treatment';
        document.getElementById('treatmentsTableCard').style.display = 'none';
        document.getElementById('treatmentFormCard').style.display = 'block';
        document.getElementById('treatmentResultMsg').style.display = 'none';
        window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
    }

    function closeTreatmentModal() {
        document.getElementById('treatmentFormCard').style.display = 'none';
        document.getElementById('treatmentsTableCard').style.display = 'block';
    }

    function editTreatment(id) {
        var treatment = cachedTreatments.find(t => (t.treatmentId || t.treatment_id) === id);
        if (!treatment) return;

        document.getElementById('editTreatmentId').value = treatment.treatmentId || treatment.treatment_id;
        document.getElementById('tName').value = treatment.treatmentName || treatment.treatment_name || '';

        var rawCost = treatment.treatmentCost !== undefined ? treatment.treatmentCost : treatment.treatment_cost;
        document.getElementById('tCost').value = rawCost !== undefined && rawCost !== null ? rawCost : '';

        document.getElementById('treatmentFormTitle').textContent = 'Edit Treatment';
        document.getElementById('saveTreatmentBtn').textContent = 'Update Treatment';
        document.getElementById('treatmentsTableCard').style.display = 'none';
        document.getElementById('treatmentFormCard').style.display = 'block';
        document.getElementById('treatmentResultMsg').style.display = 'none';
        window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
    }

    function saveTreatment() {
        var id = document.getElementById('editTreatmentId').value;
        var resultMsg = document.getElementById('treatmentResultMsg');

        var payload = {
            treatmentName: document.getElementById('tName').value.trim(),
            treatmentCost: parseFloat(document.getElementById('tCost').value || 0)
        };

        var url = id ? (BASE_URL + '/treatments/' + id + '?actingUsername=' + encodeURIComponent(currentStaffUsername)) : BASE_URL + '/treatments';
        var method = id ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.ok ? res.text().then(t => t ? JSON.parse(t) : {}) : res.text().then(t => { throw new Error(t); }))
        .then(data => {
            resultMsg.className = 'success'; resultMsg.style.display = 'block';
            resultMsg.innerHTML = id ? '✅ Treatment updated successfully!' : '✅ New treatment added successfully!';
            loadTreatmentsTable();
            loadDropdowns();
            setTimeout(() => {
                closeTreatmentModal();
                resultMsg.style.display = 'none';
            }, 1500);
        })
        .catch(err => {
            resultMsg.className = 'error'; resultMsg.style.display = 'block';
            resultMsg.innerHTML = 'Operation failed. ' + err.message;
        });
    }

    function deleteTreatment(id) {
        if (!confirm("Are you sure you want to delete this treatment?")) return;

        fetch(BASE_URL + '/treatments/delete/' + id + '?actingUsername=' + encodeURIComponent(currentStaffUsername), { method: 'POST' })
            .then(res => {
                if (res.ok) {
                    loadTreatmentsTable();
                    loadDropdowns();
                } else {
                    res.text().then(t => alert('Failed to delete treatment: ' + t));
                }
            })
            .catch(err => console.error("Error deleting treatment:", err));
    }
    
    function loadPatientsTable() {
        var tbody = document.getElementById('patientsTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding:24px; color:#9aa0ab;">Loading patients...</td></tr>';

        fetch(BASE_URL + '/patients')
            .then(res => res.json())
            .then(data => {
                cachedPatients = data || [];
                renderPatientsTableData(cachedPatients);
                renderOverviewCards();
            })
            .catch(err => {
                console.error("Error fetching patients:", err);
                if (tbody) tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#d33; padding:24px;">Failed to load patients data</td></tr>';
            });
    }

    function renderPatientsTableData(data) {
        var tbody = document.getElementById('patientsTableBody');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding:24px; color:#9aa0ab;">No patients found.</td></tr>';
            return;
        }

        data.sort((a, b) => (a.patientId || a.patient_id || 0) - (b.patientId || b.patient_id || 0));

        var isAdmin = currentUserRole.toUpperCase() === 'ADMIN';
        var rowsHTML = '';
        data.forEach(p => {
            var id = p.patientId || p.patient_id || '-';
            var name = p.patientName || p.patient_name || '-';
            var address = p.address || '-';
            var contact = p.contactNo || p.contact_no || '-';

            var actionCell = `<button class="btn-action btn-edit" onclick="editPatient(${id})">Edit</button>`
                + (isAdmin ? `<button class="btn-action btn-delete" onclick="deletePatient(${id})">Delete</button>` : '');

            rowsHTML += `<tr>
                <td><b>${id}</b></td>
                <td>${name}</td>
                <td>${address}</td>
                <td>${contact}</td>
                <td class="actions-cell">${actionCell}</td>
            </tr>`;
        });
        tbody.innerHTML = rowsHTML;
    }

    function filterPatientTable() {
        var query = document.getElementById('searchPatientInput').value.toLowerCase().trim();
        if (!query) {
            renderPatientsTableData(cachedPatients);
            return;
        }
        var filtered = cachedPatients.filter(p => {
            var name = (p.patientName || p.patient_name || '').toLowerCase();
            var contact = (p.contactNo || p.contact_no || '').toLowerCase();
            var address = (p.address || '').toLowerCase();
            return name.includes(query) || contact.includes(query) || address.includes(query);
        });
        renderPatientsTableData(filtered);
    }

    function editPatient(id) {
        var patient = cachedPatients.find(p => (p.patientId || p.patient_id) === id);
        if (!patient) return;

        document.getElementById('editPatientId').value = patient.patientId || patient.patient_id;
        document.getElementById('pName').value = patient.patientName || patient.patient_name || '';
        document.getElementById('pAddress').value = patient.address || '';
        document.getElementById('pContact').value = patient.contactNo || patient.contact_no || '';

        document.getElementById('patientsTableCard').style.display = 'none';
        document.getElementById('patientFormCard').style.display = 'block';
        document.getElementById('patientResultMsg').style.display = 'none';
        window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
    }

    function closePatientModal() {
        document.getElementById('patientFormCard').style.display = 'none';
        document.getElementById('patientsTableCard').style.display = 'block';
    }

    function savePatient() {
        var id = document.getElementById('editPatientId').value;
        var resultMsg = document.getElementById('patientResultMsg');

        var payload = {
            patientName: document.getElementById('pName').value.trim(),
            address: document.getElementById('pAddress').value.trim(),
            contactNo: document.getElementById('pContact').value.trim()
        };

        fetch(BASE_URL + '/patients/' + id, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.ok ? res.json() : res.text().then(t => { throw new Error(t); }))
        .then(data => {
            resultMsg.className = 'success'; resultMsg.style.display = 'block';
            resultMsg.innerHTML = '✅ Patient updated successfully!';
            loadPatientsTable();
            setTimeout(() => {
                closePatientModal();
                resultMsg.style.display = 'none';
            }, 1200);
        })
        .catch(err => {
            resultMsg.className = 'error'; resultMsg.style.display = 'block';
            resultMsg.innerHTML = 'Update failed. ' + err.message;
        });
    }

    function deletePatient(id) {
        if (!confirm("Are you sure you want to delete this patient? This will fail if they still have appointments.")) return;

        fetch(BASE_URL + '/patients/' + id + '?actingUsername=' + encodeURIComponent(currentStaffUsername), { method: 'DELETE' })
            .then(res => {
                if (res.ok) {
                    loadPatientsTable();
                } else {
                    res.text().then(t => alert('Failed to delete patient: ' + t));
                }
            })
            .catch(err => console.error("Error deleting patient:", err));
    }

    /* --- STAFF MEMBERS MANAGEMENT SCRIPTS --- */

    function loadStaffTable() {
        var tbody = document.getElementById('staffTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:24px; color:#9aa0ab;">Loading staff...</td></tr>';

        fetch(BASE_URL + '/staff')
            .then(res => res.json())
            .then(data => {
                cachedStaff = data || [];
                renderStaffTableData(cachedStaff);
                renderOverviewCards();
            })
            .catch(err => {
                console.error("Error fetching staff:", err);
                if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#d33; padding:24px;">Failed to load staff data</td></tr>';
            });
    }

    function renderStaffTableData(data) {
        var tbody = document.getElementById('staffTableBody');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:24px; color:#9aa0ab;">No staff members found.</td></tr>';
            return;
        }

        data.sort((a, b) => (a.staffId || a.staff_id || 0) - (b.staffId || b.staff_id || 0));

        var rowsHTML = '';
        var isAdmin = currentUserRole.toUpperCase() === 'ADMIN';

        data.forEach(s => {
            var id = s.staffId || s.staff_id || '-';
            var name = s.staffName || s.staff_name || '-';
            var username = s.username || '-';
            var contact = s.contactNo || s.contact_no || '-';
            var role = (s.role || 'STAFF').toUpperCase();

            var actionButtons = isAdmin ? `
                <button class="btn-action btn-edit" onclick="editStaff(${id})">Edit</button>
                <button class="btn-action btn-delete" onclick="deleteStaff(${id})">Delete</button>
            ` : `<span style="color:#9aa0ab; font-size:12px;">Restricted</span>`;

            rowsHTML += `<tr>
                <td><b>${id}</b></td>
                <td>${name}</td>
                <td>${username}</td>
                <td>${contact}</td>
                <td><span class="badge ${role}">${role}</span></td>
                <td class="actions-cell">${actionButtons}</td>
            </tr>`;
        });
        tbody.innerHTML = rowsHTML;
    }

    function filterStaffTable() {
        var query = document.getElementById('searchStaffInput').value.toLowerCase().trim();
        if (!query) {
            renderStaffTableData(cachedStaff);
            return;
        }
        var filtered = cachedStaff.filter(s => {
            var name = (s.staffName || s.staff_name || '').toLowerCase();
            var username = (s.username || '').toLowerCase();
            var role = (s.role || '').toLowerCase();
            return name.includes(query) || username.includes(query) || role.includes(query);
        });
        renderStaffTableData(filtered);
    }

    function openStaffModal() {
        document.getElementById('staffForm').reset();
        document.getElementById('editStaffId').value = '';
        document.getElementById('staffFormTitle').textContent = 'Add New Staff';
        document.getElementById('saveStaffBtn').textContent = 'Save Staff';
        document.getElementById('staffTableCard').style.display = 'none';
        document.getElementById('staffFormCard').style.display = 'block';
        document.getElementById('staffResultMsg').style.display = 'none';
        resetPasswordVisibility('stPassword', 'stPassword-toggle');
        window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
    }

    function closeStaffModal() {
        document.getElementById('staffFormCard').style.display = 'none';
        document.getElementById('staffTableCard').style.display = 'block';
        resetPasswordVisibility('stPassword', 'stPassword-toggle');
    }

    function editStaff(id) {
        var staff = cachedStaff.find(s => (s.staffId || s.staff_id) === id);
        if (!staff) return;

        document.getElementById('editStaffId').value = staff.staffId || staff.staff_id;
        document.getElementById('stName').value = staff.staffName || staff.staff_name || '';
        document.getElementById('stUsername').value = staff.username || '';
        document.getElementById('stPassword').value = '';
        document.getElementById('stContact').value = staff.contactNo || staff.contact_no || '';
        document.getElementById('stRole').value = (staff.role || 'STAFF').toUpperCase();

        document.getElementById('staffFormTitle').textContent = 'Edit Staff Member';
        document.getElementById('saveStaffBtn').textContent = 'Update Staff';
        document.getElementById('staffTableCard').style.display = 'none';
        document.getElementById('staffFormCard').style.display = 'block';
        document.getElementById('staffResultMsg').style.display = 'none';
        resetPasswordVisibility('stPassword', 'stPassword-toggle');
        window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
    }

    function saveStaff() {
        var id = document.getElementById('editStaffId').value;
        var resultMsg = document.getElementById('staffResultMsg');

        var payload = {
            staffName: document.getElementById('stName').value.trim(),
            username: document.getElementById('stUsername').value.trim(),
            password: document.getElementById('stPassword').value.trim(),
            contactNo: document.getElementById('stContact').value.trim(),
            role: document.getElementById('stRole').value
        };

        var url = id ? (BASE_URL + '/staff/' + id) : BASE_URL + '/staff';
        url += (url.indexOf('?') === -1 ? '?' : '&') + 'actingUsername=' + encodeURIComponent(currentStaffUsername);
        var method = id ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            headers: { 
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        })
        .then(res => res.ok ? res.json() : res.text().then(t => { throw new Error(t); }))
        .then(data => {
            resultMsg.className = 'success'; resultMsg.style.display = 'block';
            resultMsg.innerHTML = id ? '✅ Staff member updated successfully!' : '✅ Staff member added successfully!';
            loadStaffTable();
            setTimeout(() => {
                closeStaffModal();
                resultMsg.style.display = 'none';
            }, 1200);
        })
        .catch(err => {
            resultMsg.className = 'error'; resultMsg.style.display = 'block';
            resultMsg.innerHTML = 'Operation failed: ' + err.message;
        });
    }

    function deleteStaff(id) {
        if (!confirm("Are you sure you want to delete this staff member?")) return;

        fetch(BASE_URL + '/staff/' + id + '?actingUsername=' + encodeURIComponent(currentStaffUsername), { 
            method: 'DELETE'
        })
        .then(res => {
            if (res.ok) {
                loadStaffTable();
            } else {
                res.text().then(t => alert('Failed to delete staff member: ' + t));
            }
        })
        .catch(err => console.error("Error deleting staff:", err));
    }

    function loadStaffProfileIntoForm() {
        var msg = document.getElementById('profileResultMsg');
        if (msg) msg.style.display = 'none';

        fetch(BASE_URL + '/staff')
            .then(res => res.json())
            .then(data => {
                var me = (data || []).find(s => (s.username || '').toLowerCase() === (currentStaffUsername || '').toLowerCase());
                if (!me) return;

                document.getElementById('pf-staffId').value = me.staffId || me.staff_id || '-';
                document.getElementById('pf-role').value = (me.role || currentUserRole || 'STAFF').toUpperCase();
                document.getElementById('pf-username').value = me.username || currentStaffUsername || '';
                document.getElementById('pf-name').value = me.staffName || me.staff_name || '';
                document.getElementById('pf-contact').value = me.contactNo || me.contact_no || '';
                document.getElementById('pf-password').value = '';
            })
            .catch(err => console.error('Failed to load profile data:', err));
    }

    function updateStaffProfile() {
        var msg = document.getElementById('profileResultMsg');
        var id = document.getElementById('pf-staffId').value;
        if (!id || id === '-') {
            msg.className = 'error'; msg.style.display = 'block';
            msg.textContent = 'Could not identify your staff account. Please log out and log in again.';
            return;
        }

        var name = document.getElementById('pf-name').value.trim();
        var payload = {
            staffName: name,
            username: document.getElementById('pf-username').value.trim(),
            password: document.getElementById('pf-password').value.trim(),
            contactNo: document.getElementById('pf-contact').value.trim(),
            role: document.getElementById('pf-role').value.trim()
        };

        fetch(BASE_URL + '/staff/' + id + '?actingUsername=' + encodeURIComponent(currentStaffUsername), {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.ok ? res.json() : res.text().then(t => { throw new Error(t); }))
        .then(() => {
            msg.className = 'success'; msg.style.display = 'block';
            msg.textContent = '✅ Profile updated successfully!';
            if (name) {
                document.getElementById('staffName').textContent = name;
            }
            document.getElementById('pf-password').value = '';
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        })
        .catch(err => {
            msg.className = 'error'; msg.style.display = 'block';
            msg.textContent = 'Failed to update profile. ' + err.message;
        });
    }

    function resetPasswordVisibility(inputId, btnId) {
        var input = document.getElementById(inputId);
        var btn = document.getElementById(btnId);
        if (input) input.type = 'password';
        if (btn) btn.textContent = 'Show';
    }

    function toggleStaffPasswordVisibility(inputId, btnId) {
        inputId = inputId || 'pf-password';
        btnId = btnId || 'pf-password-toggle';
        var input = document.getElementById(inputId);
        var btn = document.getElementById(btnId);
        if (!input || !btn) return;
        if (input.type === 'password') {
            input.type = 'text';
            btn.textContent = 'Hide';
        } else {
            input.type = 'password';
            btn.textContent = 'Show';
        }
    }

    function loadNoticeDentistDropdown() {
        var select = document.getElementById('noticeDentistId');
        if (!select) return;

        select.innerHTML = '<option value="">-- Select Dentist --</option>';

        fetch(BASE_URL + '/dentists')
            .then(res => res.json())
            .then(data => {
                (data || []).forEach(d => {
                    var opt = document.createElement('option');
                    opt.value = d.dentistId || d.dentist_id;
                    var dName = d.dentistName || d.dentist_name;
                    opt.textContent = `${dName} (${d.specialization || ''})`;
                    select.appendChild(opt);
                });
            })
            .catch(err => console.error('Error loading dentists for notice:', err));
    }

    function sendDentistNotice() {
        var msg = document.getElementById('noticeResultMsg');
        var dentistId = document.getElementById('noticeDentistId').value;
        var description = document.getElementById('noticeDescription').value.trim();

        if (!dentistId || !description) {
            msg.className = 'error'; msg.style.display = 'block';
            msg.textContent = 'Please select a dentist and enter a notice description.';
            return;
        }

        var payload = {
            dentistId: dentistId,
            description: description,
            sentBy: currentStaffUsername
        };

        var btn = document.getElementById('sendNoticeBtn');
        if (btn) { btn.disabled = true; btn.textContent = 'Sending...'; }

        fetch(BASE_URL + '/notice', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.ok ? res.json() : res.text().then(t => { throw new Error(t); }))
        .then(() => {
            msg.className = 'success'; msg.style.display = 'block';
            msg.textContent = '✅ Notice sent successfully to the selected dentist!';
            document.getElementById('noticeForm').reset();
            setTimeout(() => { msg.style.display = 'none'; }, 4000);
        })
        .catch(err => {
            msg.className = 'error'; msg.style.display = 'block';
            msg.textContent = 'Failed to send notice. ' + err.message;
        })
        .finally(() => {
            if (btn) { btn.disabled = false; btn.textContent = 'Send Notice'; }
        });
    }

    function editAppointment(appointmentNo) {
        var apt = cachedAppointments.find(a => (a.appointmentNo || a.appointment_no) === appointmentNo);
        if (!apt) return;
        var status = (apt.status || 'PENDING').toUpperCase();
        if (status !== 'PENDING') return;

        document.getElementById('editAppointmentNo').value = appointmentNo;
        document.getElementById('patientName').value = apt.patientName || apt.patient_name || '';
        document.getElementById('address').value = apt.address || '';
        document.getElementById('contactNo').value = apt.contactNo || apt.contact_no || '';
        hideSuggestionsBox();
        hideExistingPatientTag();
        document.getElementById('appointmentDate').value = apt.appointmentDate || apt.appointment_date || '';

        var rawTime = apt.appointmentTime || apt.appointment_time || '';
        setAppointmentTimeUI(rawTime.length >= 5 ? rawTime.substring(0, 5) : rawTime);

        setTimeout(() => {
            var dentistId = apt.dentistId || apt.dentist_id;
            var treatmentId = apt.treatmentId || apt.treatment_id;
            if (dentistId) document.getElementById('dentistId').value = dentistId;
            if (treatmentId) document.getElementById('treatmentId').value = treatmentId;
        }, 400);

        document.getElementById('appointmentFormTitle').textContent = 'Edit Appointment ' + appointmentNo;
        document.getElementById('appointmentFormSub').textContent = 'Update the appointment details below';
        document.getElementById('saveAppointmentBtn').textContent = 'Update Appointment';

        isEditingAppointment = true;
        showSection('sec-register');

        document.querySelectorAll('.nav-item[data-target]').forEach(n => n.classList.remove('active'));
        var appointmentsNav = document.querySelector('.nav-item[data-target="sec-appointments"]');
        if (appointmentsNav) appointmentsNav.classList.add('active');
    }

    function cancelAppointmentForm() {
        document.getElementById('appointmentForm').reset();
        resetAppointmentTimeUI();
        document.getElementById('editAppointmentNo').value = '';
        resetAppointmentFormLabels();
        hideSuggestionsBox();
        hideExistingPatientTag();
        showSection('sec-appointments');
    }

    function resetAppointmentFormLabels() {
        document.getElementById('appointmentFormTitle').textContent = 'Register New Appointment';
        document.getElementById('appointmentFormSub').textContent = 'Enter patient and appointment details to book a new slot';
        document.getElementById('saveAppointmentBtn').textContent = 'Submit & Save Appointment';
    }

    function deleteAppointment(appointmentNo) {
        if (!confirm('Are you sure you want to delete appointment ' + appointmentNo + '?')) return;

        fetch(BASE_URL + '/appointment/' + appointmentNo + '?actingUsername=' + encodeURIComponent(currentStaffUsername), { method: 'DELETE' })
            .then(res => {
                if (res.ok) {
                    loadAppointments();
                } else {
                    res.text().then(t => alert('Failed to delete appointment: ' + t));
                }
            })
            .catch(err => console.error("Error deleting appointment:", err));
    }

    function pad2(n) { return n < 10 ? '0' + n : '' + n; }

    function onApptTimeInput() {
        document.getElementById('appointmentTime').style.borderColor = '';
    }

    function onApptTimeBlur() {
        var input = document.getElementById('appointmentTime');
        var val = input.value;
        if (!val) { input.style.borderColor = ''; return; }
        input.style.borderColor = (val < '08:00' || val > '17:00') ? '#d33' : '';
    }

    function setAppointmentTimeUI(rawTime) {
        if (!rawTime) { resetAppointmentTimeUI(); return; }
        var parts = rawTime.split(':');
        var hhmm = pad2(parseInt(parts[0], 10)) + ':' + pad2(parseInt(parts[1] || '0', 10));
        var input = document.getElementById('appointmentTime');
        input.value = hhmm;
        input.style.borderColor = '';
    }

    function resetAppointmentTimeUI() {
        var input = document.getElementById('appointmentTime');
        input.value = '';
        input.style.borderColor = '';
    }

    function registerAppointment() {
        var resultMsg = document.getElementById('resultMsg');
        var timeInput = document.getElementById('appointmentTime');
        var rawTime = timeInput.value;

        if (!rawTime) {
            resultMsg.className = 'error';
            resultMsg.style.display = 'block';
            resultMsg.innerHTML = '⚠️ Please enter a valid appointment time (e.g: 08:30 AM).';
            timeInput.style.borderColor = '#d33';
            return;
        }

        if (rawTime < '08:00' || rawTime > '17:00') {
            resultMsg.className = 'error';
            resultMsg.style.display = 'block';
            resultMsg.innerHTML = '⚠️ Appointment time must be between 8.00 AM and 5.00 PM.';
            timeInput.style.borderColor = '#d33';
            return;
        }

        timeInput.style.borderColor = '';

        var formattedTime = rawTime.length === 5 ? rawTime + ':00' : rawTime;
        var editNo = document.getElementById('editAppointmentNo').value;

        var payload = {
            patientName: document.getElementById('patientName').value.trim(),
            address: document.getElementById('address').value.trim(),
            contactNo: document.getElementById('contactNo').value.trim(),
            dentistId: parseInt(document.getElementById('dentistId').value, 10),
            treatmentId: parseInt(document.getElementById('treatmentId').value, 10),
            appointmentDate: document.getElementById('appointmentDate').value,
            appointmentTime: formattedTime,
            bookedByUsername: currentStaffUsername && currentStaffUsername.trim() !== '' ? currentStaffUsername.trim() : null
        };

        var url = editNo ? (BASE_URL + '/appointment/' + editNo) : BASE_URL + '/appointment';
        var method = editNo ? 'PUT' : 'POST';
        var expectedOkStatus = editNo ? 200 : 201;

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.status === expectedOkStatus ? res.json() : res.text().then(t => { throw new Error(t); }))
        .then(data => {
            resultMsg.className = 'success'; resultMsg.style.display = 'block';
            resultMsg.innerHTML = editNo
                ? '✅ Appointment <b>' + editNo + '</b> updated successfully!'
                : '✅ Appointment registered successfully! Assigned Number: <b>' + (data.appointmentNo || data.appointment_no) + '</b>';
            document.getElementById('appointmentForm').reset();
            resetAppointmentTimeUI();
            document.getElementById('editAppointmentNo').value = '';
            hideSuggestionsBox();
            hideExistingPatientTag();
            loadAppointments();
            loadPatientsForAutocomplete();
            setTimeout(() => {
                showSection('sec-appointments');
                resultMsg.style.display = 'none';
                resetAppointmentFormLabels();
            }, 1500);
        })
        .catch(err => {
            resultMsg.className = 'error'; resultMsg.style.display = 'block';
            resultMsg.innerHTML = 'Failed to register appointment. ' + err.message;
        });
    }

    function loadPatientsForAutocomplete() {
        fetch(BASE_URL + '/patients')
            .then(res => res.json())
            .then(data => { cachedPatients = data || []; })
            .catch(err => console.error("Error preloading patients for autocomplete:", err));
    }

    function getPatientInitial(name) {
        return (name || '?').trim().charAt(0).toUpperCase() || '?';
    }

    function onPatientNameInput() {
        var input = document.getElementById('patientName');
        var box = document.getElementById('patientSuggestions');
        if (!input || !box) return;

        hideExistingPatientTag();

        var query = input.value.trim().toLowerCase();
        if (!query) { box.classList.remove('show'); box.innerHTML = ''; return; }

        var matches = (cachedPatients || []).filter(p => {
            var name = (p.patientName || p.patient_name || '').toLowerCase();
            return name.indexOf(query) !== -1;
        }).slice(0, 6);

        if (matches.length === 0) {
            box.innerHTML = '<div class="patient-suggestions-empty">No matching registered patient — this will be added as a new patient.</div>';
            box.classList.add('show');
            return;
        }

        box.innerHTML = matches.map(p => {
            var id = p.patientId || p.patient_id;
            var name = p.patientName || p.patient_name || '-';
            var contact = p.contactNo || p.contact_no || '';
            var address = p.address || '';
            var meta = [contact, address].filter(Boolean).join(' • ');
            return '<div class="patient-suggestion-item" onmousedown="event.preventDefault(); selectPatientSuggestion(' + id + ')">'
                + '<div class="patient-suggestion-avatar">' + getPatientInitial(name) + '</div>'
                + '<div class="patient-suggestion-info">'
                +   '<div class="patient-suggestion-name">' + name + '</div>'
                +   (meta ? '<div class="patient-suggestion-meta">' + meta + '</div>' : '')
                + '</div>'
                + '</div>';
        }).join('');
        box.classList.add('show');
    }

    function selectPatientSuggestion(patientId) {
        var patient = (cachedPatients || []).find(p => (p.patientId || p.patient_id) === patientId);
        if (!patient) return;

        document.getElementById('patientName').value = patient.patientName || patient.patient_name || '';
        document.getElementById('address').value = patient.address || '';
        document.getElementById('contactNo').value = patient.contactNo || patient.contact_no || '';

        hideSuggestionsBox();
        showExistingPatientTag();
    }

    function onPatientNameBlur() {
        // Small delay so a click on a suggestion registers before the list is hidden.
        setTimeout(hideSuggestionsBox, 150);
    }

    function hideSuggestionsBox() {
        var box = document.getElementById('patientSuggestions');
        if (box) { box.classList.remove('show'); box.innerHTML = ''; }
    }

    function showExistingPatientTag() {
        var tag = document.getElementById('existingPatientTag');
        if (!tag) return;
        tag.innerHTML = '✓ Existing patient details auto-filled';
        tag.classList.add('show');
    }

    function hideExistingPatientTag() {
        var tag = document.getElementById('existingPatientTag');
        if (tag) { tag.classList.remove('show'); tag.innerHTML = ''; }
    }

    function loadBillableAppointments() {
        var tbody = document.getElementById('billAppointmentsTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; padding:24px; color:#9aa0ab;">Loading completed appointments...</td></tr>';

        fetch(BASE_URL + '/appointment')
            .then(res => res.json())
            .then(data => {
                cachedBillableAppointments = (data || [])
                    .filter(a => (a.status || '').toUpperCase() === 'COMPLETED')
                    .sort((a, b) => {
                        var aNo = (a.appointmentNo || a.appointment_no || '').toString();
                        var bNo = (b.appointmentNo || b.appointment_no || '').toString();
                        var aNum = parseInt(aNo.replace(/[^0-9]/g, ''), 10) || 0;
                        var bNum = parseInt(bNo.replace(/[^0-9]/g, ''), 10) || 0;
                        return aNum - bNum;
                    });
                renderBillTableData(cachedBillableAppointments);
            })
            .catch(err => {
                console.error("Error fetching completed appointments:", err);
                if (tbody) tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; color:#d33; padding:24px;">Failed to load data</td></tr>';
            });
    }

    function renderBillTableData(data) {
        var tbody = document.getElementById('billAppointmentsTableBody');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; padding:24px; color:#9aa0ab;">No completed appointments found.</td></tr>';
            return;
        }

        var rowsHTML = '';
        data.forEach(a => {
            var status = (a.status || 'COMPLETED').toUpperCase();
            var rawDentist = a.dentistName || a.dentist_name || '';
            var dentist = rawDentist ? (rawDentist.startsWith('Dr.') ? rawDentist : 'Dr. ' + rawDentist) : '-';
            var aptNo = a.appointmentNo || a.appointment_no || '-';
            var rawTime = a.appointmentTime || a.appointment_time || '';
            var formattedTime = formatTimeTo12Hour(rawTime);

            rowsHTML += `<tr>
                <td><b>${aptNo}</b></td>
                <td>${a.patientName || a.patient_name || '-'}</td>
                <td>${a.contactNo || a.contact_no || '-'}</td>
                <td>${dentist}</td>
                <td>${a.treatmentName || a.treatment_name || '-'}</td>
                <td>${a.appointmentDate || a.appointment_date || '-'}</td>
                <td>${formattedTime}</td>
                <td><span class="badge ${status}">${status}</span></td>
                <td>
                    <button class="btn-action btn-edit" onclick="calculateBill('${aptNo}')">Calculate Bill</button>
                </td>
            </tr>`;
        });
        tbody.innerHTML = rowsHTML;
    }

    function filterBillTable() {
        var query = document.getElementById('billSearchInput').value.toLowerCase().trim();
        if (!query) {
            renderBillTableData(cachedBillableAppointments);
            return;
        }
        var filtered = cachedBillableAppointments.filter(a => {
            var aptNo = (a.appointmentNo || a.appointment_no || '').toLowerCase();
            var pName = (a.patientName || a.patient_name || '').toLowerCase();
            var contact = (a.contactNo || a.contact_no || '').toLowerCase();
            return aptNo.includes(query) || pName.includes(query) || contact.includes(query);
        });
        renderBillTableData(filtered);
    }

    function calculateBill(aptNo) {
        var msg = document.getElementById('billResultMsg');
        var box = document.getElementById('receiptBox');
        var tableWrapper = document.getElementById('billTableWrapper');
        box.style.display = 'none'; msg.style.display = 'none';
        if (!aptNo) return;

        fetch(BASE_URL + '/bill/' + encodeURIComponent(aptNo), { method: 'POST' })
            .then(res => res.status === 200 ? res.json() : res.text().then(t => { throw new Error(t); }))
            .then(bill => {
                populateReceiptData(bill);
            })
            .catch(err => { 
                var cachedItem = cachedAppointments.concat(cachedBillableAppointments).find(a => (a.appointmentNo || a.appointment_no) === aptNo);
                if (cachedItem) {
                    var dFee = 0, tCost = 0;
                    var dentist = cachedDentists.find(d => (d.dentistId || d.dentist_id) == (cachedItem.dentistId || cachedItem.dentist_id));
                    var treatment = cachedTreatments.find(t => (t.treatmentId || t.treatment_id) == (cachedItem.treatmentId || cachedItem.treatment_id));
                    
                    if (dentist) dFee = dentist.consultationFee !== undefined ? dentist.consultationFee : (dentist.consultation_fee || 0);
                    if (treatment) tCost = treatment.treatmentCost !== undefined ? treatment.treatmentCost : (treatment.treatment_cost || 0);

                    populateReceiptData({
                        appointmentNo: aptNo,
                        patientName: cachedItem.patientName || cachedItem.patient_name,
                        contactNo: cachedItem.contactNo || cachedItem.contact_no,
                        dentistName: cachedItem.dentistName || cachedItem.dentist_name,
                        treatmentName: cachedItem.treatmentName || cachedItem.treatment_name,
                        appointmentDate: cachedItem.appointmentDate || cachedItem.appointment_date,
                        appointmentTime: cachedItem.appointmentTime || cachedItem.appointment_time,
                        treatmentCost: tCost,
                        consultationFee: dFee,
                        totalAmount: parseFloat(tCost) + parseFloat(dFee)
                    });
                } else {
                    msg.className = 'error'; msg.style.display = 'block'; msg.textContent = 'Error: ' + err.message;
                }
            });
    }

    function populateReceiptData(bill) {
        var box = document.getElementById('receiptBox');
        var tableWrapper = document.getElementById('billTableWrapper');
        
        var rawTime = bill.appointmentTime || bill.appointment_time || '';
        var formattedTime = formatTimeTo12Hour(rawTime);

        document.getElementById('r-apptNo').textContent = bill.appointmentNo || bill.appointment_no || '-';
        document.getElementById('r-datetime').textContent = (bill.appointmentDate || bill.appointment_date || '') + ' ' + formattedTime;
        document.getElementById('r-patient').textContent = bill.patientName || bill.patient_name || '-';
        document.getElementById('r-contact').textContent = bill.contactNo || bill.contact_no || '-';
        document.getElementById('r-dentist').textContent = bill.dentistName || bill.dentist_name || '-';
        document.getElementById('r-treatment').textContent = bill.treatmentName || bill.treatment_name || '-';
        
        var tCost = bill.treatmentCost !== undefined ? bill.treatmentCost : bill.treatment_cost;
        var cFee = bill.consultationFee !== undefined ? bill.consultationFee : bill.consultation_fee;
        var total = bill.totalAmount !== undefined ? bill.totalAmount : bill.total_amount;

        document.getElementById('r-treatmentCost').textContent = 'Rs. ' + Number(tCost || 0).toFixed(2);
        document.getElementById('r-consultationFee').textContent = 'Rs. ' + Number(cFee || 0).toFixed(2);
        document.getElementById('r-total').textContent = 'Rs. ' + Number(total || 0).toFixed(2);
        
        if (tableWrapper) tableWrapper.style.display = 'none';
        box.style.display = 'block';
        box.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    function backToBillList() {
        document.getElementById('receiptBox').style.display = 'none';
        document.getElementById('billResultMsg').style.display = 'none';
        var tableWrapper = document.getElementById('billTableWrapper');
        if (tableWrapper) tableWrapper.style.display = '';
    }

    window.addEventListener('afterprint', function () {
        var box = document.getElementById('receiptBox');
        if (box && box.style.display !== 'none') {
            backToBillList();
        }
    });