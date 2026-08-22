package model;

import java.io.Serializable;

public class Dentist implements Serializable {

    private int dentistId;
    private String dentistName;
    private String username;
    private String password;
    private String specialization;
    private String contactNo;
    private double consultationFee;

    public Dentist() {
    }

    public Dentist(int dentistId, String dentistName, String username, String password,
                    String specialization, String contactNo, double consultationFee) {
        this.dentistId = dentistId;
        this.dentistName = dentistName;
        this.username = username;
        this.password = password;
        this.specialization = specialization;
        this.contactNo = contactNo;
        this.consultationFee = consultationFee;
    }

    public Dentist(String dentistName, String username, String password,
                    String specialization, String contactNo, double consultationFee) {
        this.dentistName = dentistName;
        this.username = username;
        this.password = password;
        this.specialization = specialization;
        this.contactNo = contactNo;
        this.consultationFee = consultationFee;
    }

    public int getDentistId() {
        return dentistId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public String getDentistName() {
        return dentistName;
    }

    public void setDentistName(String dentistName) {
        this.dentistName = dentistName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    @Override
    public String toString() {
        return "Dentist{" +
                "dentistId=" + dentistId +
                ", dentistName='" + dentistName + '\'' +
                ", username='" + username + '\'' +
                ", specialization='" + specialization + '\'' +
                ", contactNo='" + contactNo + '\'' +
                ", consultationFee=" + consultationFee +
                '}';
    }
}