package dao;

import model.TreatmentType;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TreatmentDAO {

    public List<TreatmentType> getAllTreatments() {
        List<TreatmentType> list = new ArrayList<>();
        String sql = "SELECT treatment_id, treatment_name, treatment_cost FROM treatment_types ORDER BY treatment_name";

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TreatmentType t = new TreatmentType();
                t.setTreatmentId(rs.getInt("treatment_id"));
                t.setTreatmentName(rs.getString("treatment_name"));
                t.setTreatmentCost(rs.getBigDecimal("treatment_cost"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching treatments: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    
     public List<TreatmentType> getAllTreatments() {
        List<TreatmentType> list = new ArrayList<>();
        String sql = "SELECT treatment_id, treatment_name, treatment_cost FROM treatment_types ORDER BY treatment_name";

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TreatmentType t = new TreatmentType();
                t.setTreatmentId(rs.getInt("treatment_id"));
                t.setTreatmentName(rs.getString("treatment_name"));
                t.setTreatmentCost(rs.getBigDecimal("treatment_cost"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching treatments: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public int addTreatment(TreatmentType t) {
        String sql = "INSERT INTO treatment_types (treatment_name, treatment_cost) VALUES (?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTreatmentName());
            ps.setBigDecimal(2, t.getTreatmentCost());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding treatment: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateTreatment(TreatmentType t) {
        String sql = "UPDATE treatment_types SET treatment_name = ?, treatment_cost = ? WHERE treatment_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTreatmentName());
            ps.setBigDecimal(2, t.getTreatmentCost());
            ps.setInt(3, t.getTreatmentId());
            ps.executeUpdate();
            return true; // don't rely on affected-rows count (0 if values unchanged)
        } catch (SQLException e) {
            System.err.println("Error updating treatment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Hard delete, cascading through bills/appointments that reference this
    // treatment (same FK-safe pattern used for deleting a dentist).
    public boolean deleteTreatment(int treatmentId) {
        Connection conn = DBConnection.getInstance().getConnection();
        String deleteBillsSql =
                "DELETE b FROM bills b " +
                "JOIN appointments a ON b.appointment_no = a.appointment_no " +
                "WHERE a.treatment_id = ?";
        String deleteAppointmentsSql = "DELETE FROM appointments WHERE treatment_id = ?";
        String deleteTreatmentSql = "DELETE FROM treatment_types WHERE treatment_id = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement psBills = conn.prepareStatement(deleteBillsSql)) {
                psBills.setInt(1, treatmentId);
                psBills.executeUpdate();
            }
            try (PreparedStatement psAppointments = conn.prepareStatement(deleteAppointmentsSql)) {
                psAppointments.setInt(1, treatmentId);
                psAppointments.executeUpdate();
            }

            int rowsAffected;
            try (PreparedStatement psTreatment = conn.prepareStatement(deleteTreatmentSql)) {
                psTreatment.setInt(1, treatmentId);
                rowsAffected = psTreatment.executeUpdate();
            }

            conn.commit();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting treatment (cascade): " + e.getMessage());
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
}