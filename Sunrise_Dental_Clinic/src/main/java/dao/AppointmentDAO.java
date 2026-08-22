package dao;

import util.DBConnection;
import model.Appointment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    // 1. Staff Dashboard (සියලුම Appointments)
    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.patient_name, p.contact_no, p.address, d.dentist_name, t.treatment_name " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                     "JOIN treatment_types t ON a.treatment_id = t.treatment_id " +
                     "ORDER BY a.appointment_date DESC, a.appointment_time DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Appointment apt = mapResultSetToAppointment(rs);
                list.add(apt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Dentist Dashboard (Dentist ID එකට අදාළ Appointments)
    public List<Appointment> getAppointmentsByDentistId(int dentistId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.patient_name, p.contact_no, p.address, d.dentist_name, t.treatment_name " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                     "JOIN treatment_types t ON a.treatment_id = t.treatment_id " +
                     "WHERE a.dentist_id = ? " +
                     "ORDER BY a.appointment_date DESC, a.appointment_time DESC";

        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment apt = mapResultSetToAppointment(rs);
                    list.add(apt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. Appointment Number එකෙන් Search කිරීම
    public Appointment getAppointmentByNo(String appointmentNo) {
        String sql = "SELECT a.*, p.patient_name, p.contact_no, p.address, d.dentist_name, t.treatment_name " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                     "JOIN treatment_types t ON a.treatment_id = t.treatment_id " +
                     "WHERE a.appointment_no = ?";

        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Appointment apt = mapResultSetToAppointment(rs);
                    return apt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 4. Billing Details එකතු කරගත් Appointment එකක් ගැනීම
    public Appointment getAppointmentWithBillingDetails(String appointmentNo) {
        String sql = "SELECT a.*, p.patient_name, p.contact_no, p.address, d.dentist_name, d.consultation_fee, " +
                     "t.treatment_name, t.treatment_cost " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                     "JOIN treatment_types t ON a.treatment_id = t.treatment_id " +
                     "WHERE a.appointment_no = ?";

        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Appointment apt = mapResultSetToAppointment(rs);
                    apt.setConsultationFee(rs.getDouble("consultation_fee"));
                    apt.setTreatmentCost(rs.getDouble("treatment_cost"));
                    return apt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 5. Auto Generate Appointment Number
    //    Uses the highest existing appointment_no + 1 (not COUNT(*)), so that
    //    deleting appointments never causes a duplicate/collided number to be
    //    generated for a new appointment.
    public String generateNextAppointmentNo() {
        String sql = "SELECT appointment_no FROM appointments " +
                      "ORDER BY CAST(SUBSTRING(appointment_no, 4) AS UNSIGNED) DESC LIMIT 1";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String lastNo = rs.getString(1); // e.g. "APT0012"
                int nextNum = Integer.parseInt(lastNo.substring(3)) + 1;
                return String.format("APT%04d", nextNum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "APT0001";
    }

    // 6. Appointment එක Save කිරීම
    public boolean registerAppointment(Appointment apt) {
        String sql = "INSERT INTO appointments (appointment_no, patient_id, dentist_id, treatment_id, " +
                     "booked_by_username, appointment_date, appointment_time, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, apt.getAppointmentNo());
            ps.setInt(2, apt.getPatientId());
            ps.setInt(3, apt.getDentistId());
            ps.setInt(4, apt.getTreatmentId());
            ps.setString(5, apt.getBookedByUsername());
            ps.setDate(6, java.sql.Date.valueOf(apt.getAppointmentDate()));
            ps.setTime(7, java.sql.Time.valueOf(apt.getAppointmentTime()));
            ps.setString(8, apt.getStatus());

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 7. Update Appointment Status (used by Dentist Dashboard Accept / Cancel buttons)
    public boolean updateStatus(String appointmentNo, String newStatus) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_no = ?";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, appointmentNo);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 8. Full Update of an Appointment (used by Staff Dashboard Edit button)
    //    Updates the linked patient's name/contact along with the appointment's
    //    dentist, treatment, date and time.
    public boolean updateAppointment(String appointmentNo, String patientName, String contactNo,
                                      String address, int dentistId, int treatmentId,
                                      String appointmentDate, String appointmentTime) {

        String selectPatientSql = "SELECT patient_id FROM appointments WHERE appointment_no = ?";
        String updatePatientSql = "UPDATE patients SET patient_name = ?, contact_no = ?, address = ? WHERE patient_id = ?";
        String updateAptSql = "UPDATE appointments SET dentist_id = ?, treatment_id = ?, " +
                               "appointment_date = ?, appointment_time = ? WHERE appointment_no = ?";

        Connection conn = DBConnection.getInstance().getConnection();

        try {
            int patientId = -1;
            try (PreparedStatement ps = conn.prepareStatement(selectPatientSql)) {
                ps.setString(1, appointmentNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        patientId = rs.getInt("patient_id");
                    } else {
                        return false; // appointment doesn't exist
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updatePatientSql)) {
                ps.setString(1, patientName);
                ps.setString(2, contactNo);
                ps.setString(3, address);
                ps.setInt(4, patientId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(updateAptSql)) {
                ps.setInt(1, dentistId);
                ps.setInt(2, treatmentId);
                ps.setDate(3, java.sql.Date.valueOf(appointmentDate));
                ps.setTime(4, java.sql.Time.valueOf(appointmentTime));
                ps.setString(5, appointmentNo);

                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 9. Delete Appointment (used by Staff Dashboard Delete button)
    //    Removes any bill tied to the appointment first to satisfy the FK constraint.
    public boolean deleteAppointment(String appointmentNo) {
        String deleteBillSql = "DELETE FROM bills WHERE appointment_no = ?";
        String deleteAptSql = "DELETE FROM appointments WHERE appointment_no = ?";

        Connection conn = DBConnection.getInstance().getConnection();

        try {
            try (PreparedStatement ps = conn.prepareStatement(deleteBillSql)) {
                ps.setString(1, appointmentNo);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(deleteAptSql)) {
                ps.setString(1, appointmentNo);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Safe Data Mapping (No appointment_id dependency)
    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment apt = new Appointment();
        apt.setAppointmentNo(rs.getString("appointment_no"));
        apt.setPatientId(rs.getInt("patient_id"));
        apt.setPatientName(rs.getString("patient_name"));
        apt.setContactNo(rs.getString("contact_no"));
        apt.setAddress(rs.getString("address"));
        apt.setDentistId(rs.getInt("dentist_id"));
        apt.setDentistName(rs.getString("dentist_name"));
        apt.setTreatmentId(rs.getInt("treatment_id"));
        apt.setTreatmentName(rs.getString("treatment_name"));

        Date date = rs.getDate("appointment_date");
        if (date != null) apt.setAppointmentDate(date.toString());

        Time time = rs.getTime("appointment_time");
        if (time != null) apt.setAppointmentTime(time.toString());

        apt.setStatus(rs.getString("status"));
        return apt;
    }
}