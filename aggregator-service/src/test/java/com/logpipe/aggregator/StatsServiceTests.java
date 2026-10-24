package com.logpipe.aggregator;

import com.logpipe.aggregator.bigquery.BigQueryExportService;
import com.logpipe.aggregator.model.LogEvent;
import com.logpipe.aggregator.model.ServiceStat;
import com.logpipe.aggregator.repository.ServiceStatRepository;
import com.logpipe.aggregator.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StatsServiceTests {

    private ServiceStatRepository repository;
    private BigQueryExportService exportService;
    private StatsService statsService;

    @BeforeEach
    void setUp() {
        repository = mock(ServiceStatRepository.class);
        exportService = mock(BigQueryExportService.class);
        statsService = new StatsService(repository, exportService);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void recordIncrementsTotalAndErrorCountsCorrectly() {
        when(repository.findById("checkout-service")).thenReturn(Optional.empty());

        LogEvent info = new LogEvent();
        info.setService("checkout-service");
        info.setLevel("INFO");
        statsService.record(info);

        ServiceStat afterInfo = new ServiceStat();
        afterInfo.setServiceName("checkout-service");
        afterInfo.setTotalRequests(1);
        when(repository.findById("checkout-service")).thenReturn(Optional.of(afterInfo));

        LogEvent error = new LogEvent();
        error.setService("checkout-service");
        error.setLevel("ERROR");
        statsService.record(error);

        assertEquals(2, afterInfo.getTotalRequests());
        assertEquals(1, afterInfo.getErrorCount());
    }

    @Test
    void flushWindowExportsThenResetsCounters() {
        ServiceStat stat = new ServiceStat();
        stat.setServiceName("payments-service");
        stat.setTotalRequests(10);
        stat.setErrorCount(2);
        when(repository.findAll()).thenReturn(java.util.List.of(stat));

        statsService.flushWindow();

        verify(exportService).export(stat);
        assertEquals(0, stat.getTotalRequests());
        assertEquals(0, stat.getErrorCount());
    }
}
