package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.OfferType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Offer Type repository
 */
public interface OfferTypeRepository extends JpaRepository<OfferType, String> {

    /**
     * This method used to get all Offer type list order by updatedAt field.
     *
     * @return Offer Type list
     */
    List<OfferType> findAllByOrderByUpdatedAtDesc();

    /**
     * This method returns true when offer type name exist in database.
     *
     * @param name offer type name
     * @return true/ false
     */
    boolean existsByName(String name);

    /**
     * This method returns true when name exist with other offer types.
     *
     * @param name offerType name
     * @param id   offerType id
     * @return true/ false
     */
    boolean existsByNameAndIdNot(String name, String id);
}
