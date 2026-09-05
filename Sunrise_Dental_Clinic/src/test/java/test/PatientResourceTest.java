package test;

import com.mycompany.sunrise_dental_clinic.resources.PatientResource;
import model.Patient;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientResourceTest {

    private PatientResource patientResource;

    @BeforeEach
    void setUp() {
        patientResource = new PatientResource();
    }

    @Test
    @DisplayName("Test getAllPatients - Fetches real DB records")
    void testGetAllPatients() {
        Response response = patientResource.getAllPatients();

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Patient> patients = (List<Patient>) response.getEntity();
        assertFalse(patients.isEmpty(), "Patients table එකෙහි දත්ත පැවතිය යුතුය.");
    }

    @Test
    @DisplayName("Test addPatient - Success (201 Created)")
    void testAddPatientSuccess() {
        Patient newPatient = new Patient();
        newPatient.setPatientName("Nimal Perera");
        newPatient.setAddress("No 45, Kandy Road, Nittambuwa");
        newPatient.setContactNo("0719998877");

        Response response = patientResource.addPatient(newPatient);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    @DisplayName("Test updatePatient - Existing Patient ID 2")
    void testUpdatePatientSuccess() {
        Patient updatedPatient = new Patient();
        updatedPatient.setPatientName("Mr. Sunil Jayamaha Updated");
        updatedPatient.setAddress("58/15, Galle road, Colombo");
        updatedPatient.setContactNo("07752321545");

        Response response = patientResource.updatePatient(2, updatedPatient);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    @DisplayName("Test updatePatient - Non-existent Patient ID (9999)")
    void testUpdatePatientNotFound() {
        Patient patient = new Patient();
        patient.setPatientName("Non Existent Patient");
        patient.setAddress("N/A");
        patient.setContactNo("0000000000");

        Response response = patientResource.updatePatient(9999, patient);

        assertEquals(404, response.getStatus());
    }

    @Test
    @DisplayName("Test deletePatient - Forbidden for Non-Admin User (shehara01)")
    void testDeletePatientForbidden() {
        Response response = patientResource.deletePatient(5, "shehara01");

        assertEquals(403, response.getStatus());
    }

    @Test
    @DisplayName("Test deletePatient - Conflict status when deleting existing patient linked to records")
    void testDeletePatientConflict() {
        Response response = patientResource.deletePatient(5, "admin");

        assertEquals(409, response.getStatus());
    }
}