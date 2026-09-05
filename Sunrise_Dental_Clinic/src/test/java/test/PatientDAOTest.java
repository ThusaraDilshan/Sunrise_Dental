package test;

import dao.PatientDAO;
import model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientDAOTest {

    private PatientDAO patientDAO;

    @BeforeEach
    void setUp() {
        patientDAO = new PatientDAO();
    }

    @Test
    @DisplayName("Test findByContactNo - Success with DB Dump Data (071846987)")
    void testFindByContactNoSuccess() {
        Patient patient = patientDAO.findByContactNo("071846987");

        assertNotNull(patient, "Patient should be found in database");
        assertEquals("Mr. Kamal Perera", patient.getPatientName());
        assertEquals("58/18/A, Swarna road, Colombo", patient.getAddress());
    }

    @Test
    @DisplayName("Test findByContactNo - Non-existent contact")
    void testFindByContactNoNotFound() {
        Patient patient = patientDAO.findByContactNo("0000000000");

        assertNull(patient, "Should return null for non-existent contact number");
    }

    @Test
    @DisplayName("Test getAllPatients - Returns data list from DB")
    void testGetAllPatients() {
        List<Patient> list = patientDAO.getAllPatients();

        assertNotNull(list);
        assertFalse(list.isEmpty(), "Patient list should not be empty");
    }

    @Test
    @DisplayName("Test updatePatient - Success")
    void testUpdatePatient() {
        Patient patient = new Patient(1, "Mr. Kamal Perera", "58/18/A, Swarna road, Colombo", "071846987");
        boolean updated = patientDAO.updatePatient(patient);

        assertTrue(updated, "Patient details update should return true");
    }
}