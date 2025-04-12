package com.crime.reporting_system.repository;

import com.crime.reporting_system.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CaseRepository extends JpaRepository<Case, Long> {
    @Query("SELECT c FROM Case c WHERE c.status = :status")
    List<Case> findByStatus(String status);
}