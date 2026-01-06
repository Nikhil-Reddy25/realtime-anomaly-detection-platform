package com.anomaly.orchestrator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetectionServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private CacheService cacheService;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private DetectionService detectionService;

    @BeforeEach
    void setUp() {
        // Setup common test data
    }

    @Test
    void testProcessEventSuccess() {
        // Given
        String eventId = "test-event-001";
        String eventData = "{\"features\":[1.0,2.0,3.0]}";
        
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        when(cacheService.getCachedResult(anyString())).thenReturn(null);

        // When
        String result = detectionService.processEvent(eventId, eventData);

        // Then
        assertNotNull(result);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
        verify(metricsService, times(1)).recordAnomaly();
    }

    @Test
    void testProcessEventWithCache() {
        // Given
        String eventId = "test-event-002";
        String eventData = "{\"features\":[1.0,2.0,3.0]}";
        String cachedResult = "cached-result";
        
        when(cacheService.getCachedResult(eventId)).thenReturn(cachedResult);

        // When
        String result = detectionService.processEvent(eventId, eventData);

        // Then
        assertEquals(cachedResult, result);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
        verify(cacheService, times(1)).getCachedResult(eventId);
    }

    @Test
    void testProcessEventKafkaFailure() {
        // Given
        String eventId = "test-event-003";
        String eventData = "{\"features\":[1.0,2.0,3.0]}";
        
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        when(cacheService.getCachedResult(anyString())).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            detectionService.processEvent(eventId, eventData);
        });
    }

    @Test
    void testProcessEventNullInput() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            detectionService.processEvent(null, null);
        });
    }

    @Test
    void testProcessEventEmptyEventId() {
        // Given
        String eventId = "";
        String eventData = "{\"features\":[1.0,2.0,3.0]}";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            detectionService.processEvent(eventId, eventData);
        });
    }

    @Test
    void testProcessBatchEvents() {
        // Given
        String[] eventIds = {"event-1", "event-2", "event-3"};
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        when(cacheService.getCachedResult(anyString())).thenReturn(null);

        // When
        int processed = detectionService.processBatch(eventIds);

        // Then
        assertEquals(3, processed);
        verify(kafkaTemplate, times(3)).send(anyString(), anyString(), anyString());
        verify(metricsService, times(3)).recordAnomaly();
    }
}
