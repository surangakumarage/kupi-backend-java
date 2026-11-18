package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.ApiAccessKey;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ApiAccessKeyRepository
 */
public interface ApiAccessKeyRepository extends JpaRepository<ApiAccessKey, String> {
}
