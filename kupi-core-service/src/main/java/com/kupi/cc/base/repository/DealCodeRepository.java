package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.DealCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DealCodeRepository extends JpaRepository<DealCode, String> {

    /**
     * This method returns last dealCode.
     *
     * @param localDate local date
     * @return DealCode
     */
    Optional<DealCode> findTopByDealDateOrderByDealDateDescCreatedAtDesc(LocalDate localDate);

}
