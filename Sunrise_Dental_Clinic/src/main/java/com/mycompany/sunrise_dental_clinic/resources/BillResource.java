package com.mycompany.sunrise_dental_clinic.resources;

import dao.BillDAO;
import model.Bill;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/bill")
public class BillResource {

    private final BillDAO billDAO = new BillDAO();

    @POST
    @Path("/{appointmentNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateBill(@PathParam("appointmentNo") String appointmentNo) {
        // This actually calculates AND saves the bill row in the "bills" table
        // (or returns the existing one if it was already billed).
        Bill bill = billDAO.calculateAndSaveBill(appointmentNo);

        if (bill == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Appointment not found or billing failed\"}")
                    .build();
        }

        BillResponse response = new BillResponse(
                bill.getAppointmentNo(),
                bill.getPatientName(),
                bill.getContactNo(),
                bill.getDentistName(),
                bill.getTreatmentName(),
                bill.getAppointmentDate(),
                bill.getAppointmentTime(),
                bill.getTreatmentCost(),
                bill.getConsultationFee(),
                bill.getTotalAmount()
        );

        return Response.ok(response).build();
    }

    public static class BillResponse {
        public String appointmentNo;
        public String patientName;
        public String contactNo;
        public String dentistName;
        public String treatmentName;
        public String appointmentDate;
        public String appointmentTime;
        public java.math.BigDecimal treatmentCost;
        public java.math.BigDecimal consultationFee;
        public java.math.BigDecimal totalAmount;

        public BillResponse(String appointmentNo, String patientName, String contactNo, String dentistName, String treatmentName, String appointmentDate, String appointmentTime, java.math.BigDecimal treatmentCost, java.math.BigDecimal consultationFee, java.math.BigDecimal totalAmount) {
            this.appointmentNo = appointmentNo;
            this.patientName = patientName;
            this.contactNo = contactNo;
            this.dentistName = dentistName;
            this.treatmentName = treatmentName;
            this.appointmentDate = appointmentDate;
            this.appointmentTime = appointmentTime;
            this.treatmentCost = treatmentCost;
            this.consultationFee = consultationFee;
            this.totalAmount = totalAmount;
        }
    }
}