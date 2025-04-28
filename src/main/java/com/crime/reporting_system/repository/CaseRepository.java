package com.crime.reporting_system.repository;

import com.crime.reporting_system.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CaseRepository extends JpaRepository<Case, Long> {
    @EntityGraph(attributePaths = {"reports", "assignedOfficer"})
    @Query("SELECT c FROM Case c")
    List<Case> findAllWithReports();
}