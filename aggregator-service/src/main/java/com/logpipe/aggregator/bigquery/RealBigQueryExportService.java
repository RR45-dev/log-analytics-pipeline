package com.logpipe.aggregator.bigquery;

import com.google.cloud.bigquery.*;
import com.logpipe.aggregator.model.ServiceStat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Streams rolling-window snapshots into a real BigQuery table. Activated
 * with app.bigquery.enabled=true and GCP_PROJECT_ID set; requires
 * Application Default Credentials to be available (e.g. GOOGLE_APPLICATION_CREDENTIALS).
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.bigquery.enabled", havingValue = "true")
public class RealBigQueryExportService implements BigQueryExportService {

    @Value("${app.bigquery.dataset}")
    private String dataset;

    @Value("${app.bigquery.table}")
    private String table;

    private final BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();

    @Override
    public void export(ServiceStat stat) {
        double errorRate = stat.getTotalRequests() == 0 ? 0.0 : (double) stat.getErrorCount() / stat.getTotalRequests();

        Map<String, Object> row = new HashMap<>();
        row.put("service_name", stat.getServiceName());
        row.put("total_requests", stat.getTotalRequests());
        row.put("error_count", stat.getErrorCount());
        row.put("error_rate", errorRate);
        row.put("exported_at", stat.getUpdatedAt().toString());

        TableId tableId = TableId.of(dataset, table);
        InsertAllResponse response = bigQuery.insertAll(
                InsertAllRequest.newBuilder(tableId)
                        .addRow(UUID.randomUUID().toString(), row)
                        .build());

        if (response.hasErrors()) {
            log.error("BigQuery insert errors: {}", response.getInsertErrors());
        }
    }
}
