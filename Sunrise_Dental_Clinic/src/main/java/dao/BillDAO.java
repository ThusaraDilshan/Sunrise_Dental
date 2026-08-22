/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Bill;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BillDAO {

    /**
     * Checks if a bill already exists for this appointment (avoids duplicate billing).
     */
    public Bill getBillByAppointmentNo(String appointmentNo) {
        String sql = "SELECT b.*, "
                + "p.patient_name, p.contact_no, p.address, "
                + "d.dentist_name, "
                + "t.treatment_name, "
                + "a.appointment_date, a.appointment_time "
                + "FROM bills b "
                + "JOIN appointments a ON b.appointment_no = a.appointment_no "
                + "JOIN patients p ON a.patient_id = p.patient_id "
                + "JOIN dentists d ON a.dentist_id = d.dentist_id "
                + "JOIN treatment_types t ON a.treatment_id = t.treatment_id "
                + "WHERE b.appointment_no = ?";

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBill(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Requirement 4: Calculate and Print Bill.
     *
     * Pulls the treatment cost (from treatment_types) and the dentist's
     * consultation fee (from dentists) for the given appointment, computes
     * the total, saves it as a new bill row, and returns the full receipt data.
     *
     * If a bill already exists for this appointment, returns the existing one
     * instead of creating a duplicate.
     */
    public Bill calculateAndSaveBill(String appointmentNo) {

        // 1. Avoid duplicate billing
        Bill existing = getBillByAppointmentNo(appointmentNo);
        if (existing != null) {
            return existing;
        }

        // 2. Pull treatment cost + dentist consultation fee for this appointment
        String lookupSql = "SELECT t.treatment_cost, d.consultation_fee "
                + "FROM appointments a "
                + "JOIN dentists d ON a.dentist_id = d.dentist_id "
                + "JOIN treatment_types t ON a.treatment_id = t.treatment_id "
                + "WHERE a.appointment_no = ?";

        Connection conn = DBConnection.getInstance().getConnection();
        BigDecimal treatmentCost;
        BigDecimal consultationFee;

        try (PreparedStatement ps = conn.prepareStatement(lookupSql)) {
            ps.setString(1, appointmentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null; // appointment not found
                }
                treatmentCost = rs.getBigDecimal("treatment_cost");
                consultationFee = rs.getBigDecimal("consultation_fee");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        BigDecimal total = treatmentCost.add(consultationFee);

        // 3. Save the new bill
        String insertSql = "INSERT INTO bills (appointment_no, treatment_cost, consultation_fee, total_amount) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, appointmentNo);
            ps.setBigDecimal(2, treatmentCost);
            ps.setBigDecimal(3, consultationFee);
            ps.setBigDecimal(4, total);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        // 4. Return the full receipt (re-fetch with joined display fields)
        return getBillByAppointmentNo(appointmentNo);
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));
        bill.setAppointmentNo(rs.getString("appointment_no"));
        bill.setTreatmentCost(rs.getBigDecimal("treatment_cost"));
        bill.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        bill.setTotalAmount(rs.getBigDecimal("total_amount"));
        bill.setBillDate(String.valueOf(rs.getTimestamp("bill_date")));

        bill.setPatientName(rs.getString("patient_name"));
        bill.setContactNo(rs.getString("contact_no"));
        bill.setAddress(rs.getString("address"));
        bill.setDentistName(rs.getString("dentist_name"));
        bill.setTreatmentName(rs.getString("treatment_name"));
        bill.setAppointmentDate(String.valueOf(rs.getDate("appointment_date")));
        bill.setAppointmentTime(String.valueOf(rs.getTime("appointment_time")));

        return bill;
    }
}