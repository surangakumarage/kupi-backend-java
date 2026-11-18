package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Permission Repository
 */
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
}
