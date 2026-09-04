package test;


import model.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class StaffTest {

    private Staff staff;

    @BeforeEach
    void setUp() {
        staff = new Staff();
    }

    @Test
    @DisplayName("Test Constructor with ID and Core Fields")
    void testConstructorWithId() {
        Staff testStaff = new Staff(1, "Super Admin", "admin", "admin123", "0775801800", "ADMIN");

        assertEquals(1, testStaff.getStaffId());
        assertEquals("Super Admin", testStaff.getStaffName());
        assertEquals("admin", testStaff.getUsername());
        assertEquals("admin123", testStaff.getPassword());
        assertEquals("0775801800", testStaff.getContactNo());
        assertEquals("ADMIN", testStaff.getRole());
    }

    @Test
    @DisplayName("Test Full Constructor Including createdAt (StaffDAO Mapping)")
    void testFullConstructorWithCreatedAt() {
        Staff fullStaff = new Staff(1, "Super Admin", "admin", "admin123", "0775801800", "ADMIN", "2026-08-19 15:13:47");

        assertEquals(1, fullStaff.getStaffId());
        assertEquals("Super Admin", fullStaff.getStaffName());
        assertEquals("admin", fullStaff.getUsername());
        assertEquals("admin123", fullStaff.getPassword());
        assertEquals("0775801800", fullStaff.getContactNo());
        assertEquals("ADMIN", fullStaff.getRole());
        assertEquals("2026-08-19 15:13:47", fullStaff.getCreatedAt());
    }

    @Test
    @DisplayName("Test Constructor without ID (New Staff Registration)")
    void testConstructorWithoutId() {
        Staff newStaff = new Staff("Shehara Hansani", "shehara01", "shehara123", "0779696856", "RECEPTIONIST");

        assertEquals(0, newStaff.getStaffId());
        assertEquals("Shehara Hansani", newStaff.getStaffName());
        assertEquals("shehara01", newStaff.getUsername());
        assertEquals("shehara123", newStaff.getPassword());
        assertEquals("0779696856", newStaff.getContactNo());
        assertEquals("RECEPTIONIST", newStaff.getRole());
    }

    /**
     * Database dump එකේ ඇති staff table එකේ සැබෑ දත්ත පරීක්ෂා කිරීම:
     * ID: 1 | Super Admin    | admin     | admin123   | 0775801800 | ADMIN
     * ID: 2 | Admin 1        | admin1    | jagath123  | 0775815493 | ADMIN
     * ID: 6 | Shehara Hansani| shehara01 | shehara123 | 0779696856 | RECEPTIONIST
     */
    @ParameterizedTest
    @DisplayName("Verify Staff Data from Database Dump")
    @CsvSource({
        "1, Super Admin, admin, admin123, 0775801800, ADMIN",
        "2, Admin 1, admin1, jagath123, 0775815493, ADMIN",
        "6, Shehara Hansani, shehara01, shehara123, 0779696856, RECEPTIONIST"
    })
    void testStaffDataFromDump(int staffId, String name, String username, String password, String contactNo, String role) {
        Staff dbStaff = new Staff(staffId, name, username, password, contactNo, role);

        assertEquals(staffId, dbStaff.getStaffId());
        assertEquals(name, dbStaff.getStaffName());
        assertEquals(username, dbStaff.getUsername());
        assertEquals(password, dbStaff.getPassword());
        assertEquals(contactNo, dbStaff.getContactNo());
        assertEquals(role, dbStaff.getRole());
    }

    @Test
    @DisplayName("Test Getters and Setters")
    void testGettersAndSetters() {
        staff.setStaffId(10);
        staff.setStaffName("Kamal Jayalath");
        staff.setUsername("kamalj");
        staff.setPassword("pass1234");
        staff.setContactNo("0712233445");
        staff.setRole("RECEPTIONIST");
        staff.setCreatedAt("2026-09-04 12:00:00");

        assertEquals(10, staff.getStaffId());
        assertEquals("Kamal Jayalath", staff.getStaffName());
        assertEquals("kamalj", staff.getUsername());
        assertEquals("pass1234", staff.getPassword());
        assertEquals("0712233445", staff.getContactNo());
        assertEquals("RECEPTIONIST", staff.getRole());
        assertEquals("2026-09-04 12:00:00", staff.getCreatedAt());
    }

    @Test
    @DisplayName("Test toString Method")
    void testToString() {
        Staff testStaff = new Staff(1, "Super Admin", "admin", "admin123", "0775801800", "ADMIN");
        String expected = "Staff{staffId=1, staffName='Super Admin', username='admin', role='ADMIN'}";

        assertEquals(expected, testStaff.toString());
    }
}