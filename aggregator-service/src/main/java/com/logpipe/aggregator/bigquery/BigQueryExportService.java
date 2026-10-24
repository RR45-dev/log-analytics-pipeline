package com.logpipe.aggregator.bigquery;

import com.logpipe.aggregator.model.ServiceStat;

public interface BigQueryExportService {
    void export(ServiceStat stat);
}
