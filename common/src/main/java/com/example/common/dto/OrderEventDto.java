package com.example.common.dto;

import com.example.common.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for order events sent through Kafka
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventDto {
    private Long orderId;
    private String symbol;
    private EventType eventType;
    private String eventData;
    private Instant timestamp;
}