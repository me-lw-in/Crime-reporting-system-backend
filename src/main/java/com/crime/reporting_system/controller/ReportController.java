package com.crime.reporting_system.controller;

import com.crime.reporting_system.response.ErrorResponse;
import com.crime.reporting_system.response.MessageResponse;
import com.crime.reporting_system.dto.ReportDTO;
import com.crime.reporting_system.entity.Report;
import com.crime.reporting_system.entity.User;
import com.crime.reporting_system.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<?> submitReport(@RequestBody ReportDTO reportDTO) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }
            reportService.createReport(reportDTO, username);
            return ResponseEntity.ok(new MessageResponse("Report submitted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse(e.getMessage(), "authentication"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Failed to submit report: " + e.getMessage(), "general"));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingReports() {
        try {
            List<Report> reports = reportService.getPendingReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to fetch pending reports: " + e.getMessage(), "general"));
        }
    }

    @GetMapping("/rejected")
    public ResponseEntity<?> getRejectedReports() {
        try {
            List<Report> reports = reportService.getRejectedReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to fetch rejected reports: " + e.getMessage(), "general"));
        }
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(@PathVariable Long reportId, @RequestBody Report reportUpdate) {
        try {
            Report updatedReport = reportService.updateReport(reportId, reportUpdate);
            return ResponseEntity.ok(updatedReport);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403)
                    .body(new ErrorResponse(e.getMessage(), "report"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to update report: " + e.getMessage(), "general"));
        }
    }

    @GetMapping("/officers")
    public ResponseEntity<?> getOfficers() {
        try {
            List<User> officers = reportService.getOfficers();
            return ResponseEntity.ok(officers);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to fetch officers: " + e.getMessage(), "general"));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllReports() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }
            List<ReportController.ReportWithOfficer> reports = reportService.getReportsForUser(username);
            return ResponseEntity.ok(reports);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse(e.getMessage(), "authentication"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to fetch reports: " + e.getMessage(), "general"));
        }
    }

    public static class ReportWithOfficer {
        private Long id;
        private String title;
        private String description;
        private String location;
        private String reporterType;
        private String victimName;
        private String incidentDate;
        private String status;
        private String officerName;
        private String rejectionReason;

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
        public String getOfficerName() { return officerName; }
        public void setOfficerName(String officerName) { this.officerName = officerName; }
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }
}