package com.logpipe.aggregator.bigquery;

import com.logpipe.aggregator.model.BigQueryExportRow;
import com.logpipe.aggregator.model.ServiceStat;
import com.logpipe.aggregator.repository.BigQueryExportRowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.bigquery.enabled", havingValue = "false", matchIfMissing = true)
public class LocalBigQueryExportService implements BigQueryExportService {

    private final BigQueryExportRowRepository repository;

    @Override
    public void export(ServiceStat stat) {
        BigQueryExportRow row = new BigQueryExportRow();
        row.setServiceName(stat.getServiceName());
        row.setTotalRequests(stat.getTotalRequests());
        row.setErrorCount(stat.getErrorCount());
        row.setErrorRate(stat.getTotalRequests() == 0 ? 0.0 : (double) stat.getErrorCount() / stat.getTotalRequests());
        repository.save(row);
    }
}
