package dao;

import model.Staff;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StaffDAO {

    // Used by LoginServlet / LoginResource to authenticate staff
    public Staff validateLogin(String username, String password) {
        Staff staff = findByUsername(username);
        if (staff != null && staff.getPassword() != null && staff.getPassword().equals(password)) {
            return staff;
        }
        return null;
    }
    
    public class StaffDAO {

    // Used by LoginServlet / LoginResource to authenticate staff
    public Staff validateLogin(String username, String password) {
        Staff staff = findByUsername(username);
        if (staff != null && staff.getPassword() != null && staff.getPassword().equals(password)) {
            return staff;
        }
        return null;
    }

    // Used by login and by the dashboard to find out the current user's role
    public Staff findByUsername(String username) {
        String sql = "SELECT * FROM staff WHERE username = ? LIMIT 1";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Staff(
                            rs.getInt("staff_id"),
                            rs.getString("staff_name"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("contact_no"),
                            rs.getString("role"),
                            String.valueOf(rs.getTimestamp("created_at"))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Used by Staff Management page (Staff Dashboard). Password is left out
    // of the list on purpose so it never gets sent to the browser in bulk.
    public java.util.List<Staff> getAllStaff() {
        java.util.List<Staff> list = new java.util.ArrayList<>();
        String sql = "SELECT staff_id, staff_name, username, contact_no, role, created_at " +
                     "FROM staff ORDER BY staff_name";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Staff staff = new Staff();
                staff.setStaffId(rs.getInt("staff_id"));
                staff.setStaffName(rs.getString("staff_name"));
                staff.setUsername(rs.getString("username"));
                staff.setContactNo(rs.getString("contact_no"));
                staff.setRole(rs.getString("role"));
                staff.setCreatedAt(String.valueOf(rs.getTimestamp("created_at")));
                list.add(staff);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Add a new staff member (Admin only - enforced in StaffResource)
    public int createStaff(Staff staff) {
        String sql = "INSERT INTO staff (staff_name, username, password, contact_no, role) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, staff.getStaffName());
            ps.setString(2, staff.getUsername());
            ps.setString(3, staff.getPassword());
            ps.setString(4, staff.getContactNo());
            ps.setString(5, staff.getRole());

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

    // Update staff details (Admin only - enforced in StaffResource).
    // Password is only overwritten when a new one is actually supplied,
    // so editing name/role/contact doesn't wipe out the existing password.
    public boolean updateStaff(Staff staff) {
        boolean changePassword = staff.getPassword() != null && !staff.getPassword().trim().isEmpty();

        String sql = changePassword
                ? "UPDATE staff SET staff_name = ?, username = ?, password = ?, contact_no = ?, role = ? WHERE staff_id = ?"
                : "UPDATE staff SET staff_name = ?, username = ?, contact_no = ?, role = ? WHERE staff_id = ?";

        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, staff.getStaffName());
            ps.setString(idx++, staff.getUsername());
            if (changePassword) {
                ps.setString(idx++, staff.getPassword());
            }
            ps.setString(idx++, staff.getContactNo());
            ps.setString(idx++, staff.getRole());
            ps.setInt(idx, staff.getStaffId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete staff (Admin only - enforced in StaffResource)
    public boolean deleteStaff(int staffId) {
        String sql = "DELETE FROM staff WHERE staff_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}