package com.mumbra.illegalbuildings.controller;

import com.mumbra.illegalbuildings.model.User;
import com.mumbra.illegalbuildings.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;      // <-- ADD THIS
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;                  // <-- ADD THIS
import java.util.Collections;              // <-- ADD THIS


@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5500") // allows your JS frontend to call this
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody User user) {
        try {
            userService.createUser(user);
            return ResponseEntity.ok("Account created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create account: " + e.getMessage());
        }
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

@PostMapping("/login")
public ResponseEntity<?> loginUser(@RequestBody User user) {
    Optional<User> existing = userService.getByEmail(user.getEmail());
    if(existing.isPresent() && existing.get().getPassword().equals(user.getPassword())) {
        // Return user data
        return ResponseEntity.ok(existing.get());
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                       .body(Collections.singletonMap("message", "Invalid credentials"));
}

}
