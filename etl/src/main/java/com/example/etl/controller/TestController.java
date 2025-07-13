package com.example.etl.controller;

import com.example.etl.service.DlqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final DlqService dlqService;

    @PostMapping("/simulate-dlq")
    public ResponseEntity<String> simulateDlq() {
        try {
            // Simulate a failed message for testing DLQ
            Exception testError = new RuntimeException("Simulated processing failure for DLQ testing");
            
            dlqService.sendToDlq(
                "order-events", 
                "test-key-123", 
                "Test message content for DLQ simulation", 
                testError, 
                3
            );
            
            log.info("Test DLQ message sent successfully");
            return ResponseEntity.ok("DLQ test message sent successfully");
            
        } catch (Exception e) {
            log.error("Failed to send test DLQ message", e);
            return ResponseEntity.status(500).body("Failed to send DLQ test message: " + e.getMessage());
        }
    }
}