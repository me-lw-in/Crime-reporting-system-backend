package com.crime.reporting_system.controller;

import com.crime.reporting_system.response.MessageResponse;
import com.crime.reporting_system.dto.UserDTO;
import com.crime.reporting_system.response.ErrorResponse;
import com.crime.reporting_system.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.sql.SQLIntegrityConstraintViolationException;
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
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().body(new MessageResponse("Logged out successfully"));
    }

    @GetMapping("/officers")
    public ResponseEntity<?> getOfficers() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String currentUsername;
            if (principal instanceof UserDetails) {
                currentUsername = ((UserDetails) principal).getUsername();
            } else {
                currentUsername = principal.toString();
            }

            List<UserDTO> officers = userService.getAllOfficers()
                    .stream()
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