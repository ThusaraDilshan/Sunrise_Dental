package com.mycompany.sunrise_dental_clinic.resources;

import dao.PatientDAO;
import dao.StaffDAO;
import model.Patient;
import model.Staff;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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

@Path("/patients")
public class PatientResource {

    private final PatientDAO patientDAO = new PatientDAO();
    private final StaffDAO staffDAO = new StaffDAO();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPatients() {
        List<Patient> list = patientDAO.getAllPatients();
        return Response.ok(list).build();
    }

   @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPatient(Patient patient) {
        int generatedId = patientDAO.createPatient(patient);
        if (generatedId > 0) {
            patient.setPatientId(generatedId);
            return Response.status(Response.Status.CREATED).entity(patient).build();
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"Could not add patient\"}")
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePatient(@PathParam("id") int id, Patient patient) {
        patient.setPatientId(id);
        boolean success = patientDAO.updatePatient(patient);
        if (success) {
            return Response.ok(patient).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Patient not found or update failed\"}")
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePatient(@PathParam("id") int id,
                                   @QueryParam("actingUsername") String actingUsername) {
        if (!isAdmin(actingUsername)) {
            return forbidden();
        }
        boolean success = patientDAO.deletePatient(id);
        if (success) {
            return Response.ok("{\"message\":\"Patient deleted successfully\"}").build();
        }
        return Response.status(Response.Status.CONFLICT)
                .entity("{\"error\":\"Could not delete patient. They may still have appointments linked.\"}")
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