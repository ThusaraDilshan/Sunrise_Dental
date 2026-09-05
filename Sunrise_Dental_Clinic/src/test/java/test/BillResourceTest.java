package test;

import com.mycompany.sunrise_dental_clinic.resources.BillResource;
import dao.BillDAO;
import model.Bill;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillResourceTest {

    @InjectMocks
    private BillResource billResource;

    @Mock
    private BillDAO mockBillDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        setPrivateField(billResource, "billDAO", mockBillDAO);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("Test generateBill - Success")
    void testGenerateBillSuccess() {

        Bill mockBill = new Bill();
        mockBill.setAppointmentNo("APT0001");
        mockBill.setPatientName("Kamal Perera");
        mockBill.setContactNo("071846987");
        mockBill.setDentistName("Dr. Shehan");
        mockBill.setTreatmentName("Braces Fitting");
        mockBill.setAppointmentDate("2026-03-01");
        mockBill.setAppointmentTime("10:00:00");
        mockBill.setTreatmentCost(new BigDecimal("15000.00"));
        mockBill.setConsultationFee(new BigDecimal("2500.00"));
        mockBill.setTotalAmount(new BigDecimal("17500.00"));

        when(mockBillDAO.calculateAndSaveBill("APT0001")).thenReturn(mockBill);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(200);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.ok(any())).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Response response = billResource.generateBill("APT0001");

            assertEquals(200, response.getStatus());
            verify(mockBillDAO, times(1)).calculateAndSaveBill("APT0001");
        }
    }

    @Test
    @DisplayName("Test generateBill - Not Found (404)")
    void testGenerateBillNotFound() {
        // Given
        when(mockBillDAO.calculateAndSaveBill("APT9999")).thenReturn(null);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(404);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.status(Response.Status.NOT_FOUND)).thenReturn(builder);
            when(builder.entity(any())).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Response response = billResource.generateBill("APT9999");

            assertEquals(404, response.getStatus());
            verify(mockBillDAO, times(1)).calculateAndSaveBill("APT9999");
        }
    }
}