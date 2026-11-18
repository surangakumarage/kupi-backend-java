package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.BusinessSliderImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessSliderImageRepository extends JpaRepository<BusinessSliderImage, String> {

    List<BusinessSliderImage> findByMerchantId(String merchantId);

    void deleteByMerchantId(String merchantId);

    /**
     * This method returns slider images for multiple merchants.
     *
     * @param merchantIds list of merchant ids
     * @return list of BusinessSliderImage
     */
    List<BusinessSliderImage> findByMerchantIdIn(List<String> merchantIds);
}
