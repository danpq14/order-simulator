package com.example.common.constant;

/**
 * Constants used across the order simulator services
 */
public final class Constants {
    
    private Constants() {
        // Utility class
    }
    
    // Kafka Topics
    public static final String ORDER_EVENTS_TOPIC = "order-events";
    public static final String ORDER_EVENTS_DLQ_TOPIC = "order-events-dlq";
}