package com.crime.reporting_system.service;

import com.crime.reporting_system.dto.ReportDTO;
import com.crime.reporting_system.controller.ReportController;
import com.crime.reporting_system.entity.Report;
import com.crime.reporting_system.entity.User;
import com.crime.reporting_system.repository.ReportRepository;
import com.crime.reporting_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public Report createReport(ReportDTO reportDTO, String username) {
        if (username == null) {
            throw new RuntimeException("Unauthorized: Please log in");
        }

        Report report = new Report();
        report.setTitle(reportDTO.getTitle());
        report.setDescription(reportDTO.getDescription());
        report.setLocation(reportDTO.getLocation());
        report.setReporterType(reportDTO.getReporterType());
        report.setVictimName(reportDTO.getVictimName());
        report.setIncidentDate(reportDTO.getIncidentDate());
        report.setStatus("pending");
        report.setCaseEntity(null);
        report.setAssignedOfficer(null);

        User currentUser = userRepository.findByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }
        report.setSubmittedBy(currentUser);

        return reportRepository.save(report);
    }

    public List<ReportController.ReportWithOfficer> getReportsForUser(String username) {
        if (username == null) {
            throw new RuntimeException("Unauthorized: Please log in");
        }

        List<Report> reports = reportRepository.findAll().stream()
                .filter(report -> report.getSubmittedBy() != null && report.getSubmittedBy().getUsername().equals(username))
                .collect(Collectors.toList());

        return reports.stream().map(report -> {
            ReportController.ReportWithOfficer rwo = new ReportController.ReportWithOfficer();
            rwo.setId(report.getId());
            rwo.setTitle(report.getTitle());
            rwo.setDescription(report.getDescription());
            rwo.setLocation(report.getLocation());
            rwo.setReporterType(report.getReporterType());
            rwo.setVictimName(report.getVictimName());
            rwo.setIncidentDate(report.getIncidentDate());
            rwo.setStatus(report.getStatus());
            rwo.setRejectionReason(report.getRejectionReason());
            if (report.getAssignedOfficer() != null) {
                rwo.setOfficerName(report.getAssignedOfficer().getFullName());
            } else {
                rwo.setOfficerName(null);
            }
            return rwo;
        }).collect(Collectors.toList());
    }

    public List<Report> getPendingReports() {
        return reportRepository.findByStatus("pending");
    }

    public List<Report> getRejectedReports() {
        return reportRepository.findByStatus("rejected");
    }

    public Report updateReport(Long reportId, Report reportUpdate) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
        if ("rejected".equals(report.getStatus())) {
            throw new RuntimeException("Cannot edit rejected reports");
        }
        if (report.getCaseEntity() != null) {
            report.setTitle(reportUpdate.getTitle());
            report.setDescription(reportUpdate.getDescription());
            report.setLocation(reportUpdate.getLocation());
            report.setReporterType(reportUpdate.getReporterType());
            report.setVictimName(reportUpdate.getVictimName());
            report.setIncidentDate(reportUpdate.getIncidentDate());
            return reportRepository.save(report);
        }
        throw new RuntimeException("Cannot edit unassigned reports");
    }

    public List<User> getOfficers() {
        return userRepository.findAll().stream()
                .filter(user -> "POLICE".equals(user.getRole()))
                .collect(Collectors.toList());
    }
}