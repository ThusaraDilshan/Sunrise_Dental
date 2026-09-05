package test;

import com.mycompany.sunrise_dental_clinic.resources.AppointmentResource;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentResourceTest {

    private AppointmentResource appointmentResource;

    @BeforeEach
    void setUp() {
        appointmentResource = new AppointmentResource();
    }

    @Test
    @DisplayName("Test getAllAppointments - Status 200 OK")
    void testGetAllAppointments() {
        Response response = appointmentResource.getAllAppointments();

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    @DisplayName("Test getAppointment - Existing ID (APT0001)")
    void testGetAppointmentSuccess() {
        Response response = appointmentResource.getAppointment("APT0001");

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    @DisplayName("Test getAppointment - Non-existent ID (APT9999)")
    void testGetAppointmentNotFound() {
        Response response = appointmentResource.getAppointment("APT9999");

        assertEquals(404, response.getStatus());
    }

    @Test
    @DisplayName("Test registerAppointment - Validation Error (Missing required fields)")
    void testRegisterAppointmentValidationError() {
        AppointmentResource.AppointmentRequest request = new AppointmentResource.AppointmentRequest();
        // Required fields (patientName, contactNo, etc.) හිස්ව තබා අැත.

        Response response = appointmentResource.registerAppointment(request);

        assertEquals(400, response.getStatus());
    }

    @Test
    @DisplayName("Test deleteAppointment - Non-admin user should get 403 Forbidden")
    void testDeleteAppointmentForbidden() {
        // "reception" යනු Admin නොවන Staff සාමාජිකයෙකි.
        Response response = appointmentResource.deleteAppointment("APT0001", "reception");

        assertEquals(403, response.getStatus());
    }
}