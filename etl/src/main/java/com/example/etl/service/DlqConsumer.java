package com.example.etl.service;

import com.example.common.constant.Constants;
import com.example.common.dto.DlqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DlqConsumer {

    @KafkaListener(topics = Constants.ORDER_EVENTS_DLQ_TOPIC, groupId = "dlq-monitoring-group")
    public void handleDlqMessage(
            @Payload DlqMessage dlqMessage,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.error("DLQ Message received - Topic: {}, Key: {}, Partition: {}, Offset: {}", 
                topic, key, partition, offset);
            log.error("Original Topic: {}, Original Key: {}, Retry Count: {}, Failed At: {}", 
                dlqMessage.getOriginalTopic(), dlqMessage.getOriginalKey(), 
                dlqMessage.getRetryCount(), dlqMessage.getFailedAt());
            log.error("Error Message: {}", dlqMessage.getErrorMessage());
            log.error("Original Message: {}", dlqMessage.getOriginalMessage());
            
            // Here you could implement:
            // 1. Save to a dead letter database table for analysis
            // 2. Send alerts to monitoring systems
            // 3. Trigger manual intervention workflows
            // 4. Attempt reprocessing after fixing issues
            
            processFailedMessage(dlqMessage);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process DLQ message: key={}, error={}", key, e.getMessage(), e);
            // Even if DLQ processing fails, we should acknowledge to prevent loops
            acknowledgment.acknowledge();
        }
    }
    
    private void processFailedMessage(DlqMessage dlqMessage) {
        // Implementation could include:
        // - Storing in a dedicated DLQ database table for analysis
        // - Sending notifications to administrators
        // - Creating tickets in issue tracking systems
        // - Metrics collection for monitoring dashboards
        
        log.warn("Processing failed message for monitoring: originalTopic={}, retryCount={}, errorMessage={}", 
            dlqMessage.getOriginalTopic(), dlqMessage.getRetryCount(), dlqMessage.getErrorMessage());
        
        // For now, just log the failure for monitoring purposes
    }
}