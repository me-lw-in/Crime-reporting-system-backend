package com.crime.reporting_system.controller;

import com.crime.reporting_system.MessageResponse;
import com.crime.reporting_system.ErrorResponse;
import com.crime.reporting_system.entity.Case;
import com.crime.reporting_system.entity.Report;
import com.crime.reporting_system.entity.User;
import com.crime.reporting_system.repository.CaseRepository;
import com.crime.reporting_system.repository.ReportRepository;
import com.crime.reporting_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getOfficerDashboard() {
        try {
            List<Case> cases = caseRepository.findAllWithReports(); // Use a custom query or EntityGraph

            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to fetch dashboard: " + e.getMessage(), "general"));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCases() {
        try {
            List<Case> cases = caseRepository.findAll();
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to fetch cases", "general"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCase(@RequestBody Case caseDTO) {
        try {
            if (caseDTO.getCaseNumber() == null || caseDTO.getCaseNumber().isEmpty()) {
                caseDTO.setCaseNumber("CASE-" + (caseRepository.count() + 1));
            }
            Case savedCase = caseRepository.save(caseDTO);
            return ResponseEntity.ok(savedCase);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to create case", "general"));
        }
    }

    @PostMapping("/reports/{reportId}/accept")
    public ResponseEntity<?> acceptReport(@PathVariable Long reportId, @RequestParam(required = false) Long caseId) {
        try {
            Optional<Report> reportOpt = reportRepository.findById(reportId);
            if (!reportOpt.isPresent()) {
                return ResponseEntity.status(404).body(new ErrorResponse("Report not found", "reportId"));
            }

            Report report = reportOpt.get();
            if (!"pending".equalsIgnoreCase(report.getStatus())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Report is not in pending status", "status"));
            }

            // Get the authenticated officer
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }
            User officer = userRepository.findByUsername(username);
            if (officer == null || !"POLICE".equalsIgnoreCase(officer.getRole())) {
                return ResponseEntity.status(403).body(new ErrorResponse("Only officers can accept reports", "general"));
            }

            Case caseEntity;
            if (caseId != null) {
                Optional<Case> caseOpt = caseRepository.findById(caseId);
                if (!caseOpt.isPresent()) {
                    return ResponseEntity.status(404).body(new ErrorResponse("Case not found", "caseId"));
                }
                caseEntity = caseOpt.get();
            } else {
                caseEntity = new Case();
                caseEntity.setTitle(report.getTitle());
                caseEntity.setDescription(report.getDescription());
                caseEntity.setStatus("investigating");
                caseEntity.setCaseNumber("CASE-" + (caseRepository.count() + 1));
                caseEntity.setAssignedOfficer(officer); // Assign the officer to the case
                caseEntity = caseRepository.save(caseEntity);
            }

            // Update the report
            report.setCaseEntity(caseEntity);
            report.setStatus("investigating"); // Changed from "accepted" to "investigating"
            report.setAssignedOfficer(officer); // Assign the officer to the report
            reportRepository.save(report);

            return ResponseEntity.ok(new MessageResponse("Report accepted and linked to case"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to accept report: " + e.getMessage(), "general"));
        }
    }

    @PostMapping("/reports/{reportId}/reject")
    public ResponseEntity<?> rejectReport(@PathVariable Long reportId, @RequestParam String rejectionReason) {
        try {
            Optional<Report> reportOpt = reportRepository.findById(reportId);
            if (!reportOpt.isPresent()) {
                return ResponseEntity.status(404).body(new ErrorResponse("Report not found", "reportId"));
            }

            Report report = reportOpt.get();
            if (!"pending".equalsIgnoreCase(report.getStatus())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Report is not in pending status", "status"));
            }

            report.setStatus("rejected");
            report.setRejectionReason(rejectionReason);
            reportRepository.save(report);

            return ResponseEntity.ok(new MessageResponse("Report rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to reject report", "general"));
        }
    }

    @PutMapping("/{caseId}/reassign")
    public ResponseEntity<?> reassignCase(@PathVariable Long caseId, @RequestParam Long officerId) {
        try {
            Optional<Case> caseOpt = caseRepository.findById(caseId);
            if (!caseOpt.isPresent()) {
                return ResponseEntity.status(404).body(new ErrorResponse("Case not found", "caseId"));
            }

            Optional<User> officerOpt = userRepository.findById(officerId);
            if (!officerOpt.isPresent()) {
                return ResponseEntity.status(404).body(new ErrorResponse("POLICE not found", "officerId"));
            }

            User officer = officerOpt.get();
            if (!"POLICE".equalsIgnoreCase(officer.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("User is not an POLICE", "officerId"));
            }

            Case caseEntity = caseOpt.get();
            caseEntity.setAssignedOfficer(officer);
            caseRepository.save(caseEntity);

            return ResponseEntity.ok(new MessageResponse("Case reassigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to reassign case", "general"));
        }
    }

    @PutMapping("/{caseId}/status")
    public ResponseEntity<?> updateCaseStatus(@PathVariable Long caseId, @RequestParam String status) {
        try {
            Optional<Case> caseOpt = caseRepository.findById(caseId);
            if (!caseOpt.isPresent()) {
                return ResponseEntity.status(404).body(new ErrorResponse("Case not found", "caseId"));
            }

            Case caseEntity = caseOpt.get();
            if (!"investigating".equalsIgnoreCase(caseEntity.getStatus()) && !"resolved".equalsIgnoreCase(status)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid status transition", "status"));
            }

            caseEntity.setStatus(status);
            caseRepository.save(caseEntity);

            return ResponseEntity.ok(new MessageResponse("Case status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to update case status: " + e.getMessage(), "general"));
        }
    }
}