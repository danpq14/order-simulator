package com.example.handler.service;

import com.example.handler.model.dto.CreateOrderRequest;
import com.example.handler.model.dto.OrderResponse;
import com.example.common.exception.InvalidOrderStateException;
import com.example.common.exception.OrderNotFoundException;
import com.example.common.exception.OrderProcessingException;
import com.example.common.enums.EventType;
import com.example.handler.model.entity.Order;
import com.example.common.enums.OrderStatus;
import com.example.handler.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final Random random = new Random();

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order: symbol={}, quantity={}, price={}, side={}",
                request.getSymbol(), request.getQuantity(), request.getPrice(), request.getSide());

        try {
            Order order = new Order();
            order.setSymbol(request.getSymbol().toUpperCase());
            order.setQuantity(request.getQuantity());
            order.setPrice(request.getPrice());
            order.setSide(request.getSide());
            order.setStatus(OrderStatus.PENDING);

            Order savedOrder = orderRepository.save(order);

            // Publish order created event
            eventPublisher.publishOrderEvent(savedOrder.getId(), savedOrder.getSymbol(), EventType.ORDER_CREATED, savedOrder);

            log.info("Order created successfully: id={}", savedOrder.getId());
            return mapToResponse(savedOrder);

        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage(), e);
            throw new OrderProcessingException("Failed to create order: " + e.getMessage(), e);
        }
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return orderRepository.findAll(pageable).map(this::mapToResponse);
    }

    public OrderResponse getOrderById(Long id) {
        log.debug("Fetching order by id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        log.info("Cancelling order: id={}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(id, order.getStatus(), "cancel");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
        Order savedOrder = orderRepository.save(order);

        // Publish order cancelled event
        eventPublisher.publishOrderEvent(savedOrder.getId(), savedOrder.getSymbol(), EventType.ORDER_CANCELLED, savedOrder);

        log.info("Order cancelled successfully: id={}", id);
        return mapToResponse(savedOrder);
    }

    @Transactional
    public List<OrderResponse> simulateExecution() {
        log.info("Starting order execution simulation");

        List<Order> pendingOrders = orderRepository.findRandomOrdersByStatus(OrderStatus.PENDING, 5);

        if (pendingOrders.isEmpty()) {
            log.info("No pending orders found for simulation");
            return List.of();
        }

        return pendingOrders.stream().map(order -> {
            try {
                // Simulate random execution (80% success rate)
                boolean isSuccessful = random.nextDouble() < 0.8;

                if (isSuccessful) {
                    order.setStatus(OrderStatus.EXECUTED);
                    eventPublisher.publishOrderEvent(order.getId(), order.getSymbol(), EventType.ORDER_EXECUTED, order);
                    log.info("Order executed successfully: id={}", order.getId());
                } else {
                    order.setStatus(OrderStatus.FAILED);
                    eventPublisher.publishOrderEvent(order.getId(), order.getSymbol(), EventType.ORDER_FAILED, order);
                    log.info("Order execution failed: id={}", order.getId());
                }

                Order savedOrder = orderRepository.save(order);
                return mapToResponse(savedOrder);

            } catch (Exception e) {
                log.error("Failed to simulate execution for order {}: {}", order.getId(), e.getMessage(), e);
                throw new OrderProcessingException("Failed to simulate order execution", e);
            }
        }).toList();
    }

    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getSymbol(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                order.getSide(),
                order.getCreatedAt()
        );
    }
}
