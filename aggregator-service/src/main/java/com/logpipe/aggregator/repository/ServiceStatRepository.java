package com.logpipe.aggregator.repository;

import com.logpipe.aggregator.model.ServiceStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceStatRepository extends JpaRepository<ServiceStat, String> {
}
