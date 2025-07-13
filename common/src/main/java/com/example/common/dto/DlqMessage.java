package com.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for messages sent to Dead Letter Queue
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DlqMessage {
    private String originalTopic;
    private String originalKey;
    private String originalMessage;
    private String errorMessage;
    private String stackTrace;
    private int retryCount;
    private Instant failedAt;
}