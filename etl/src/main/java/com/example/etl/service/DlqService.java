package com.example.etl.service;

import com.example.common.constant.Constants;
import com.example.common.dto.DlqMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DlqService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendToDlq(String originalTopic, String originalKey, Object originalMessage, Exception error, int retryCount) {
        try {
            String originalMessageJson = objectMapper.writeValueAsString(originalMessage);
            String stackTrace = getStackTrace(error);

            DlqMessage dlqMessage = new DlqMessage(
                originalTopic,
                originalKey,
                originalMessageJson,
                error.getMessage(),
                stackTrace,
                retryCount,
                Instant.now()
            );

            kafkaTemplate.send(Constants.ORDER_EVENTS_DLQ_TOPIC, originalKey, dlqMessage);
            
            log.error("Message sent to DLQ: topic={}, key={}, retryCount={}, error={}", 
                originalTopic, originalKey, retryCount, error.getMessage());

        } catch (Exception e) {
            log.error("Failed to send message to DLQ: originalTopic={}, originalKey={}, error={}", 
                originalTopic, originalKey, e.getMessage(), e);
        }
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}