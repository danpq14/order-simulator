package com.example.handler.model.dto;

import com.example.common.enums.OrderSide;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for order creation
 */
@Data
public class CreateOrderRequest {
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private OrderSide side;
}
