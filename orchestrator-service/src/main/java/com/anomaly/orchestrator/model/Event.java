package com.anomaly.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("user_id")
    private String userId;
    
    private Long timestamp;
    
    private Double amount;
    
    private String location;
    
    private Map<String, Object> metadata;
    
    public Event(String eventId, String userId) {
        this.eventId = eventId;
        this.userId = userId;
        this.timestamp = Instant.now().toEpochMilli();
        this.metadata = new HashMap<>();
    }
    
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    public boolean isValid() {
        return eventId != null && !eventId.isEmpty() 
            && userId != null && !userId.isEmpty();
    }
}
