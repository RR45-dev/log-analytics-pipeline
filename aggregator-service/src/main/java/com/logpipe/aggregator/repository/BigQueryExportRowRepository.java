package com.logpipe.aggregator.repository;

import com.logpipe.aggregator.model.BigQueryExportRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BigQueryExportRowRepository extends JpaRepository<BigQueryExportRow, Long> {
}
