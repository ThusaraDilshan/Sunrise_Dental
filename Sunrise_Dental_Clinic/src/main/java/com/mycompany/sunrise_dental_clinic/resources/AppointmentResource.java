package com.mycompany.sunrise_dental_clinic.resources;

import dao.AppointmentDAO;
import dao.PatientDAO;
import dao.StaffDAO;
import model.Appointment;
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

@Path("/appointment")
public class AppointmentResource {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final StaffDAO staffDAO = new StaffDAO();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerAppointment(AppointmentRequest request) {

        if (request.getPatientName() == null || request.getPatientName().trim().isEmpty()
                || request.getContactNo() == null || request.getContactNo().trim().isEmpty()
                || request.getDentistId() <= 0 || request.getTreatmentId() <= 0
                || request.getAppointmentDate() == null || request.getAppointmentTime() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"patientName, contactNo, dentistId, treatmentId, appointmentDate and appointmentTime are required\"}")
                    .build();
        }

        Patient patient = new Patient(request.getPatientName(), request.getAddress(), request.getContactNo());
        int newPatientId = patientDAO.createPatient(patient);

        if (newPatientId == -1) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to create patient record\"}")
                    .build();
        }

        Appointment apt = new Appointment();
        apt.setAppointmentNo(appointmentDAO.generateNextAppointmentNo());
        apt.setPatientId(newPatientId);
        apt.setDentistId(request.getDentistId());
        apt.setTreatmentId(request.getTreatmentId());
        apt.setBookedByUsername(request.getBookedByUsername());
        apt.setAppointmentDate(request.getAppointmentDate());

        String timeStr = request.getAppointmentTime().trim();
        if (timeStr.length() == 4) {
            timeStr = "0" + timeStr + ":00";
        } else if (timeStr.length() == 5) {
            timeStr = timeStr + ":00";
        }
        apt.setAppointmentTime(timeStr);
        apt.setStatus("PENDING");

        boolean success = appointmentDAO.registerAppointment(apt);

        if (success) {
            return Response.status(Response.Status.CREATED).entity(apt).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Patient was created but appointment registration failed\"}")
                    .build();
        }
    }

    @GET
    @Path("/by-dentist/{dentistId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAppointmentsByDentist(@PathParam("dentistId") int dentistId) {
        List<Appointment> list = appointmentDAO.getAppointmentsByDentistId(dentistId);
        return Response.ok(list).build();
    }

    @PUT
    @Path("/{appointmentNo}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateStatus(@PathParam("appointmentNo") String appointmentNo, StatusUpdateRequest request) {

        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"status is required\"}")
                    .build();
        }

        boolean updated = appointmentDAO.updateStatus(appointmentNo, request.getStatus().toUpperCase());

        if (updated) {
            Appointment apt = appointmentDAO.getAppointmentByNo(appointmentNo);
            return Response.ok(apt).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"No appointment found with number " + appointmentNo + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{appointmentNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAppointment(@PathParam("appointmentNo") String appointmentNo) {
        Appointment apt = appointmentDAO.getAppointmentByNo(appointmentNo);

        if (apt != null) {
            return Response.ok(apt).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"No appointment found with number " + appointmentNo + "\"}")
                    .build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAppointments() {
        List<Appointment> list = appointmentDAO.getAllAppointments();
        return Response.ok(list).build();
    }

    @PUT
    @Path("/{appointmentNo}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAppointment(@PathParam("appointmentNo") String appointmentNo, AppointmentRequest request) {

        if (request.getPatientName() == null || request.getPatientName().trim().isEmpty()
                || request.getContactNo() == null || request.getContactNo().trim().isEmpty()
                || request.getDentistId() <= 0 || request.getTreatmentId() <= 0
                || request.getAppointmentDate() == null || request.getAppointmentTime() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"patientName, contactNo, dentistId, treatmentId, appointmentDate and appointmentTime are required\"}")
                    .build();
        }

        String timeStr = request.getAppointmentTime().trim();
        if (timeStr.length() == 4) {
            timeStr = "0" + timeStr + ":00";
        } else if (timeStr.length() == 5) {
            timeStr = timeStr + ":00";
        }

        boolean updated = appointmentDAO.updateAppointment(
                appointmentNo,
                request.getPatientName(),
                request.getContactNo(),
                request.getAddress(),
                request.getDentistId(),
                request.getTreatmentId(),
                request.getAppointmentDate(),
                timeStr
        );

        if (updated) {
            Appointment apt = appointmentDAO.getAppointmentByNo(appointmentNo);
            return Response.ok(apt).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"No appointment found with number " + appointmentNo + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{appointmentNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAppointment(@PathParam("appointmentNo") String appointmentNo,
                                       @QueryParam("actingUsername") String actingUsername) {
        if (!isAdmin(actingUsername)) {
            return forbidden();
        }
        boolean deleted = appointmentDAO.deleteAppointment(appointmentNo);

        if (deleted) {
            return Response.ok("{\"message\":\"Appointment deleted successfully\"}").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"No appointment found with number " + appointmentNo + "\"}")
                    .build();
        }
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

    public static class AppointmentRequest {
        private String patientName;
        private String address;
        private String contactNo;
        private int dentistId;
        private int treatmentId;
        private String bookedByUsername;
        private String appointmentDate;
        private String appointmentTime;

        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getContactNo() { return contactNo; }
        public void setContactNo(String contactNo) { this.contactNo = contactNo; }

        public int getDentistId() { return dentistId; }
        public void setDentistId(int dentistId) { this.dentistId = dentistId; }

        public int getTreatmentId() { return treatmentId; }
        public void setTreatmentId(int treatmentId) { this.treatmentId = treatmentId; }

        public String getBookedByUsername() { return bookedByUsername; }
        public void setBookedByUsername(String bookedByUsername) { this.bookedByUsername = bookedByUsername; }

        public String getAppointmentDate() { return appointmentDate; }
        public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

        public String getAppointmentTime() { return appointmentTime; }
        public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    }

        public static class StatusUpdateRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}