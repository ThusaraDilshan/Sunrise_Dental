package test;

import com.mycompany.sunrise_dental_clinic.resources.DentistResource;
import dao.DentistDAO;
import dao.DropdownDAO;
import dao.StaffDAO;
import model.Dentist;
import model.Staff;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DentistResourcesTest {

    @InjectMocks
    private DentistResource dentistResource;

    @Mock
    private DropdownDAO mockDropdownDAO;

    @Mock
    private DentistDAO mockDentistDAO;

    @Mock
    private StaffDAO mockStaffDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        setPrivateField(dentistResource, "dropdownDAO", mockDropdownDAO);
        setPrivateField(dentistResource, "dentistDAO", mockDentistDAO);
        setPrivateField(dentistResource, "staffDAO", mockStaffDAO);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("Test getAllDentists - Success (200 OK)")
    void testGetAllDentists() {
        List<Dentist> list = new ArrayList<>();
        list.add(new Dentist(1, "Dr. Shehan", "dr.shehan", "pass", "General", "0771234567", 5000.00));

        when(mockDropdownDAO.getAllDentists()).thenReturn(list);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(200);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.ok(any())).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Response response = dentistResource.getAllDentists();

            assertEquals(200, response.getStatus());
            verify(mockDropdownDAO, times(1)).getAllDentists();
        }
    }

    @Test
    @DisplayName("Test addDentist - Success (210 Created)")
    void testAddDentistSuccess() {
        Dentist newDentist = new Dentist(0, "Dr. Perera", "dr.perera", "perera123", "Orthodontics", "0712345678", 6000.00);

        when(mockDentistDAO.addDentist(any(Dentist.class))).thenReturn(10);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.status(Response.Status.CREATED)).thenReturn(builder);
            when(builder.entity(any())).thenReturn(builder);
            when(builder.type(anyString())).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Response response = dentistResource.addDentist(newDentist);

            assertEquals(201, response.getStatus());
            verify(mockDentistDAO, times(1)).addDentist(any(Dentist.class));
        }
    }

    @Test
    @DisplayName("Test updateDentist - Forbidden for Non-Admin User (403)")
    void testUpdateDentistForbidden() {
        Staff staff = new Staff();
        staff.setRole("RECEPTIONIST");

        when(mockStaffDAO.findByUsername("reception")).thenReturn(staff);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(403);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.status(Response.Status.FORBIDDEN)).thenReturn(builder);
            when(builder.entity(any())).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Dentist dentist = new Dentist();
            Response response = dentistResource.updateDentist(1, "reception", dentist);

            assertEquals(403, response.getStatus());
            verify(mockDentistDAO, never()).updateDentist(any(Dentist.class));
        }
    }

    @Test
    @DisplayName("Test deleteDentist - Success for Admin User (200 OK)")
    void testDeleteDentistSuccess() {
        Staff admin = new Staff();
        admin.setRole("ADMIN");

        when(mockStaffDAO.findByUsername("admin")).thenReturn(admin);
        when(mockDentistDAO.deleteDentist(1)).thenReturn(true);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(200);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.ok(anyString())).thenReturn(builder);
            when(builder.type(anyString())).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Response response = dentistResource.deleteDentist(1, "admin");

            assertEquals(200, response.getStatus());
            verify(mockDentistDAO, times(1)).deleteDentist(1);
        }
    }
}