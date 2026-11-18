package com.kupi.cc.location.repository;

import com.kupi.cc.location.exception.KupiLocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link CacheRepository)
 */
@Repository
@Transactional
public class LocationCacheRepository implements CacheRepository {

    private final long ttl;
    private final StringRedisTemplate redisTemplate;
    private final ValueOperations<String, String> valueOps;

    @Autowired
    public LocationCacheRepository(StringRedisTemplate redisTemplate,
                                   @Value("${redis.cache.ttl:3600}") long ttl) {
        this.redisTemplate = redisTemplate;
        valueOps = redisTemplate.opsForValue();
        this.ttl = ttl;
    }

    /**
     * Save the key value pair in cache with a ttl
     *
     * @param key   cache key
     * @param value cache value
     */
    @Override
    public void put(String key, String value) {
        try {
            valueOps.set(key, value);
            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
            // Caching runtime exception since specific exception is not defined
        } catch (RuntimeException e) {
            throw new KupiLocationException("Error while saving to cache ", e);
        }
    }

    /**
     * Returns the cached value
     *
     * @param key cached key
     * @return cached value
     */
    @Override
    public Optional<String> get(String key) {
        try {
            Boolean b = redisTemplate.hasKey(key);
            if (b) {
                return Optional.ofNullable(valueOps.get(key));
            } else {
                return Optional.empty();
            }
            // Caching runtime exception since specific exception is not defined
        } catch (RuntimeException e) {
            throw new KupiLocationException("Error while retrieving from the cache ", e);
        }
    }

}
