package com.logpipe.aggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Mirrors the JSON payload ingestion-service (Go) publishes to Kafka. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEvent {
    private String service;
    private String level;
    private String message;
    private long timestamp;
}
