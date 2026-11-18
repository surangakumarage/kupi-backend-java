package com.kupi.cc.location.repository;

import java.util.Optional;


public interface CacheRepository {

    /**
     * Store the key value pair in cache
     *
     * @param key   String key
     * @param value Integer value
     */
    void put(String key, String value);

    /**
     * Retrieve value from cache when key is provided
     *
     * @param key String key
     * @return Optional String
     */
    Optional<String> get(String key);

}
