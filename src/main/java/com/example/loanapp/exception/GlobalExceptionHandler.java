package com.example.loanapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        logger.error("Error occurred: ", ex);
        return ResponseEntity
                .internalServerError()
                .body("An error occurred: " + ex.getMessage());
    }

    @ExceptionHandler(LoanValidationException.class)
    public ResponseEntity<String> handleLoanValidationException(LoanValidationException ex) {
        logger.error("Validation error: ", ex);
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }
} 