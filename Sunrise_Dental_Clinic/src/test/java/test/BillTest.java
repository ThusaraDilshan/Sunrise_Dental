package test;

import model.Bill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BillTest {

    private Bill bill;

    @BeforeEach
    void setUp() {
        bill = new Bill();
    }

    @Test
    @DisplayName("Test Bill Getters and Setters for Core Fields")
    void testCoreFieldsGettersAndSetters() {
        bill.setBillId(1);
        bill.setAppointmentNo("APT0001");
        bill.setTreatmentCost(new BigDecimal("45000.00"));
        bill.setConsultationFee(new BigDecimal("2000.00"));
        bill.setTotalAmount(new BigDecimal("47000.00"));
        bill.setBillDate("2026-08-19 15:53:12");

        assertEquals(1, bill.getBillId());
        assertEquals("APT0001", bill.getAppointmentNo());
        assertEquals(new BigDecimal("45000.00"), bill.getTreatmentCost());
        assertEquals(new BigDecimal("2000.00"), bill.getConsultationFee());
        assertEquals(new BigDecimal("47000.00"), bill.getTotalAmount());
        assertEquals("2026-08-19 15:53:12", bill.getBillDate());
    }

    @Test
    @DisplayName("Test Bill Extra Display Fields for Receipt")
    void testDisplayFieldsGettersAndSetters() {
        bill.setPatientName("Mr. Kamal Perera");
        bill.setContactNo("071846987");
        bill.setAddress("58/18/A, Swarna road, Colombo");
        bill.setDentistName("Dr. Shevon De Silva");
        bill.setTreatmentName("Braces Fitting");
        bill.setAppointmentDate("2026-08-22");
        bill.setAppointmentTime("10:25:00");

        assertEquals("Mr. Kamal Perera", bill.getPatientName());
        assertEquals("071846987", bill.getContactNo());
        assertEquals("58/18/A, Swarna road, Colombo", bill.getAddress());
        assertEquals("Dr. Shevon De Silva", bill.getDentistName());
        assertEquals("Braces Fitting", bill.getTreatmentName());
        assertEquals("2026-08-22", bill.getAppointmentDate());
        assertEquals("10:25:00", bill.getAppointmentTime());
    }

    @ParameterizedTest
    @DisplayName("Verify Total Amount Calculation based on Database Dump Data")
    @CsvSource({
        "1, APT0001, 45000.00, 2000.00, 47000.00",
        "2, APT0002,  4500.00, 2000.00,  6500.00",
        "4, APT0005, 45000.00, 8700.00, 53700.00"
    })
    void testBillCalculationWithDbData(int billId, String appNo, String treatmentCost, String consultationFee, String expectedTotal) {
        bill.setBillId(billId);
        bill.setAppointmentNo(appNo);
        bill.setTreatmentCost(new BigDecimal(treatmentCost));
        bill.setConsultationFee(new BigDecimal(consultationFee));

        BigDecimal actualTotal = bill.getTreatmentCost().add(bill.getConsultationFee());
        bill.setTotalAmount(actualTotal);

        assertEquals(new BigDecimal(expectedTotal), bill.getTotalAmount(),
                "Bill ID " + billId + " එකෙහි Total Amount එක එකතුවට සමාන විය යුතුය.");
    }
}