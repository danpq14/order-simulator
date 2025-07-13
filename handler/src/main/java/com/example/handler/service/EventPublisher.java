package com.example.handler.service;

import com.example.common.constant.Constants;
import com.example.common.dto.OrderEventDto;
import com.example.common.enums.EventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishOrderEvent(Long orderId, String symbol, EventType eventType, Object eventData) {
        try {
            String eventDataJson = objectMapper.writeValueAsString(eventData);

            OrderEventDto orderEvent = new OrderEventDto(
                orderId,
                symbol,
                eventType,
                eventDataJson,
                Instant.now()
            );

            kafkaTemplate.send(Constants.ORDER_EVENTS_TOPIC, orderId.toString(), orderEvent);
            log.info("Published order event: orderId={}, eventType={}", orderId, eventType);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event data for order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to publish order event", e);
        }
    }
}
