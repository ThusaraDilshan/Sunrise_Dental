package com.mycompany.sunrise_dental_clinic.resources;

import dao.DentistDAO;
import dao.StaffDAO;
import model.Dentist;
import model.Staff;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/login")
public class LoginResource {

    private final StaffDAO staffDAO = new StaffDAO();
    private final DentistDAO dentistDAO = new DentistDAO();

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleLogin(@FormParam("username") String username, 
                                @FormParam("password") String password) {

        Map<String, Object> result = new HashMap<>();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            result.put("status", "error");
            result.put("message", "Username and Password are required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
        }

        try {
            // 1. Check Staff Login
            Staff staff = staffDAO.validateLogin(username, password);
            if (staff != null) {
                String staffRole = staff.getRole() != null ? staff.getRole().toUpperCase() : "STAFF";
                result.put("status", "success");
                result.put("role", staffRole);
                result.put("username", staff.getUsername());
                return Response.ok(result).build();
            }

            // 2. Check Dentist Login
            Dentist dentist = dentistDAO.validateLogin(username, password);
            if (dentist != null) {
                result.put("status", "success");
                result.put("role", "DENTIST");
                result.put("username", dentist.getUsername());
                result.put("dentistId", dentist.getDentistId());
                return Response.ok(result).build();
            }

            // Invalid Login
            result.put("status", "error");
            result.put("message", "Invalid username or password.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(result).build();

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Database Error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
        }
    }
}