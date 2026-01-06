package com.anomaly.orchestrator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCacheAnomalyResultSuccess() {
        // Given
        String key = "anomaly:test-001";
        Object value = "anomaly-detected";
        long timeout = 10L;

        // When
        cacheService.cacheAnomalyResult(key, value, timeout);

        // Then
        verify(valueOperations, times(1)).set(key, value, timeout, TimeUnit.MINUTES);
    }

    @Test
    void testGetCachedResultSuccess() {
        // Given
        String key = "anomaly:test-002";
        Object expectedValue = "cached-result";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        // When
        Object result = cacheService.getCachedResult(key);

        // Then
        assertEquals(expectedValue, result);
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    void testGetCachedResultNotFound() {
        // Given
        String key = "anomaly:test-003";
        when(valueOperations.get(key)).thenReturn(null);

        // When
        Object result = cacheService.getCachedResult(key);

        // Then
        assertNull(result);
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    void testInvalidateCache() {
        // Given
        String key = "anomaly:test-004";
        when(redisTemplate.delete(key)).thenReturn(true);

        // When
        cacheService.invalidateCache(key);

        // Then
        verify(redisTemplate, times(1)).delete(key);
    }

    @Test
    void testCacheWithZeroTimeout() {
        // Given
        String key = "anomaly:test-005";
        Object value = "test-value";
        long timeout = 0L;

        // When
        cacheService.cacheAnomalyResult(key, value, timeout);

        // Then
        verify(valueOperations, times(1)).set(key, value, timeout, TimeUnit.MINUTES);
    }

    @Test
    void testCacheWithNegativeTimeout() {
        // Given
        String key = "anomaly:test-006";
        Object value = "test-value";
        long timeout = -1L;

        // When & Then - Should handle gracefully or throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            cacheService.cacheAnomalyResult(key, value, timeout);
        });
    }
}
