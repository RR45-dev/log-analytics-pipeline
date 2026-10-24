package com.logpipe.aggregator.service;

import com.logpipe.aggregator.bigquery.BigQueryExportService;
import com.logpipe.aggregator.model.LogEvent;
import com.logpipe.aggregator.model.ServiceStat;
import com.logpipe.aggregator.repository.ServiceStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ServiceStatRepository repository;
    private final BigQueryExportService exportService;

    @Transactional
    public void record(LogEvent event) {
        ServiceStat stat = repository.findById(event.getService()).orElseGet(() -> {
            ServiceStat s = new ServiceStat();
            s.setServiceName(event.getService());
            return s;
        });
        stat.setTotalRequests(stat.getTotalRequests() + 1);
        if ("ERROR".equalsIgnoreCase(event.getLevel())) {
            stat.setErrorCount(stat.getErrorCount() + 1);
        }
        stat.setUpdatedAt(Instant.now());
        repository.save(stat);
    }

    public ServiceStat getStat(String serviceName) {
        return repository.findById(serviceName).orElseGet(() -> {
            ServiceStat empty = new ServiceStat();
            empty.setServiceName(serviceName);
            return empty;
        });
    }

    public List<ServiceStat> allStats() {
        return repository.findAll();
    }

    /**
     * Every 5 minutes: snapshot each service's running counters to the
     * export sink (BigQuery or its local stand-in) and reset them. This
     * approximates a rolling window without a full streaming windowing
     * engine (Kafka Streams/Flink would be the production upgrade).
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void flushWindow() {
        List<ServiceStat> stats = repository.findAll();
        for (ServiceStat stat : stats) {
            exportService.export(stat);
            stat.setTotalRequests(0);
            stat.setErrorCount(0);
            stat.setWindowStart(Instant.now());
        }
        repository.saveAll(stats);
    }
}
