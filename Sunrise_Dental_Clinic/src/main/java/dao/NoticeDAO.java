package dao;

import model.Notice;
import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {
    public boolean addNotice(Notice notice) {
        String sql = "INSERT INTO notices (dentist_id, description, sent_by) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notice.getDentistId());
            ps.setString(2, notice.getDescription());
            ps.setString(3, notice.getSentBy());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        notice.setNoticeId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error in addNotice: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Notice> getNoticesByDentistId(int dentistId) {
        List<Notice> notices = new ArrayList<>();
        String sql = "SELECT * FROM notices WHERE dentist_id = ? ORDER BY created_at DESC";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notices.add(new Notice(
                            rs.getInt("notice_id"),
                            rs.getInt("dentist_id"),
                            rs.getString("description"),
                            rs.getString("sent_by"),
                            rs.getBoolean("is_read"),
                            String.valueOf(rs.getTimestamp("created_at"))
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getNoticesByDentistId: " + e.getMessage());
            e.printStackTrace();
        }
        return notices;
    }
    
    public boolean markAsRead(int noticeId) {
        String sql = "UPDATE notices SET is_read = 1 WHERE notice_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, noticeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in markAsRead: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}