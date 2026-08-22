/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;

public class Staff implements Serializable {

    private int staffId;
    private String staffName;
    private String username;
    private String password;
    private String contactNo;
    private String role;
    private String createdAt;

    public Staff() {
    }

    public Staff(int staffId, String staffName, String username, String password,
                 String contactNo, String role) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.username = username;
        this.password = password;
        this.contactNo = contactNo;
        this.role = role;
    }

    public Staff(String staffName, String username, String password,
                 String contactNo, String role) {
        this.staffName = staffName;
        this.username = username;
        this.password = password;
        this.contactNo = contactNo;
        this.role = role;
    }

    // Used by StaffDAO.findByUsername() - includes password + createdAt
    public Staff(int staffId, String staffName, String username, String password,
                 String contactNo, String role, String createdAt) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.username = username;
        this.password = password;
        this.contactNo = contactNo;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
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

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "staffId=" + staffId +
                ", staffName='" + staffName + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}