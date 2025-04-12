package com.crime.reporting_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;
    private String reporterType; // e.g., "victim", "witness"
    private String victimName;
    private String incidentDate;
    private String status; // e.g., "pending", "investigating", "rejected", "resolved"

    @ManyToOne
    @JoinColumn(name = "case_id")
    private Case caseEntity; // Foreign key to cases

    @ManyToOne
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer; // Foreign key to users

    @ManyToOne
    @JoinColumn(name = "submitted_by_id")
    private User submittedBy; // New field to track the submitting user

    // Constructors
    public Report() {}

    public Report(String title, String description, String location, String reporterType, String victimName, String incidentDate, String status) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.reporterType = reporterType;
        this.victimName = victimName;
        this.incidentDate = incidentDate;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getReporterType() { return reporterType; }
    public void setReporterType(String reporterType) { this.reporterType = reporterType; }
    public String getVictimName() { return victimName; }
    public void setVictimName(String victimName) { this.victimName = victimName; }
    public String getIncidentDate() { return incidentDate; }
    public void setIncidentDate(String incidentDate) { this.incidentDate = incidentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Case getCaseEntity() { return caseEntity; }
    public void setCaseEntity(Case caseEntity) { this.caseEntity = caseEntity; }
    public User getAssignedOfficer() { return assignedOfficer; }
    public void setAssignedOfficer(User assignedOfficer) { this.assignedOfficer = assignedOfficer; }
    public User getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(User submittedBy) { this.submittedBy = submittedBy; }
}