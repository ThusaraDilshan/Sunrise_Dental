package com.mycompany.sunrise_dental_clinic.resources;

import dao.TreatmentDAO;
import dao.StaffDAO;
import model.TreatmentType;
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

@Path("/treatments")
public class TreatmentResource {

    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private final StaffDAO staffDAO = new StaffDAO();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTreatments() {
        List<TreatmentType> list = treatmentDAO.getAllTreatments();
        return Response.ok(list).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTreatment(TreatmentType treatment) {
        int newId = treatmentDAO.addTreatment(treatment);
        if (newId != -1) {
            treatment.setTreatmentId(newId);
            return Response.status(Response.Status.CREATED)
                    .entity(treatment)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Failed to add treatment\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Only an Admin account may edit a treatment.
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTreatment(@PathParam("id") int id,
                                     @QueryParam("actingUsername") String actingUsername,
                                     TreatmentType treatment) {
        if (!isAdmin(actingUsername)) {
            return forbidden();
        }
        treatment.setTreatmentId(id);
        boolean success = treatmentDAO.updateTreatment(treatment);
        if (success) {
            return Response.ok(treatment)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Treatment not found or update failed\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Only an Admin account may delete a treatment.
    // POST (not @DELETE) - same GlassFish 405 workaround used for dentists.
    @POST
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTreatment(@PathParam("id") int id,
                                     @QueryParam("actingUsername") String actingUsername) {
        if (!isAdmin(actingUsername)) {
            return forbidden();
        }
        boolean success = treatmentDAO.deleteTreatment(id);
        if (success) {
            return Response.ok("{\"message\":\"Treatment deleted successfully\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Failed to delete treatment: Treatment not found\"}")
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