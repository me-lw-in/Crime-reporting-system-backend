package com.crime.reporting_system.controller;

import com.crime.reporting_system.entity.Report;
import com.crime.reporting_system.entity.User;
import com.crime.reporting_system.repository.ReportRepository;
import com.crime.reporting_system.repository.UserRepository;
import com.crime.reporting_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Report> submitReport(@RequestBody Report report, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
        report.setStatus("pending"); // Set default status
        report.setCaseEntity(null); // Ensure caseId is null initially
        report.setAssignedOfficer(null); // Ensure assignedOfficer is null initially
        User currentUser = userRepository.findByUsername(authentication.getName()); // Get submitting user
        report.setSubmittedBy(currentUser); // Set the submitting user
        Report savedReport = reportRepository.save(report);
        return ResponseEntity.ok(savedReport);
    }

    @GetMapping("/pending")
    public ResponseEntity<Iterable<Report>> getPendingReports() {
        return ResponseEntity.ok(reportRepository.findByStatus("pending"));
    }

    @GetMapping("/rejected")
    public ResponseEntity<Iterable<Report>> getRejectedReports() {
        return ResponseEntity.ok(reportRepository.findByStatus("rejected"));
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<Report> updateReport(@PathVariable Long reportId, @RequestBody Report reportUpdate) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
        if ("rejected".equals(report.getStatus())) {
            return ResponseEntity.status(403).body(null); // Forbidden - Cannot edit rejected reports
        }
        if (report.getCaseEntity() != null) {
            report.setTitle(reportUpdate.getTitle());
            report.setDescription(reportUpdate.getDescription());
            report.setLocation(reportUpdate.getLocation());
            report.setReporterType(reportUpdate.getReporterType());
            report.setVictimName(reportUpdate.getVictimName());
            report.setIncidentDate(reportUpdate.getIncidentDate());
            return ResponseEntity.ok(reportRepository.save(report));
        }
        return ResponseEntity.status(403).body(null); // Forbidden - Cannot edit unassigned reports
    }

    @GetMapping("/officers")
    public ResponseEntity<List<User>> getOfficers() {
        List<User> officers = userRepository.findAll().stream()
                .filter(user -> "POLICE".equals(user.getRole()))
                .toList();
        return ResponseEntity.ok(officers);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReportWithOfficer>> getAllReports(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
        String currentUsername = authentication.getName();
        List<Report> reports = reportRepository.findAll().stream()
                .filter(report -> report.getSubmittedBy() != null && report.getSubmittedBy().getUsername().equals(currentUsername))
                .collect(Collectors.toList());
        List<ReportWithOfficer> response = reports.stream().map(report -> {
            ReportWithOfficer rwo = new ReportWithOfficer();
            rwo.setId(report.getId());
            rwo.setTitle(report.getTitle());
            rwo.setDescription(report.getDescription());
            rwo.setLocation(report.getLocation());
            rwo.setReporterType(report.getReporterType());
            rwo.setVictimName(report.getVictimName());
            rwo.setIncidentDate(report.getIncidentDate());
            rwo.setStatus(report.getStatus());
            if (report.getAssignedOfficer() != null) {
                rwo.setOfficerName(report.getAssignedOfficer().getFullName());
            } else {
                rwo.setOfficerName(null);
            }
            return rwo;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            userService.registerUser(
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getAddress()
            );
            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Inner class to include officer name
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
        public String getOfficerName() { return officerName; }
        public void setOfficerName(String officerName) { this.officerName = officerName; }
    }
}