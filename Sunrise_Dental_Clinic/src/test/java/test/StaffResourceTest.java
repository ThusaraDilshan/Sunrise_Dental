package test;

import com.mycompany.sunrise_dental_clinic.resources.StaffResource;
import dao.StaffDAO;
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

class StaffResourceTest {

    @InjectMocks
    private StaffResource staffResource;

    @Mock
    private StaffDAO mockStaffDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        setPrivateField(staffResource, "staffDAO", mockStaffDAO);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("Test getAllStaff - Success (200 OK)")
    void testGetAllStaff() {
        List<Staff> list = new ArrayList<>();
        Staff staff = new Staff();
        staff.setStaffId(1);
        staff.setUsername("admin");
        list.add(staff);

        when(mockStaffDAO.getAllStaff()).thenReturn(list);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(200);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.ok(any())).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Response response = staffResource.getAllStaff();

            assertEquals(200, response.getStatus());
            verify(mockStaffDAO, times(1)).getAllStaff();
        }
    }

    @Test
    @DisplayName("Test getByUsername - Success (200 OK)")
    void testGetByUsernameSuccess() {
        Staff staff = new Staff();
        staff.setStaffId(1);
        staff.setUsername("admin");
        staff.setPassword("secret");

        when(mockStaffDAO.findByUsername("admin")).thenReturn(staff);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(200);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.ok(any(Staff.class))).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Response response = staffResource.getByUsername("admin");

            assertEquals(200, response.getStatus());
            assertNull(staff.getPassword(), "Password should be cleared before response");
            verify(mockStaffDAO, times(1)).findByUsername("admin");
        }
    }

    @Test
    @DisplayName("Test getByUsername - Not Found (404)")
    void testGetByUsernameNotFound() {
        when(mockStaffDAO.findByUsername("unknown")).thenReturn(null);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(404);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.status(Response.Status.NOT_FOUND)).thenReturn(builder);
            when(builder.entity(any())).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Response response = staffResource.getByUsername("unknown");

            assertEquals(404, response.getStatus());
            verify(mockStaffDAO, times(1)).findByUsername("unknown");
        }
    }

    @Test
    @DisplayName("Test createStaff - Forbidden for Non-Admin (403)")
    void testCreateStaffForbidden() {
        Staff actingUser = new Staff();
        actingUser.setRole("RECEPTIONIST");

        when(mockStaffDAO.findByUsername("reception")).thenReturn(actingUser);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(403);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.status(Response.Status.FORBIDDEN)).thenReturn(builder);
            when(builder.entity(any())).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Staff newStaff = new Staff();
            Response response = staffResource.createStaff("reception", newStaff);

            assertEquals(403, response.getStatus());
            verify(mockStaffDAO, never()).createStaff(any(Staff.class));
        }
    }

    @Test
    @DisplayName("Test deleteStaff - Success for Admin (200 OK)")
    void testDeleteStaffSuccess() {
        Staff admin = new Staff();
        admin.setRole("ADMIN");

        when(mockStaffDAO.findByUsername("admin")).thenReturn(admin);
        when(mockStaffDAO.deleteStaff(2)).thenReturn(true);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(200);

        try (MockedStatic<Response> responseMockedStatic = mockStatic(Response.class)) {
            Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
            responseMockedStatic.when(() -> Response.ok(anyString())).thenReturn(builder);
            when(builder.build()).thenReturn(mockResponse);

            Response response = staffResource.deleteStaff(2, "admin");

            assertEquals(200, response.getStatus());
            verify(mockStaffDAO, times(1)).deleteStaff(2);
        }
    }
}