package test;

import util.DBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DBConnectionTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        // Singleton Instance එකක් සෑම Test එකකටම පෙර Reset කිරීමට Reflection භාවිතා කරයි
        Field instanceField = DBConnection.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    @DisplayName("Test: Singleton instance creation")
    void testGetInstanceReturnsNotNull() {
        DBConnection instance1 = DBConnection.getInstance();
        assertNotNull(instance1, "DBConnection instance එක null නොවිය යුතුය.");
    }

    @Test
    @DisplayName("Test: Singleton pattern (Same instance check)")
    void testGetInstanceReturnsSameInstance() {
        DBConnection instance1 = DBConnection.getInstance();
        DBConnection instance2 = DBConnection.getInstance();

        assertSame(instance1, instance2, "getInstance() මගින් සැමවිටම එකම instance එක ලබා දිය යුතුය.");
    }

    @Test
    @DisplayName("Test: Connection object retrieval")
    void testGetConnection() throws SQLException {
        DBConnection dbConnection = DBConnection.getInstance();
        Connection connection = dbConnection.getConnection();

        assertNotNull(connection, "Connection object එක null නොවිය යුතුය.");
        assertFalse(connection.isClosed(), "Database connection එක active තත්වයේ පැවතිය යුතුය.");
    }
}