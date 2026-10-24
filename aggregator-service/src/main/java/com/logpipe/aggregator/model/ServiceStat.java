package com.logpipe.aggregator.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "service_stats")
@Data
@NoArgsConstructor
public class ServiceStat {

    @Id
    private String serviceName;

    private long totalRequests;
    private long errorCount;

    private Instant windowStart = Instant.now();
    private Instant updatedAt = Instant.now();
}
