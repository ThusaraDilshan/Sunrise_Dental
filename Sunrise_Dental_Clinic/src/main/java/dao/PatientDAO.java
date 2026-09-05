/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Patient;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PatientDAO {
    public int createPatient(Patient patient) {
        String sql = "INSERT INTO patients (patient_name, address, contact_no) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getPatientName());
            ps.setString(2, patient.getAddress());
            ps.setString(3, patient.getContactNo());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Patient findByContactNo(String contactNo) {
        String sql = "SELECT * FROM patients WHERE contact_no = ? LIMIT 1";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contactNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                            rs.getInt("patient_id"),
                            rs.getString("patient_name"),
                            rs.getString("address"),
                            rs.getString("contact_no")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.List<Patient> getAllPatients() {
        java.util.List<Patient> list = new java.util.ArrayList<>();
        String sql = "SELECT patient_id, patient_name, address, contact_no FROM patients ORDER BY patient_name";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("address"),
                        rs.getString("contact_no")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE patients SET patient_name = ?, address = ?, contact_no = ? WHERE patient_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getPatientName());
            ps.setString(2, patient.getAddress());
            ps.setString(3, patient.getContactNo());
            ps.setInt(4, patient.getPatientId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete patient. Fails (returns false) if the patient still has
    // appointments/bills linked, since those tables reference patient_id
    // via FOREIGN KEY without ON DELETE CASCADE.
    public boolean deletePatient(int patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}