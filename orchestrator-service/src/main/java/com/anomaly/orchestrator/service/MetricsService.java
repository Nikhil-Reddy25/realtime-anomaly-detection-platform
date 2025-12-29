package com.anomaly.orchestrator.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {
    
    private final Counter anomalyCounter;
    private final MeterRegistry meterRegistry;
    
    @Autowired
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.anomalyCounter = Counter.builder("anomaly.detected")
            .description("Number of anomalies detected")
            .register(meterRegistry);
    }
    
    public void recordAnomaly() {
        anomalyCounter.increment();
    }
    
    public void recordProcessingTime(long duration) {
        meterRegistry.timer("processing.time").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
