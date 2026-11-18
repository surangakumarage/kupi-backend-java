package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.RejectedProfileUpdates;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectedProfileUpdatesRepository extends JpaRepository<RejectedProfileUpdates, String> {
}
