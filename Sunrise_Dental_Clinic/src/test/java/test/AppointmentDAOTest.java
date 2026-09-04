package test;

import dao.AppointmentDAO;
import util.DBConnection;
import model.Appointment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentDAOTest {

    private AppointmentDAO appointmentDAO;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private DBConnection mockDbConnectionInstance;
    private MockedStatic<DBConnection> mockedStatic;

    @BeforeEach
    void setUp() {
        appointmentDAO = new AppointmentDAO();

        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockDbConnectionInstance = mock(DBConnection.class);

        mockedStatic = mockStatic(DBConnection.class);
        mockedStatic.when(DBConnection::getInstance).thenReturn(mockDbConnectionInstance);
        when(mockDbConnectionInstance.getConnection()).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close(); // always release the static mock after each test
    }

    @Test
    @DisplayName("getAppointmentByNo should return a mapped Appointment when the record exists")
    void testGetAppointmentByNo_found() throws SQLException {
        String appointmentNo = "APT0001";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("appointment_no")).thenReturn("APT0001");
        when(mockResultSet.getInt("patient_id")).thenReturn(1);
        when(mockResultSet.getString("patient_name")).thenReturn("Nimal Perera");
        when(mockResultSet.getString("contact_no")).thenReturn("0771234567");
        when(mockResultSet.getString("address")).thenReturn("Colombo");
        when(mockResultSet.getInt("dentist_id")).thenReturn(2);
        when(mockResultSet.getString("dentist_name")).thenReturn("Dr. Silva");
        when(mockResultSet.getInt("treatment_id")).thenReturn(3);
        when(mockResultSet.getString("treatment_name")).thenReturn("Root Canal");
        when(mockResultSet.getDate("appointment_date")).thenReturn(Date.valueOf("2026-09-04"));
        when(mockResultSet.getTime("appointment_time")).thenReturn(Time.valueOf("10:00:00"));
        when(mockResultSet.getString("status")).thenReturn("CONFIRMED");

        Appointment result = appointmentDAO.getAppointmentByNo(appointmentNo);

        assertNotNull(result, "Expected an Appointment object, got null");
        assertEquals("APT0001", result.getAppointmentNo());
        assertEquals("Nimal Perera", result.getPatientName());
        assertEquals("Dr. Silva", result.getDentistName());
        assertEquals("CONFIRMED", result.getStatus());

        verify(mockStatement).setString(1, appointmentNo);
    }

    @Test
    @DisplayName("getAppointmentByNo should return null when no matching record exists")
    void testGetAppointmentByNo_notFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // no rows

        Appointment result = appointmentDAO.getAppointmentByNo("APT9999");

        assertNull(result, "Expected null when appointment does not exist");
    }

    @Test
    @DisplayName("registerAppointment should return true when insert affects 1+ rows")
    void testRegisterAppointment_success() throws SQLException {
        Appointment apt = new Appointment();
        apt.setAppointmentNo("APT0010");
        apt.setPatientId(1);
        apt.setDentistId(2);
        apt.setTreatmentId(3);
        apt.setBookedByUsername("receptionist1");
        apt.setAppointmentDate("2026-09-10");
        apt.setAppointmentTime("14:30:00");
        apt.setStatus("CONFIRMED");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1); // 1 row inserted

        boolean result = appointmentDAO.registerAppointment(apt);

        assertTrue(result, "registerAppointment should return true on successful insert");
        verify(mockStatement).setString(1, "APT0010");
        verify(mockStatement).setInt(2, 1);
        verify(mockStatement).executeUpdate();
    }

    @Test
    @DisplayName("registerAppointment should return false when a SQLException occurs")
    void testRegisterAppointment_sqlException() throws SQLException {
        Appointment apt = new Appointment();
        apt.setAppointmentNo("APT0011");
        apt.setAppointmentDate("2026-09-10");
        apt.setAppointmentTime("14:30:00");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB connection lost"));

        boolean result = appointmentDAO.registerAppointment(apt);

        assertFalse(result, "registerAppointment should return false when SQLException is thrown");
    }

    @Test
    @DisplayName("updateStatus should return true when the appointment status is updated")
    void testUpdateStatus_success() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = appointmentDAO.updateStatus("APT0001", "COMPLETED");

        assertTrue(result);
        verify(mockStatement).setString(1, "COMPLETED");
        verify(mockStatement).setString(2, "APT0001");
    }

    @Test
    @DisplayName("deleteAppointment should return true when the appointment row is removed")
    void testDeleteAppointment_success() throws SQLException {
        PreparedStatement mockDeleteBillStmt = mock(PreparedStatement.class);
        PreparedStatement mockDeleteAptStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(contains("DELETE FROM bills"))).thenReturn(mockDeleteBillStmt);
        when(mockConnection.prepareStatement(contains("DELETE FROM appointments"))).thenReturn(mockDeleteAptStmt);
        when(mockDeleteAptStmt.executeUpdate()).thenReturn(1);

        boolean result = appointmentDAO.deleteAppointment("APT0001");

        assertTrue(result);
        verify(mockDeleteBillStmt).setString(1, "APT0001");
        verify(mockDeleteAptStmt).setString(1, "APT0001");
    }

    @Test
    @DisplayName("generateNextAppointmentNo should default to APT0001 when the table is empty")
    void testGenerateNextAppointmentNo_emptyTable() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // no existing appointments

        String result = appointmentDAO.generateNextAppointmentNo();

        assertEquals("APT0001", result);
    }

    @Test
    @DisplayName("generateNextAppointmentNo should increment the last appointment number")
    void testGenerateNextAppointmentNo_incrementsLastNumber() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(1)).thenReturn("APT0012");

        String result = appointmentDAO.generateNextAppointmentNo();

        assertEquals("APT0013", result);
    }
}