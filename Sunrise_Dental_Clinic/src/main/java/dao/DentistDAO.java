package dao;

import model.Dentist;
import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DentistDAO {

    public Dentist validateLogin(String username, String password) {
        String sql = "SELECT * FROM dentists WHERE username = ? AND password = ?";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Dentist(
                            rs.getInt("dentist_id"),
                            rs.getString("dentist_name"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("specialization"),
                            rs.getString("contact_no"),
                            rs.getDouble("consultation_fee")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in validateLogin: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public int addDentist(Dentist d) {
        String sql = "INSERT INTO dentists (dentist_name, username, password, specialization, contact_no, consultation_fee) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getDentistName());
            ps.setString(2, d.getUsername());
            ps.setString(3, d.getPassword());
            ps.setString(4, d.getSpecialization());
            ps.setString(5, d.getContactNo());
            ps.setDouble(6, d.getConsultationFee());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in addDentist: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateDentist(Dentist d) {
        boolean hasUsername = d.getUsername() != null && !d.getUsername().trim().isEmpty();
        boolean hasPassword = d.getPassword() != null && !d.getPassword().trim().isEmpty();

        StringBuilder sql = new StringBuilder(
                "UPDATE dentists SET dentist_name = ?, contact_no = ?, "
                        + "specialization = ?, consultation_fee = ?");
        if (hasUsername) sql.append(", username = ?");
        if (hasPassword) sql.append(", password = ?");
        sql.append(" WHERE dentist_id = ?");

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, d.getDentistName());
            ps.setString(idx++, d.getContactNo());
            ps.setString(idx++, d.getSpecialization());
            ps.setDouble(idx++, d.getConsultationFee());
            if (hasUsername) ps.setString(idx++, d.getUsername());
            if (hasPassword) ps.setString(idx++, d.getPassword());
            ps.setInt(idx, d.getDentistId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in updateDentist: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Real (hard) delete - DB schema eka vෙනස් karanne naa (is_active naa).
    // "appointments" table eke dentist_id FK eka thiyena nisa, dentist ekata
    // link una appointments/bills welinma delete karala, aparade dentist
    // record eka delete karanawa. Meka transaction ekak widihata karanne,
    // step ekak fail una data half-deleted wenne naathi wenna (rollback).
    public boolean deleteDentist(int dentistId) {
        Connection conn = DBConnection.getInstance().getConnection();
        String deleteBillsSql =
                "DELETE b FROM bills b " +
                "JOIN appointments a ON b.appointment_no = a.appointment_no " +
                "WHERE a.dentist_id = ?";
        String deleteAppointmentsSql = "DELETE FROM appointments WHERE dentist_id = ?";
        String deleteDentistSql = "DELETE FROM dentists WHERE dentist_id = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement psBills = conn.prepareStatement(deleteBillsSql)) {
                psBills.setInt(1, dentistId);
                psBills.executeUpdate();
            }

            try (PreparedStatement psAppointments = conn.prepareStatement(deleteAppointmentsSql)) {
                psAppointments.setInt(1, dentistId);
                psAppointments.executeUpdate();
            }

            int rowsAffected;
            try (PreparedStatement psDentist = conn.prepareStatement(deleteDentistSql)) {
                psDentist.setInt(1, dentistId);
                rowsAffected = psDentist.executeUpdate();
            }

            conn.commit();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting dentist (cascade): " + e.getMessage());
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException resetEx) {
                System.err.println("Failed to reset auto-commit: " + resetEx.getMessage());
            }
        }
    }

    public boolean updateDentistProfile(
            int dentistId, String name, String contactNo, 
            String specialization, double fee, String password) {
        String sql = "UPDATE dentists SET dentist_name = ?, contact_no = ?, specialization = ?,"
                   + " consultation_fee = ?, password = ? WHERE dentist_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, contactNo);
            ps.setString(3, specialization);
            ps.setDouble(4, fee);
            ps.setString(5, password);
            ps.setInt(6, dentistId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in updateDentistProfile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}