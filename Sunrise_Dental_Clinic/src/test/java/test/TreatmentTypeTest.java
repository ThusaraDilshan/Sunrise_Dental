package test;

import model.TreatmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TreatmentTypeTest {

    private TreatmentType treatmentType;

    @BeforeEach
    void setUp() {
        treatmentType = new TreatmentType();
    }

    @Test
    @DisplayName("Test Full Constructor with BigDecimal Cost")
    void testFullConstructor() {
        TreatmentType fullTreatment = new TreatmentType(1, "Tooth Filling", new BigDecimal("8000.00"));

        assertEquals(1, fullTreatment.getTreatmentId());
        assertEquals("Tooth Filling", fullTreatment.getTreatmentName());
        assertEquals(new BigDecimal("8000.00"), fullTreatment.getTreatmentCost());
    }

    /**
     * Database dump එකේ ඇති treatment_types table එකේ සැබෑ දත්ත පරීක්ෂා කිරීම:
     * ID: 1 | Tooth Filling           | Cost:  8000.00
     * ID: 2 | Tooth Extraction        | Cost:  9500.00
     * ID: 3 | Root Canal Treatment    | Cost: 15000.00
     * ID: 4 | Teeth Cleaning (Scaling)| Cost:  5500.00
     * ID: 5 | Braces Fitting          | Cost: 45000.00
     * ID: 6 | Inlays and onlays       | Cost: 12500.00
     */
    @ParameterizedTest
    @DisplayName("Verify Treatment Types Data from Database Dump")
    @CsvSource({
        "1, Tooth Filling, 8000.00",
        "2, Tooth Extraction, 9500.00",
        "3, Root Canal Treatment, 15000.00",
        "4, Teeth Cleaning (Scaling), 5500.00",
        "5, Braces Fitting, 45000.00",
        "6, Inlays and onlays, 12500.00"
    })
    void testTreatmentTypeDataFromDump(int treatmentId, String name, String cost) {
        TreatmentType dbTreatment = new TreatmentType(treatmentId, name, new BigDecimal(cost));

        assertEquals(treatmentId, dbTreatment.getTreatmentId());
        assertEquals(name, dbTreatment.getTreatmentName());
        assertEquals(new BigDecimal(cost), dbTreatment.getTreatmentCost());
    }

    @Test
    @DisplayName("Test Getters and Setters")
    void testGettersAndSetters() {
        treatmentType.setTreatmentId(10);
        treatmentType.setTreatmentName("Teeth Whitening");
        treatmentType.setTreatmentCost(new BigDecimal("20000.00"));

        assertEquals(10, treatmentType.getTreatmentId());
        assertEquals("Teeth Whitening", treatmentType.getTreatmentName());
        assertEquals(new BigDecimal("20000.00"), treatmentType.getTreatmentCost());
    }
}