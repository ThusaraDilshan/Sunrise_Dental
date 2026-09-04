package test;

import model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
    }

    @Test
    @DisplayName("Test Full Constructor with ID")
    void testFullConstructor() {
        Patient fullPatient = new Patient(1, "Mr. Kamal Perera", "58/18/A, Swarna road, Colombo", "071846987");

        assertEquals(1, fullPatient.getPatientId());
        assertEquals("Mr. Kamal Perera", fullPatient.getPatientName());
        assertEquals("58/18/A, Swarna road, Colombo", fullPatient.getAddress());
        assertEquals("071846987", fullPatient.getContactNo());
    }

    @Test
    @DisplayName("Test Constructor without ID (New Patient Registration)")
    void testConstructorWithoutId() {
        Patient newPatient = new Patient("Mrs. Chamila De Silva", "58/19/A, Galle Road, Negombo.", "0775894562");

        assertEquals(0, newPatient.getPatientId()); // Default int value
        assertEquals("Mrs. Chamila De Silva", newPatient.getPatientName());
        assertEquals("58/19/A, Galle Road, Negombo.", newPatient.getAddress());
        assertEquals("0775894562", newPatient.getContactNo());
    }

    /**
     * Database dump එකේ ඇති patients table එකේ සැබෑ දත්ත පරීක්ෂා කිරීම:
     * ID: 1 | Mr. Kamal Perera     | 58/18/A, Swarna road, Colombo | 071846987
     * ID: 2 | Mr. Sunil Jayamaha   | 58/15, Galle road,Colombo     | 07752321545
     * ID: 3 | Mr. Shehan Abeysinghe| 89, Helan Lane, Colombo       | 0755889640
     */
    @ParameterizedTest
    @DisplayName("Verify Patient Data from Database Dump")
    @CsvSource({
        "1, Mr. Kamal Perera, '58/18/A, Swarna road, Colombo', 071846987",
        "2, Mr. Sunil Jayamaha, '58/15, Galle road,Colombo', 07752321545",
        "3, Mr. Shehan Abeysinghe, '89, Helan Lane, Colombo', 0755889640"
    })
    void testPatientDataFromDump(int patientId, String name, String address, String contactNo) {
        Patient dbPatient = new Patient(patientId, name, address, contactNo);

        assertEquals(patientId, dbPatient.getPatientId());
        assertEquals(name, dbPatient.getPatientName());
        assertEquals(address, dbPatient.getAddress());
        assertEquals(contactNo, dbPatient.getContactNo());
    }

    @Test
    @DisplayName("Test Getters and Setters")
    void testGettersAndSetters() {
        patient.setPatientId(10);
        patient.setPatientName("Nimal Siripala");
        patient.setAddress("Kandy Road, Kelaniya");
        patient.setContactNo("0711122334");

        assertEquals(10, patient.getPatientId());
        assertEquals("Nimal Siripala", patient.getPatientName());
        assertEquals("Kandy Road, Kelaniya", patient.getAddress());
        assertEquals("0711122334", patient.getContactNo());
    }

    @Test
    @DisplayName("Test toString Method")
    void testToString() {
        Patient testPatient = new Patient(1, "Mr. Kamal Perera", "58/18/A, Swarna road, Colombo", "071846987");
        String expected = "Patient{patientId=1, patientName='Mr. Kamal Perera', contactNo='071846987'}";

        assertEquals(expected, testPatient.toString());
    }
}