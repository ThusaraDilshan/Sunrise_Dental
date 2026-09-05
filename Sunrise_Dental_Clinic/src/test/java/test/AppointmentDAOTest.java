package test;

import dao.AppointmentDAO;
import model.Appointment;
import util.DBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AppointmentDAOTest {

    @InjectMocks
    private AppointmentDAO appointmentDAO;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test getAppointmentByNo - Database Data (APT0001)")
    void testGetAppointmentByNoSuccess() throws SQLException {
        try (MockedStatic<DBConnection> mockedDB = Mockito.mockStatic(DBConnection.class)) {
            DBConnection mockDBInstance = mock(DBConnection.class);
            mockedDB.when(DBConnection::getInstance).thenReturn(mockDBInstance);
            when(mockDBInstance.getConnection()).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            // Dump Data: APT0001 | Patient: Kamal Perera | Dentist: Shevon | Treatment: Braces Fitting
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("appointment_no")).thenReturn("APT0001");
            when(mockResultSet.getInt("patient_id")).thenReturn(1);
            when(mockResultSet.getString("patient_name")).thenReturn("Mr. Kamal Perera");
            when(mockResultSet.getString("contact_no")).thenReturn("071846987");
            when(mockResultSet.getString("address")).thenReturn("58/18/A, Swarna road, Colombo");
            when(mockResultSet.getInt("dentist_id")).thenReturn(2);
            when(mockResultSet.getString("dentist_name")).thenReturn("Dr. Shevon De Silva");
            when(mockResultSet.getInt("treatment_id")).thenReturn(5);
            when(mockResultSet.getString("treatment_name")).thenReturn("Braces Fitting");
            when(mockResultSet.getDate("appointment_date")).thenReturn(Date.valueOf("2026-08-22"));
            when(mockResultSet.getTime("appointment_time")).thenReturn(Time.valueOf("10:25:00"));
            when(mockResultSet.getString("status")).thenReturn("COMPLETED");

            Appointment apt = appointmentDAO.getAppointmentByNo("APT0001");

            assertNotNull(apt);
            assertEquals("APT0001", apt.getAppointmentNo());
            assertEquals("Mr. Kamal Perera", apt.getPatientName());
            assertEquals("Braces Fitting", apt.getTreatmentName());
            assertEquals("COMPLETED", apt.getStatus());

            verify(mockPreparedStatement).setString(1, "APT0001");
        }
    }

    @Test
    @DisplayName("Test generateNextAppointmentNo - DB with last record APT0007")
    void testGenerateNextAppointmentNo() throws SQLException {
        try (MockedStatic<DBConnection> mockedDB = Mockito.mockStatic(DBConnection.class)) {
            DBConnection mockDBInstance = mock(DBConnection.class);
            mockedDB.when(DBConnection::getInstance).thenReturn(mockDBInstance);
            when(mockDBInstance.getConnection()).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            // DB dump එකේ අවසන් Appointment එක APT0007 වේ. ඊළඟ අංකය APT0008 විය යුතුය.
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString(1)).thenReturn("APT0007");

            String nextNo = appointmentDAO.generateNextAppointmentNo();

            assertEquals("APT0008", nextNo);
        }
    }

    @Test
    @DisplayName("Test updateStatus - Success")
    void testUpdateStatusSuccess() throws SQLException {
        try (MockedStatic<DBConnection> mockedDB = Mockito.mockStatic(DBConnection.class)) {
            DBConnection mockDBInstance = mock(DBConnection.class);
            mockedDB.when(DBConnection::getInstance).thenReturn(mockDBInstance);
            when(mockDBInstance.getConnection()).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            boolean updated = appointmentDAO.updateStatus("APT0007", "COMPLETED");

            assertTrue(updated);
            verify(mockPreparedStatement).setString(1, "COMPLETED");
            verify(mockPreparedStatement).setString(2, "APT0007");
        }
    }

    @Test
    @DisplayName("Test deleteAppointment - Cascade Delete Bill & Appointment")
    void testDeleteAppointmentSuccess() throws SQLException {
        try (MockedStatic<DBConnection> mockedDB = Mockito.mockStatic(DBConnection.class)) {
            DBConnection mockDBInstance = mock(DBConnection.class);
            mockedDB.when(DBConnection::getInstance).thenReturn(mockDBInstance);
            when(mockDBInstance.getConnection()).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            boolean deleted = appointmentDAO.deleteAppointment("APT0001");

            assertTrue(deleted);
            verify(mockPreparedStatement, times(2)).executeUpdate();
        }
    }
}