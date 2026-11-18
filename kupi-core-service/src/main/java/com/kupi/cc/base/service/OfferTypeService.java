package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.OfferType;
import com.kupi.cc.base.exception.ExistingNameException;
import com.kupi.cc.base.exception.InvalidOfferTypeException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.UnsupportedDeleteAction;
import com.kupi.cc.base.repository.OfferTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OfferTypeService {

    private static final String INVALID_OFFER_TYPE_ID = "Invalid Offer Type Id: ";

    private final OfferTypeRepository offerTypeRepository;
    private final RequestADealService requestADealService;

    @Autowired
    public OfferTypeService(OfferTypeRepository offerTypeRepository, RequestADealService requestADealService) {
        this.offerTypeRepository = offerTypeRepository;
        this.requestADealService = requestADealService;
    }

    /**
     * Save offer type in to database
     *
     * @param offerType offer type
     */
    public void saveOfferType(OfferType offerType) {
        try {
            boolean existsByName = isOfferTypeNameExists(offerType.getName(), null);
            if (existsByName) {
                throw new ExistingNameException("Offer type name: " + offerType.getName() + " exists in the database");
            }
            offerTypeRepository.save(offerType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving offer type into database was failed.", e);
        }
    }

    /**
     * This method returns true when offer type name exist in database.
     *
     * @param name offer type name.
     * @return true/ false
     */
    private boolean isOfferTypeNameExists(String name, String id) {
        try {
            return (id == null) ? offerTypeRepository.existsByName(name) :
                    offerTypeRepository.existsByNameAndIdNot(name, id);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking offer type name in database was failed.", e);
        }
    }

    /**
     * Update Offer Type
     *
     * @param offerType offerType
     * @param name      name
     */
    public void updateOfferType(OfferType offerType, String name) {
        try {
            boolean existsByName = isOfferTypeNameExists(name,offerType.getId());
            if (existsByName) {
                throw new ExistingNameException("Offer type name: " + offerType.getName() + " exists in the database");
            }
            offerType.setName(name);
            offerType.setUpdatedAt(System.currentTimeMillis());
            offerTypeRepository.save(offerType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("updating offer type into database was failed.", e);
        }
    }

    /**
     * Get all offer types
     *
     * @return offer type list
     */
    public List<OfferType> getAllOfferTypes() {
        try {
            return offerTypeRepository.findAllByOrderByUpdatedAtDesc();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting offer type list from database was failed.", e);
        }
    }

    /**
     * get offer type by id
     *
     * @param id id
     * @return offer type
     */
    public OfferType getOfferTypeById(String id) {
        try {
            Optional<OfferType> optionalOfferType = offerTypeRepository.findById(id);

            if (optionalOfferType.isPresent()) {
                return optionalOfferType.get();
            } else {
                throw new InvalidOfferTypeException(INVALID_OFFER_TYPE_ID + id);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading Offer Type from database was failed.", e);
        }
    }

    /**
     * This method used to delete an offer type.
     *
     * @param offerTypeId offer type id
     */
    public void deleteOfferType(String offerTypeId) {
        try {
            var offerTypeById = getOfferTypeById(offerTypeId);
            var offerTypeHasNoRequestADeals = requestADealService.isOfferTypeHasRequestADeals(offerTypeById);
            if (offerTypeHasNoRequestADeals) {
                throw new UnsupportedDeleteAction("Offer Type id: " + offerTypeId + " has request a deals");
            }
            offerTypeRepository.delete(offerTypeById);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Deleting Offer Type by offerTypeId: "
                    + offerTypeId + " from database was failed.", e);
        }
    }
}
