package com.logpipe.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AggregatorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AggregatorServiceApplication.class, args);
    }
}
