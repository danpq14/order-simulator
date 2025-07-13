package com.example.common.exception;

import com.example.common.enums.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(Long orderId, OrderStatus currentStatus, String operation) {
        super(String.format("Cannot %s order %d in status %s", operation, orderId, currentStatus));
    }

    public InvalidOrderStateException(String message) {
        super(message);
    }
}