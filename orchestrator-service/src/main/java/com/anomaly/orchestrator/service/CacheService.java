package com.anomaly.orchestrator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void cacheAnomalyResult(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MINUTES);
    }
    
    public Object getCachedResult(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    public void invalidateCache(String key) {
        redisTemplate.delete(key);
    }
}
