package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.DemoVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Demo Video Repository
 */
@Repository
public interface DemoVideoRepository extends JpaRepository<DemoVideo, String> {
    
    List<DemoVideo> findAllByOrderByCreatedAtDesc();
    
    Optional<DemoVideo> findByActiveTrue();
    
    @Modifying
    @Query("UPDATE DemoVideo d SET d.active = false WHERE d.active = true")
    void deactivateAll();
}

