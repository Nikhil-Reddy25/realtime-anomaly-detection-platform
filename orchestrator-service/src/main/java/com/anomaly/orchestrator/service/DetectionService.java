package com.anomaly.orchestrator.service;

import com.anomaly.orchestrator.model.Event;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DetectionService {
    
    private static final Logger log = LoggerFactory.getLogger(DetectionService.class);
    private static final double ANOMALY_THRESHOLD = 100.0;
    
    private final CacheService cacheService;
    private final Counter detectionCounter;
    private final Counter anomalyCounter;
    private final Timer detectionTimer;
    
    public DetectionService(CacheService cacheService, MeterRegistry registry) {
        this.cacheService = cacheService;
        this.detectionCounter = Counter.builder("anomaly.detection.total")
            .description("Total events processed")
            .register(registry);
        this.anomalyCounter = Counter.builder("anomaly.detection.anomalies")
            .description("Total anomalies detected")
            .register(registry);
        this.detectionTimer = Timer.builder("anomaly.detection.duration")
            .description("Detection processing time")
            .register(registry);
    }
    
    public boolean detectAnomaly(Event event) {
        return detectionTimer.record(() -> {
            detectionCounter.increment();
            
            String cacheKey = "features:" + event.getUserId();
            Double cachedAvg = cacheService.getAverage(cacheKey);
            
            if (cachedAvg != null) {
                log.debug("Cache hit for user {}", event.getUserId());
            } else {
                cachedAvg = 50.0;
                cacheService.saveAverage(cacheKey, cachedAvg);
                log.debug("Cache miss for user {}", event.getUserId());
            }
            
            boolean isAnomaly = event.getAmount() != null && 
                              event.getAmount() > ANOMALY_THRESHOLD;
            
            if (isAnomaly) {
                anomalyCounter.increment();
                log.warn("Anomaly detected for event {} - amount: {}", 
                    event.getEventId(), event.getAmount());
            }
            
            return isAnomaly;
        });
    }
}
