package test;

import dao.StaffDAO;
import model.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StaffDAOTest {

    private StaffDAO staffDAO;

    @BeforeEach
    void setUp() {
        staffDAO = new StaffDAO();
    }

    @Test
    @DisplayName("Test validateLogin - Success with DB Dump Data (admin / admin123)")
    void testValidateLoginSuccess() {
        Staff staff = staffDAO.validateLogin("admin", "admin123");

        assertNotNull(staff, "Staff user should be authenticated");
        assertEquals("Admin User", staff.getStaffName());
        assertEquals("admin", staff.getUsername());
        assertEquals("ADMIN", staff.getRole());
    }

    @Test
    @DisplayName("Test validateLogin - Invalid Credentials")
    void testValidateLoginFailure() {
        Staff staff = staffDAO.validateLogin("admin", "invalidpass");

        assertNull(staff, "Should return null for wrong password");
    }

    @Test
    @DisplayName("Test findByUsername - Success")
    void testFindByUsernameSuccess() {
        Staff staff = staffDAO.findByUsername("admin");

        assertNotNull(staff, "Username search should find existing admin");
        assertEquals("admin", staff.getUsername());
        assertEquals("ADMIN", staff.getRole());
    }

    @Test
    @DisplayName("Test findByUsername - Non-existent User")
    void testFindByUsernameNotFound() {
        Staff staff = staffDAO.findByUsername("non_existent_user");

        assertNull(staff, "Should return null for non-existent username");
    }

    @Test
    @DisplayName("Test getAllStaff - Returns data list from DB")
    void testGetAllStaff() {
        List<Staff> list = staffDAO.getAllStaff();

        assertNotNull(list);
        assertFalse(list.isEmpty(), "Staff list should not be empty");
    }

    @Test
    @DisplayName("Test updateStaff - Success")
    void testUpdateStaff() {
        Staff staff = new Staff();
        staff.setStaffId(1);
        staff.setStaffName("Admin User");
        staff.setUsername("admin");
        staff.setContactNo("0112345678");
        staff.setRole("ADMIN");
        staff.setPassword("admin123");

        boolean updated = staffDAO.updateStaff(staff);

        assertTrue(updated, "Staff details update should return true");
    }
}