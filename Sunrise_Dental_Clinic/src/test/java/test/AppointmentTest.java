package test;

import model.Appointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        appointment = new Appointment();
    }

    @ParameterizedTest
    @DisplayName("Verify Appointment Data from Database Dump")
    @CsvSource({
        "APT0001, 1, 2, 5, admin, 2026-08-22, 10:25:00, COMPLETED",
        "APT0004, 4, 2, 4, admin, 2026-08-22, 10:02:00, CANCELLED",
        "APT0007, 8, 1, 6, admin, 2026-09-02, 16:39:00, PENDING"
    })
    void testAppointmentDataFromDump(String appNo, int patientId, int dentistId, int treatmentId, 
                                     String username, String date, String time, String status) {
        appointment.setAppointmentNo(appNo);
        appointment.setPatientId(patientId);
        appointment.setDentistId(dentistId);
        appointment.setTreatmentId(treatmentId);
        appointment.setBookedByUsername(username);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setStatus(status);

        assertEquals(appNo, appointment.getAppointmentNo());
        assertEquals(patientId, appointment.getPatientId());
        assertEquals(dentistId, appointment.getDentistId());
        assertEquals(treatmentId, appointment.getTreatmentId());
        assertEquals(username, appointment.getBookedByUsername());
        assertEquals(date, appointment.getAppointmentDate());
        assertEquals(time, appointment.getAppointmentTime());
        assertEquals(status, appointment.getStatus());
    }

    /**
     * Database එකේ Foreign Key Relational Data (Patient Name + Treatment Name + Dentist Fee) 
     * Model එකට mapping වීම පරීක්ෂා කිරීම:
     * Example: APT0001 -> Patient: Mr. Kamal Perera | Treatment: Braces Fitting (45000.0) | Dentist: Dr. Shevon De Silva (8700.0)
     */
    @Test
    @DisplayName("Test Mapped Relational Database Fields for Appointment")
    void testAppointmentRelationalMapping() {
        appointment.setAppointmentNo("APT0001");
        appointment.setPatientName("Mr. Kamal Perera");
        appointment.setTreatmentName("Braces Fitting");
        appointment.setTreatmentCost(45000.00);
        appointment.setDentistName("Dr. Shevon De Silva");
        appointment.setConsultationFee(8700.00);

        assertEquals("APT0001", appointment.getAppointmentNo());
        assertEquals("Mr. Kamal Perera", appointment.getPatientName());
        assertEquals("Braces Fitting", appointment.getTreatmentName());
        assertEquals(45000.00, appointment.getTreatmentCost(), 0.001);
        assertEquals("Dr. Shevon De Silva", appointment.getDentistName());
        assertEquals(8700.00, appointment.getConsultationFee(), 0.001);
    }
}