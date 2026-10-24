package com.logpipe.aggregator.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Local stand-in for a BigQuery table. When app.bigquery.enabled=false
 * (the default, so this project runs without any GCP credentials),
 * LocalBigQueryExportService writes rolling-window snapshots here instead
 * of streaming them to real BigQuery.
 */
@Entity
@Table(name = "bigquery_export")
@Data
@NoArgsConstructor
public class BigQueryExportRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private long totalRequests;
    private long errorCount;
    private double errorRate;
    private Instant exportedAt = Instant.now();
}
