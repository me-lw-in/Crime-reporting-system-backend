package com.crime.reporting_system.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cases")
public class Case {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String caseNumber;
    private String title; // New field for case title
    private String description;
    private String status; // e.g., "investigating", "resolved"

    @ManyToOne
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer; // Foreign key to users

    @ManyToMany
    @JoinTable(
            name = "case_reports",
            joinColumns = @JoinColumn(name = "case_id"),
            inverseJoinColumns = @JoinColumn(name = "report_id")
    )
    private Set<Report> reports = new HashSet<>();

    // Constructors
    public Case() {}

    public Case(String caseNumber, String title, String description, String status, User assignedOfficer) {
        this.caseNumber = caseNumber;
        this.title = title;
        this.description = description;
        this.status = status;
        this.assignedOfficer = assignedOfficer;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getAssignedOfficer() { return assignedOfficer; }
    public void setAssignedOfficer(User assignedOfficer) { this.assignedOfficer = assignedOfficer; }
    public Set<Report> getReports() { return reports; }
    public void setReports(Set<Report> reports) { this.reports = reports; }
}