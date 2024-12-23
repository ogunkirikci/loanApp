package com.example.loanapp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    
    @GetMapping("/")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public String home() {
        return "Welcome to Loan Application!";
    }
} 