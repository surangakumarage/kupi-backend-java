package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.PreferredMerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreferredMerchantRepository extends JpaRepository<PreferredMerchant, String> {

    List<PreferredMerchant> findByUserId(String userId);

    void deleteByUserId(String userId);

}
