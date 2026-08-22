package com.mycompany.sunrise_dental_clinic.resources;

import dao.StaffDAO;
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

@Path("/staff")
public class StaffResource {

    private final StaffDAO staffDAO = new StaffDAO();

    // Anyone logged in to the dashboard can see the staff list
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllStaff() {
        List<Staff> list = staffDAO.getAllStaff();
        return Response.ok(list).build();
    }

    // Used by the dashboard on load to find out who is logged in and what
    // their role is (so it knows whether to show the Admin-only controls).
    @GET
    @Path("/username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByUsername(@PathParam("username") String username) {
        Staff staff = staffDAO.findByUsername(username);
        if (staff == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Staff member not found\"}")
                    .build();
        }
        staff.setPassword(null); // never send the password hash to the browser
        return Response.ok(staff).build();
    }

    // Only an Admin account may add new staff members.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createStaff(@QueryParam("actingUsername") String actingUsername, Staff staff) {
        if (!isAdmin(actingUsername)) {
            return forbidden();
        }
        int newId = staffDAO.createStaff(staff);
        if (newId != -1) {
            staff.setStaffId(newId);
            staff.setPassword(null);
            return Response.ok(staff).build();
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"Could not create staff member. Username may already be taken.\"}")
                .build();
    }

    // Only an Admin account may edit staff members.
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateStaff(@PathParam("id") int id,
                                 @QueryParam("actingUsername") String actingUsername,
                                 Staff staff) {
        if (!isAdmin(actingUsername)) {
            return forbidden();
        }
        staff.setStaffId(id);
        boolean success = staffDAO.updateStaff(staff);
        if (success) {
            staff.setPassword(null);
            return Response.ok(staff).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Staff member not found or update failed\"}")
                .build();
    }

    // Only an Admin account may delete staff members.
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteStaff(@PathParam("id") int id,
                                 @QueryParam("actingUsername") String actingUsername) {
        if (!isAdmin(actingUsername)) {
            return forbidden();
        }
        boolean success = staffDAO.deleteStaff(id);
        if (success) {
            return Response.ok("{\"message\":\"Staff member deleted successfully\"}").build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Staff member not found\"}")
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