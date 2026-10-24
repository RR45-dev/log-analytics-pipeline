package com.logpipe.aggregator.controller;

import com.logpipe.aggregator.model.ServiceStat;
import com.logpipe.aggregator.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public List<Map<String, Object>> all() {
        return statsService.allStats().stream().map(this::toDto).toList();
    }

    @GetMapping("/{service}")
    public Map<String, Object> one(@PathVariable String service) {
        return toDto(statsService.getStat(service));
    }

    private Map<String, Object> toDto(ServiceStat s) {
        double errorRate = s.getTotalRequests() == 0 ? 0.0 : (double) s.getErrorCount() / s.getTotalRequests();
        return Map.of(
                "service", s.getServiceName(),
                "totalRequests", s.getTotalRequests(),
                "errorCount", s.getErrorCount(),
                "errorRate", errorRate
        );
    }
}
