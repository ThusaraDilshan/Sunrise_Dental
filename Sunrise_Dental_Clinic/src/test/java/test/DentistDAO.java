package test;

import dao.DentistDAO;
import model.Dentist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DentistDAOTest {

    private DentistDAO dentistDAO;

    @BeforeEach
    void setUp() {
        dentistDAO = new DentistDAO();
    }

    @Test
    @DisplayName("Test validateLogin - Success with DB Dump Data (dr.shehan / shehan123)")
    void testValidateLoginSuccess() {
        // DB Dump: ID 1 | Dr. Shehan Jayasinghe | dr.shehan | shehan123
        Dentist dentist = dentistDAO.validateLogin("dr.shehan", "shehan123");

        assertNotNull(dentist, "Dentist should be found in database");
        assertEquals(1, dentist.getDentistId());
        assertEquals("Dr. Shehan Jayasinghe", dentist.getDentistName());
        assertEquals("General Dentistry", dentist.getSpecialization());
        assertEquals(7580.00, dentist.getConsultationFee());
    }

    @Test
    @DisplayName("Test validateLogin - Invalid Password")
    void testValidateLoginFailure() {
        Dentist dentist = dentistDAO.validateLogin("dr.shehan", "wrongpass");

        assertNull(dentist, "Should return null for invalid credentials");
    }

    @Test
    @DisplayName("Test updateDentistProfile - Success")
    void testUpdateDentistProfile() {
        // DB Dump ID 2 (Dr. Shevon De Silva) හි දත්ත update කර බැලීම
        boolean updated = dentistDAO.updateDentistProfile(
                2, 
                "Dr. Shevon De Silva", 
                "0777654321", 
                "Orthodontics", 
                8500.00, 
                "shevon123"
        );

        assertTrue(updated, "Dentist profile update should return true");
    }
}