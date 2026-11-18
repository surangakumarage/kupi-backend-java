package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.BusinessMenuImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessMenuImageRepository extends JpaRepository<BusinessMenuImage, String> {

    Optional<BusinessMenuImage> findByMerchantId(String merchantId);

    void deleteByMerchantId(String merchantId);
}
