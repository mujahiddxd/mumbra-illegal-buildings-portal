package com.mumbra.illegalbuildings.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mumbra.illegalbuildings.model.Admin;
import com.mumbra.illegalbuildings.payload.LoginRequest;
import com.mumbra.illegalbuildings.repository.AdminRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
        
    @PostMapping("/admin/login")
    public ResponseEntity<?> authenticateAdmin(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // No JWT, just return success
            return ResponseEntity.ok("Login successful");
        } catch (AuthenticationException e) {
            System.out.println("Authentication failed for user: " + loginRequest.getUsername());
            e.printStackTrace();
            return ResponseEntity.status(401).body("Invalid username/password");
        }
    }
    
    @GetMapping("/admin/create")
    public ResponseEntity<?> createAdmin() {
        try {
            if (!adminRepository.existsByUsername("admin")) {
                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                adminRepository.save(admin);
                return ResponseEntity.ok("Admin user created successfully");
            } else {
                return ResponseEntity.ok("Admin user already exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating admin user: " + e.getMessage());
        }
    }
    
    @GetMapping("/admin/reset")
    public ResponseEntity<?> resetAdmin() {
        try {
            // Delete existing admin
            adminRepository.findByUsername("admin").ifPresent(admin -> {
                adminRepository.delete(admin);
            });
            
            // Create new admin
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            adminRepository.save(admin);
            
            return ResponseEntity.ok("Admin user reset successfully. Username: admin, Password: admin123");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error resetting admin user: " + e.getMessage());
        }
    }
    
    @GetMapping("/admin/debug")
    public ResponseEntity<?> debugAdmin() {
        try {
            Admin admin = adminRepository.findByUsername("admin").orElse(null);
            if (admin != null) {
                return ResponseEntity.ok("Admin found - ID: " + admin.getId() + 
                                       ", Username: " + admin.getUsername() + 
                                       ", Role: " + admin.getRole() + 
                                       ", Password starts with: " + admin.getPassword().substring(0, 10) + "...");
            } else {
                return ResponseEntity.ok("Admin user not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error debugging admin user: " + e.getMessage());
        }
    }
    
    @GetMapping("/admin/check")
    public ResponseEntity<?> checkAuthentication() {
        // This endpoint requires authentication, so if we reach here, user is authenticated
        return ResponseEntity.ok("Authenticated");
    }
}