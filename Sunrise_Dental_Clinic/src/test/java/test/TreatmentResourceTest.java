package test;

import com.mycompany.sunrise_dental_clinic.resources.TreatmentResource;
import model.TreatmentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreatmentResourceTest {

    private TreatmentResource treatmentResource;

    @BeforeEach
    void setUp() {
        treatmentResource = new TreatmentResource();
    }

    @Test
    @DisplayName("Test getAllTreatments - Fetches real DB records")
    void testGetAllTreatments() {
        Response response = treatmentResource.getAllTreatments();

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<TreatmentType> list = (List<TreatmentType>) response.getEntity();
        assertFalse(list.isEmpty(), "Treatment types table.");
    }

    @Test
    @DisplayName("Test addTreatment - Success (201 Created)")
    void testAddTreatmentSuccess() {
        TreatmentType treatment = new TreatmentType();
        treatment.setTreatmentName("Teeth Whitening");
        treatment.setTreatmentCost(new BigDecimal("25000.00"));

        Response response = treatmentResource.addTreatment(treatment);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    @DisplayName("Test updateTreatment - Success for Admin (200 OK)")
    void testUpdateTreatmentSuccess() {
        TreatmentType treatment = new TreatmentType();
        treatment.setTreatmentName("Tooth Filling Updated");
        treatment.setTreatmentCost(new BigDecimal("8500.00"));

        Response response = treatmentResource.updateTreatment(1, "admin", treatment);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    @DisplayName("Test updateTreatment - Forbidden for Non-Admin User (403 Forbidden)")
    void testUpdateTreatmentForbidden() {
        TreatmentType treatment = new TreatmentType();
        treatment.setTreatmentName("Tooth Filling Updated");
        treatment.setTreatmentCost(new BigDecimal("8500.00"));

        Response response = treatmentResource.updateTreatment(1, "shehara01", treatment);

        assertEquals(403, response.getStatus());
    }

    @Test
    @DisplayName("Test deleteTreatment - Forbidden for Non-Admin User (403 Forbidden)")
    void testDeleteTreatmentForbidden() {
        Response response = treatmentResource.deleteTreatment(1, "shehara01");

        assertEquals(403, response.getStatus());
    }

    @Test
    @DisplayName("Test deleteTreatment - Non-existent ID (404 Not Found)")
    void testDeleteTreatmentNotFound() {
        Response response = treatmentResource.deleteTreatment(9999, "admin");

        assertEquals(404, response.getStatus());
    }
}