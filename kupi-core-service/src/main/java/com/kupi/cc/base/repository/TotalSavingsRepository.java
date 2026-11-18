package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.TotalSavings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Total Savings Repository
 */
@Repository
public interface TotalSavingsRepository extends JpaRepository<TotalSavings, String> {
    
    /**
     * Find total savings record by ID (should be "totsav-ALL")
     *
     * @param id the ID of the total savings record
     * @return Optional TotalSavings
     */
    Optional<TotalSavings> findById(String id);
}

