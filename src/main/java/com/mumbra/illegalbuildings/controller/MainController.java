package com.mumbra.illegalbuildings.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.mumbra.illegalbuildings.model.User;
import com.mumbra.illegalbuildings.repository.UserRepository;
import java.util.Optional;


@RestController
@RequestMapping("/api")
public class MainController {

    @Autowired
    private UserRepository userRepository;

    // Login endpoint
    @PostMapping("/login")
    public String loginUser(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            if (existingUser.get().getPassword().equals(user.getPassword())) {
                return "Login successful!";
            } else {
                return "Invalid password!";
            }
        } else {
            return "User not found!";
        }
    }

    // Signup endpoint
    @PostMapping("/signup")
    public String signupUser(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return "User already exists with this email!";
        } else {
            userRepository.save(user);
            return "Account created successfully!";
        }
    }
}
