package com.example.etl.service;

import com.example.common.constant.Constants;
import com.example.common.dto.OrderEventDto;
import com.example.etl.model.entity.Event;
import com.example.etl.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final EventRepository eventRepository;
    private final DlqService dlqService;
    
    private static final int MAX_RETRY_COUNT = 3;

    @KafkaListener(topics = Constants.ORDER_EVENTS_TOPIC)
    @Transactional
    public void handleOrderEvent(
            @Payload OrderEventDto orderEventDto,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "retry-count", required = false) Integer retryCount,
            Acknowledgment acknowledgment) {

        if (retryCount == null) {
            retryCount = 0;
        }

        try {
            log.info("Processing order event: orderId={}, eventType={}, topic={}, partition={}, offset={}, retryCount={}",
                    orderEventDto.getOrderId(), orderEventDto.getEventType(), topic, partition, offset, retryCount);

            // Simulate potential processing failure for demonstration
            processOrderEvent(orderEventDto);

            log.info("Event processed successfully: orderId={}, eventType={}, retryCount={}",
                    orderEventDto.getOrderId(), orderEventDto.getEventType(), retryCount);

            // Acknowledge the message only on success
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process order event: orderId={}, eventType={}, retryCount={}, error={}",
                    orderEventDto.getOrderId(), orderEventDto.getEventType(), retryCount, e.getMessage(), e);

            if (retryCount >= MAX_RETRY_COUNT) {
                log.error("Max retry count exceeded. Sending to DLQ: orderId={}, eventType={}, retryCount={}",
                        orderEventDto.getOrderId(), orderEventDto.getEventType(), retryCount);
                
                // Send to DLQ after max retries
                dlqService.sendToDlq(topic, key, orderEventDto, e, retryCount);
                
                // Acknowledge the message to prevent infinite retries
                acknowledgment.acknowledge();
            } else {
                // Do not acknowledge - let Kafka retry
                log.warn("Will retry processing: orderId={}, eventType={}, retryCount={}",
                        orderEventDto.getOrderId(), orderEventDto.getEventType(), retryCount);
                throw e;
            }
        }
    }

    private void processOrderEvent(OrderEventDto orderEventDto) {
        // Create and save event entity
        Event event = new Event();
        event.setOrderId(orderEventDto.getOrderId());
        event.setEventType(orderEventDto.getEventType());
        event.setEventData(orderEventDto.getEventData());
        event.setCreatedAt(orderEventDto.getTimestamp());

        Event savedEvent = eventRepository.save(event);

        log.debug("Event saved to database: id={}, orderId={}, eventType={}",
                savedEvent.getId(), savedEvent.getOrderId(), savedEvent.getEventType());
    }
}
