package test;

import model.Dentist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DentistTest {

    private Dentist dentist;

    @BeforeEach
    void setUp() {
        dentist = new Dentist();
    }

    @ParameterizedTest
    @DisplayName("Verify Dentist Data from Database Dump using Full Constructor")
    @CsvSource({
        "1, Dr. Shehan Jayasinghe, dr.shehan, shehan123, General Dentistry, 0771234567, 7580.00",
        "2, Dr. Shevon De Silva, dr.shevon, shevon123, Orthodontics, 0777654321, 8700.00"
    })
    void testDentistDataFromDump(int dentistId, String name, String username, String password, 
                                 String specialization, String contactNo, double consultationFee) {
        Dentist dbDentist = new Dentist(dentistId, name, username, password, specialization, contactNo, consultationFee);

        assertEquals(dentistId, dbDentist.getDentistId());
        assertEquals(name, dbDentist.getDentistName());
        assertEquals(username, dbDentist.getUsername());
        assertEquals(password, dbDentist.getPassword());
        assertEquals(specialization, dbDentist.getSpecialization());
        assertEquals(contactNo, dbDentist.getContactNo());
        assertEquals(consultationFee, dbDentist.getConsultationFee(), 0.001);
    }

    @Test
    @DisplayName("Verify toString Output for Database Records")
    void testToStringWithDatabaseData() {
        Dentist dbDentist = new Dentist(1, "Dr. Shehan Jayasinghe", "dr.shehan", "shehan123", 
                                        "General Dentistry", "0771234567", 7580.00);

        String expected = "Dentist{dentistId=1, dentistName='Dr. Shehan Jayasinghe', username='dr.shehan', " +
                         "specialization='General Dentistry', contactNo='0771234567', consultationFee=7580.0}";

        assertEquals(expected, dbDentist.toString());
    }

    @Test
    @DisplayName("Verify Consultation Fee Update")
    void testFeeUpdate() {
        dentist.setConsultationFee(7580.00);
        assertEquals(7580.00, dentist.getConsultationFee(), 0.001);

        dentist.setConsultationFee(8500.00);
        assertEquals(8500.00, dentist.getConsultationFee(), 0.001);
    }
}