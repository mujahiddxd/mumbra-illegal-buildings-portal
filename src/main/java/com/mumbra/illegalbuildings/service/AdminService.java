package com.mumbra.illegalbuildings.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mumbra.illegalbuildings.model.Admin;
import com.mumbra.illegalbuildings.repository.AdminRepository;

@Service
public class AdminService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @PostConstruct
    public void createDefaultAdmin() {
        if (!adminRepository.existsByUsername("admin")) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            adminRepository.save(admin);
            System.out.println("Default admin user created: username=admin, password=admin123");
        }
    }
}