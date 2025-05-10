package com.crime.reporting_system.dto;

public class UserDTO {
    private Long id; // Added id field
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String phoneNumber;
    private String address;

    // Getters and Setters
    public Long getId() { return id; } // Added getter
    public void setId(Long id) { this.id = id; } // Added setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}