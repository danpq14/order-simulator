package com.example.handler.model.dto;

import com.example.common.enums.OrderStatus;
import com.example.common.enums.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for order response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private OrderStatus status;
    private OrderSide side;
    private Instant createdAt;
}
