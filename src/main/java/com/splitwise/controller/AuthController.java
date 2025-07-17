package com.splitwise.controller;

import com.splitwise.dto.*;
import com.splitwise.entity.User;
import com.splitwise.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        try {
            User user = authService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone()
            );
            
            // Store user in session
            session.setAttribute("currentUser", user);
            
            UserDTO userDTO = new UserDTO(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.success("User registered successfully", userDTO));
                    
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        try {
            Optional<User> userOpt = authService.findByEmail(request.getEmail());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Invalid email or password"));
            }
            
            User user = userOpt.get();
            
            if (!authService.validatePassword(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Invalid email or password"));
            }
            
            // Store user in session
            session.setAttribute("currentUser", user);
            
            UserDTO userDTO = new UserDTO(user);
            return ResponseEntity.ok(AuthResponse.success("Login successful", userDTO));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(AuthResponse.success("Logout successful", null));
    }

    @GetMapping("/current")
    public ResponseEntity<AuthResponse> getCurrentUser(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("No user logged in"));
        }
        
        UserDTO userDTO = new UserDTO(currentUser);
        return ResponseEntity.ok(AuthResponse.success("Current user retrieved", userDTO));
    }
}
