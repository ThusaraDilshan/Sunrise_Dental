package test;

import com.mycompany.sunrise_dental_clinic.resources.LoginResource;
import dao.DentistDAO;
import dao.StaffDAO;
import model.Dentist;
import model.Staff;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginResourceTest {

    @InjectMocks
    private LoginResource loginResource;

    @Mock
    private StaffDAO mockStaffDAO;

    @Mock
    private DentistDAO mockDentistDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Inject Mocks directly into private final fields of LoginResource
        setPrivateField(loginResource, "staffDAO", mockStaffDAO);
        setPrivateField(loginResource, "dentistDAO", mockDentistDAO);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("Test Staff Login Success")
    void testStaffLoginSuccess() {
        // Given
        Staff mockStaff = new Staff();
        mockStaff.setStaffId(1);
        mockStaff.setUsername("admin");
        mockStaff.setPassword("admin123");
        mockStaff.setRole("ADMIN");

        when(mockStaffDAO.validateLogin("admin", "admin123")).thenReturn(mockStaff);

        // When
        Response response = loginResource.handleLogin("admin", "admin123");

        // Then
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("success", entity.get("status"));
        assertEquals("ADMIN", entity.get("role"));
        assertEquals("admin", entity.get("username"));

        verify(mockStaffDAO, times(1)).validateLogin("admin", "admin123");
        verify(mockDentistDAO, never()).validateLogin(anyString(), anyString());
    }

    @Test
    @DisplayName("Test Dentist Login Success")
    void testDentistLoginSuccess() {
        // Given
        Dentist mockDentist = new Dentist(1, "Dr. Shehan", "dr.shehan", "shehan123", "General", "0771234567", 7580.00);

        when(mockStaffDAO.validateLogin("dr.shehan", "shehan123")).thenReturn(null);
        when(mockDentistDAO.validateLogin("dr.shehan", "shehan123")).thenReturn(mockDentist);

        Response response = loginResource.handleLogin("dr.shehan", "shehan123");

        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("success", entity.get("status"));
        assertEquals("DENTIST", entity.get("role"));
        assertEquals("dr.shehan", entity.get("username"));
        assertEquals(1, entity.get("dentistId"));

        verify(mockStaffDAO, times(1)).validateLogin("dr.shehan", "shehan123");
        verify(mockDentistDAO, times(1)).validateLogin("dr.shehan", "shehan123");
    }

    @Test
    @DisplayName("Test Login Failure - Invalid Credentials")
    void testLoginInvalidCredentials() {
        // Given
        when(mockStaffDAO.validateLogin("admin", "wrongpassword")).thenReturn(null);
        when(mockDentistDAO.validateLogin("admin", "wrongpassword")).thenReturn(null);

        // When
        Response response = loginResource.handleLogin("admin", "wrongpassword");

        // Then
        assertEquals(401, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("error", entity.get("status"));
        assertEquals("Invalid username or password.", entity.get("message"));
    }

    @Test
    @DisplayName("Test Login Validation - Missing Fields")
    void testLoginMissingFields() {
        // When
        Response response = loginResource.handleLogin("", "");

        // Then
        assertEquals(400, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("error", entity.get("status"));
        assertEquals("Username and Password are required.", entity.get("message"));

        verifyNoInteractions(mockStaffDAO, mockDentistDAO);
    }
}