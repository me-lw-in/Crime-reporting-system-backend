package com.crime.reporting_system.controller;

import com.crime.reporting_system.MessageResponse;
import com.crime.reporting_system.UserDTO;
import com.crime.reporting_system.ErrorResponse;
import com.crime.reporting_system.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.sql.SQLIntegrityConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        try {
            userService.registerUser(
                    userDTO.getUsername(),
                    userDTO.getPassword(),
                    userDTO.getRole(),
                    userDTO.getFullName(),
                    userDTO.getPhoneNumber(),
                    userDTO.getAddress()
            );
            return ResponseEntity.ok(new MessageResponse("User registered successfully"));
        } catch (DataIntegrityViolationException e) {
            String field = e.getCause() instanceof SQLIntegrityConstraintViolationException
                    ? "username" : "general";
            return ResponseEntity.badRequest().body(new ErrorResponse("Username already exists", field));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Registration failed", "general"));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            com.crime.reporting_system.entity.User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(404).body(new ErrorResponse("User not found", "general"));
            }

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(user.getUsername());
            userDTO.setFullName(user.getFullName());
            userDTO.setRole(user.getRole());
            userDTO.setPhoneNumber(user.getPhoneNumber());
            userDTO.setAddress(user.getAddress());

            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to fetch current user", "general"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().body("Logged out successfully");
    }

    @GetMapping("/officers")
    public ResponseEntity<?> getOfficers() {
        try {
            // Get the authenticated user's username
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String currentUsername;
            if (principal instanceof UserDetails) {
                currentUsername = ((UserDetails) principal).getUsername();
            } else {
                currentUsername = principal.toString();
            }

            List<UserDTO> officers = userService.getAllOfficers()
                    .stream()
                    .filter(user -> !user.getUsername().equals(currentUsername)) // Exclude the logged-in user
                    .map(user -> {
                        UserDTO userDTO = new UserDTO();
                        userDTO.setId(user.getId());
                        userDTO.setUsername(user.getUsername());
                        userDTO.setFullName(user.getFullName());
                        userDTO.setRole(user.getRole());
                        userDTO.setPhoneNumber(user.getPhoneNumber());
                        userDTO.setAddress(user.getAddress());
                        return userDTO;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(officers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to fetch officers: " + e.getMessage(), "general"));
        }
    }
}