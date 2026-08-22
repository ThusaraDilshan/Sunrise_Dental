/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;

/**
 * Patient entity - NOT a login user. Patients are just data records
 * created by Staff when registering a new appointment.
 */
public class Patient implements Serializable {

    private int patientId;
    private String patientName;
    private String address;
    private String contactNo;

    public Patient() {
    }

    public Patient(int patientId, String patientName, String address, String contactNo) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.address = address;
        this.contactNo = contactNo;
    }

    // Used when creating a new patient record (no ID yet)
    public Patient(String patientName, String address, String contactNo) {
        this.patientName = patientName;
        this.address = address;
        this.contactNo = contactNo;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", patientName='" + patientName + '\'' +
                ", contactNo='" + contactNo + '\'' +
                '}';
    }
}