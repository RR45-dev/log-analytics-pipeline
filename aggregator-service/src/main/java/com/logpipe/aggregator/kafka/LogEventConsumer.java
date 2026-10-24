package com.logpipe.aggregator.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logpipe.aggregator.model.LogEvent;
import com.logpipe.aggregator.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventConsumer {

    private final StatsService statsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "raw-logs", groupId = "aggregator-service")
    public void onLogEvent(String rawJson) {
        try {
            LogEvent event = objectMapper.readValue(rawJson, LogEvent.class);
            statsService.record(event);
        } catch (Exception e) {
            log.warn("Failed to parse log event: {}", rawJson, e);
        }
    }
}
