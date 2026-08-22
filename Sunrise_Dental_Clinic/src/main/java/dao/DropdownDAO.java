package dao;

import model.Dentist;
import model.TreatmentType;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DropdownDAO {

    public List<Dentist> getAllDentists() {
        List<Dentist> list = new ArrayList<>();
        // contact_no සහ consultation_fee එකතු කරන ලදී - මේවා select නොවීම නිසා dashboard එකේ "-" / Rs. 0.00 පෙන්නුනේ
        String sql = "SELECT dentist_id, dentist_name, specialization, contact_no, consultation_fee FROM dentists ORDER BY dentist_name";

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Dentist d = new Dentist();
                d.setDentistId(rs.getInt("dentist_id"));
                d.setDentistName(rs.getString("dentist_name"));
                d.setSpecialization(rs.getString("specialization"));
                d.setContactNo(rs.getString("contact_no"));
                d.setConsultationFee(rs.getDouble("consultation_fee"));
                list.add(d);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching dentists from DB: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<TreatmentType> getAllTreatments() {
        List<TreatmentType> list = new ArrayList<>();
        // Database Table එක 'treatment_types' ලෙස නිවැරදි කරන ලදී
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
            System.err.println("Error fetching treatments from DB: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}