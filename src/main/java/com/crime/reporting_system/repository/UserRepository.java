package com.crime.reporting_system.repository;

import com.crime.reporting_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username); // Custom method
    List<User> findByRole(String role);
}