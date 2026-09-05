package com.mycompany.sunrise_dental_clinic.resources;

import dao.DropdownDAO;
import dao.DentistDAO;
import dao.StaffDAO;
import model.Dentist;
import model.Staff;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/dentists")
public class DentistResource {

    private final DropdownDAO dropdownDAO = new DropdownDAO();
    private final DentistDAO dentistDAO = new DentistDAO();
    private final StaffDAO staffDAO = new StaffDAO();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDentists() {
        List<Dentist> list = dropdownDAO.getAllDentists();
        return Response.ok(list).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDentist(Dentist dentist) {
        int newId = dentistDAO.addDentist(dentist);
        if (newId != -1) {
            dentist.setDentistId(newId);
            return Response.status(Response.Status.CREATED)
                    .entity(dentist)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Failed to add dentist\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Only an Admin account may edit a dentist's profile.
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDentist(@PathParam("id") int id,
                                   @QueryParam("actingUsername") String actingUsername,
                                   Dentist dentist) {
        if (!isAdmin(actingUsername)) {
            return forbidden();
        }
        dentist.setDentistId(id);
        boolean success = dentistDAO.updateDentist(dentist);
        if (success) {
            return Response.ok(dentist)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Dentist not found or update failed\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
    
    @POST
    @Path("/{id}/profile")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateOwnProfile(@PathParam("id") int id,
                                      @jakarta.ws.rs.FormParam("dentistName") String dentistName,
                                      @jakarta.ws.rs.FormParam("contactNo") String contactNo,
                                      @jakarta.ws.rs.FormParam("specialization") String specialization,
                                      @jakarta.ws.rs.FormParam("consultationFee") String consultationFeeStr,
                                      @jakarta.ws.rs.FormParam("password") String password) {

        if (dentistName == null || dentistName.trim().isEmpty()
                || contactNo == null || contactNo.trim().isEmpty()
                || consultationFeeStr == null || consultationFeeStr.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"dentistName, contactNo and consultationFee are required\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        double consultationFee;
        try {
            consultationFee = Double.parseDouble(consultationFeeStr.trim());
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"consultationFee must be a number\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        Dentist dentist = new Dentist(
                id,
                dentistName.trim(),
                null,
                (password != null && !password.trim().isEmpty()) ? password.trim() : null,
                specialization != null ? specialization.trim() : null,
                contactNo.trim(),
                consultationFee
        );

        boolean success = dentistDAO.updateDentist(dentist);
        if (success) {
            dentist.setPassword(null); // never send password back to the browser
            return Response.ok(dentist)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Failed to update profile\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @POST
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDentist(@PathParam("id") int id,
                                   @QueryParam("actingUsername") String actingUsername) {
        if (!isAdmin(actingUsername)) {
            return forbidden();
        }
        boolean success = dentistDAO.deleteDentist(id);
        if (success) {
            return Response.ok("{\"message\":\"Dentist deleted successfully\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Failed to delete dentist: Dentist not found or referenced in other tables\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private boolean isAdmin(String actingUsername) {
        if (actingUsername == null || actingUsername.trim().isEmpty()) {
            return false;
        }
        Staff acting = staffDAO.findByUsername(actingUsername);
        return acting != null && "ADMIN".equalsIgnoreCase(acting.getRole());
    }

    private Response forbidden() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\":\"Only an Admin can perform this action.\"}")
                .build();
    }
}