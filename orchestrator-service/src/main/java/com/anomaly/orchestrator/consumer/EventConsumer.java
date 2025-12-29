package com.anomaly.orchestrator.consumer;

import com.anomaly.orchestrator.model.Event;
import com.anomaly.orchestrator.service.DetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);
    private final DetectionService detectionService;
    
    public EventConsumer(DetectionService detectionService) {
        this.detectionService = detectionService;
    }
    
    @KafkaListener(
        topics = "${kafka.topics.anomaly-events}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(Event event, Acknowledgment ack) {
        try {
            if (event == null || !event.isValid()) {
                log.warn("Received invalid event, skipping");
                ack.acknowledge();
                return;
            }
            
            log.info("Processing event: {} for user: {}", 
                event.getEventId(), event.getUserId());
            
            detectionService.detectAnomaly(event);
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing event {}: {}", 
                event != null ? event.getEventId() : "unknown", e.getMessage());
        }
    }
}
