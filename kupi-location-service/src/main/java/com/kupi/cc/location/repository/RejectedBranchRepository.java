package com.kupi.cc.location.repository;

import com.kupi.cc.location.domain.entity.RejectedBranch;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Rejected Branch Repository
 */
public interface RejectedBranchRepository extends JpaRepository<RejectedBranch, String> {
}
