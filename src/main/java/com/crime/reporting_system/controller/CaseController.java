package com.crime.reporting_system.controller;

import com.crime.reporting_system.entity.Case;
import com.crime.reporting_system.entity.Report;
import com.crime.reporting_system.entity.User;
import com.crime.reporting_system.repository.CaseRepository;
import com.crime.reporting_system.repository.ReportRepository;
import com.crime.reporting_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Case> createCase(@RequestBody Case caseObj) {
        if (caseObj.getReports() == null) {
            caseObj.setReports(new HashSet<>());
        }
        User currentUser = getCurrentUser();
        caseObj.setAssignedOfficer(currentUser); // Set officer as foreign key
        caseObj.setStatus("investigating"); // Default status for new case
        Case savedCase = caseRepository.save(caseObj);
        return ResponseEntity.ok(savedCase);
    }

    @PostMapping("/link")
    public ResponseEntity<Report> linkReportToCase(@RequestParam Long reportId, @RequestParam Long caseId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
        Case caseObj = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + caseId));
        if ("resolved".equals(caseObj.getStatus())) {
            return ResponseEntity.status(400).body(null); // Bad Request - Cannot link to resolved case
        }
        report.setCaseEntity(caseObj);
        report.setAssignedOfficer(caseObj.getAssignedOfficer()); // Ensure consistency
        report.setStatus("investigating");
        caseObj.getReports().add(report);
        reportRepository.save(report);
        caseRepository.save(caseObj);
        return ResponseEntity.ok(report);
    }

    @PutMapping("/{caseId}")
    public ResponseEntity<Case> updateCase(@PathVariable Long caseId, @RequestBody Case caseUpdate) {
        Case caseObj = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + caseId));
        if (caseUpdate.getStatus() != null) {
            caseObj.setStatus(caseUpdate.getStatus()); // Update status (e.g., to "resolved")
            for (Report report : caseObj.getReports()) {
                report.setStatus(caseUpdate.getStatus());
                reportRepository.save(report);
            }
        }
        if (caseUpdate.getAssignedOfficer() != null && caseUpdate.getAssignedOfficer().getId() != null) {
            User newOfficer = userRepository.findById(caseUpdate.getAssignedOfficer().getId())
                    .orElseThrow(() -> new RuntimeException("Officer not found"));
            caseObj.setAssignedOfficer(newOfficer); // Allow reassignment
            for (Report report : caseObj.getReports()) {
                report.setAssignedOfficer(newOfficer);
                reportRepository.save(report);
            }
        }
        Case updatedCase = caseRepository.save(caseObj);
        return ResponseEntity.ok(updatedCase);
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<Case>> getAllCases() {
        return ResponseEntity.ok(caseRepository.findAll()); // Comprehensive list for linking
    }

    @GetMapping("/filter")
    public ResponseEntity<Iterable<Case>> getCasesByStatus(@RequestParam String status) {
        return ResponseEntity.ok(caseRepository.findByStatus(status));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<CaseDashboard>> getDashboardCases() {
        List<Case> cases = caseRepository.findAll();
        List<CaseDashboard> response = cases.stream().map(caze -> {
            CaseDashboard cd = new CaseDashboard();
            cd.setId(caze.getId());
            cd.setCaseNumber(caze.getCaseNumber());
            cd.setTitle(caze.getTitle());
            cd.setDescription(caze.getDescription());
            cd.setStatus(caze.getStatus());
            if (caze.getAssignedOfficer() != null) {
                cd.setOfficerName(caze.getAssignedOfficer().getFullName());
            } else {
                cd.setOfficerName(null);
            }
            long reportCount = caze.getReports().size();
            cd.setReportCount(reportCount);
            return cd;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reports/{reportId}/reject")
    public ResponseEntity<Report> rejectReport(@PathVariable Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
        report.setStatus("rejected");
        Report updatedReport = reportRepository.save(report);
        return ResponseEntity.ok(updatedReport);
    }

    @PostMapping("/reports/{reportId}/accept")
    public ResponseEntity<?> acceptReport(@PathVariable Long reportId, @RequestParam(required = false) Long caseId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
        User currentOfficer = getCurrentUser();
        report.setAssignedOfficer(currentOfficer);
        report.setStatus("investigating");

        if (caseId != null) {
            Case caseObj = caseRepository.findById(caseId)
                    .orElseThrow(() -> new RuntimeException("Case not found with id: " + caseId));
            if ("resolved".equals(caseObj.getStatus())) {
                return ResponseEntity.status(400).body(null); // Bad Request - Cannot link to resolved case
            }
            report.setCaseEntity(caseObj);
            report.setAssignedOfficer(caseObj.getAssignedOfficer()); // Ensure consistency
            caseObj.getReports().add(report);
            caseRepository.save(caseObj);
        } else {
            Case newCase = new Case();
            newCase.setCaseNumber("CASE-" + (caseRepository.count() + 1));
            newCase.setTitle(report.getTitle());
            newCase.setDescription(report.getDescription());
            newCase.setStatus("investigating");
            newCase.setAssignedOfficer(currentOfficer);
            newCase.getReports().add(report);
            report.setCaseEntity(newCase);
            caseRepository.save(newCase);
        }
        return ResponseEntity.ok(reportRepository.save(report));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return user;
    }

    // Inner class for dashboard response (updated with reportCount)
    public static class CaseDashboard {
        private Long id;
        private String caseNumber;
        private String title;
        private String description;
        private String status;
        private String officerName;
        private long reportCount;

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
        public String getOfficerName() { return officerName; }
        public void setOfficerName(String officerName) { this.officerName = officerName; }
        public long getReportCount() { return reportCount; }
        public void setReportCount(long reportCount) { this.reportCount = reportCount; }
    }
}