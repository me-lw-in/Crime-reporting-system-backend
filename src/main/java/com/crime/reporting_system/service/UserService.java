package com.crime.reporting_system.service;

import com.crime.reporting_system.entity.User;
import com.crime.reporting_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String password, String role, String fullName, String phoneNumber, String address) {
        System.out.println("Entering registerUser for: " + username); // Debug
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Username already exists: " + username);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        System.out.println("Before save: " + user); // Debug
        try {
            User savedUser = userRepository.save(user);
            System.out.println("After save: " + savedUser); // Debug
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            System.out.println("Caught DataIntegrityViolationException: " + e.getMessage());
            throw new RuntimeException("Username already exists: " + username, e);
        }
    }
}