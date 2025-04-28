package com.crime.reporting_system;

public class ReportDTO {
    private String title;
    private String description;
    private String location;
    private String reporterType;
    private String victimName;
    private String incidentDate;
    private String rejectionReason;

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
}