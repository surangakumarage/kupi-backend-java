package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.*;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.response.BasicMerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.BasicUserResponseDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidRequestADealException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.RequestADealRepository;
import com.kupi.cc.base.repository.RequestADealSearchIndexRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RequestADealService {

    protected static final String ALL = "ALL";
    private static final String INVALID_REQUEST_A_DEAL = "Invalid request a deal Id: ";
    private static final int PAGE = 0;
    private static final int SIZE = 250;
    private static final String CRON = "0 0 */1 * * *";
    private static final String TIME_ZONE = "GMT +05:30";
    private static final String ADMIN = "Admin";
    private final RequestADealRepository requestADealRepository;
    private final RequestADealSearchIndexRepository requestADealSearchIndexRepository;
    private final RequestADealSearchIndexService requestADealSearchIndexService;
    private final AuthUserService authUserService;

    @Autowired
    public RequestADealService(RequestADealRepository requestADealRepository, AuthUserService authUserService,
                               RequestADealSearchIndexRepository requestADealSearchIndexRepository,
                               RequestADealSearchIndexService requestADealSearchIndexService) {
        this.requestADealRepository = requestADealRepository;
        this.requestADealSearchIndexRepository = requestADealSearchIndexRepository;
        this.requestADealSearchIndexService = requestADealSearchIndexService;
        this.authUserService = authUserService;
    }

    /**
     * This scheduler method updates request a deal table & search index table.
     * Runs every hour.
     */
    @Scheduled(cron = CRON, zone = TIME_ZONE)
    private void updateRequestADealTables() {
        log.debug("Updating request a deal & search index table.");
        try {
            int totalPages = 1;
            Page<String> userIdPage;
            List<BasicUserResponseDto> usersList = new ArrayList<>();
            List<String> userIdList = new ArrayList<>();
            for (int page = PAGE; page < totalPages; page++) {
                Pageable pageable = PageRequest.of(page, SIZE);
                userIdPage = requestADealRepository.getDistinctUserIds(pageable);
                BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto(userIdPage.toList());
                List<BasicMerchantBusinessResponseDto> users =
                        authUserService.getBulkUserResponse(ADMIN, bulkUserRequestDto).getUsers();
                users.forEach(user -> usersList.add(new BasicUserResponseDto(user)));
                userIdList.addAll(getUserIdList(usersList));
                totalPages = userIdPage.getTotalPages();
            }
            requestADealRepository.deleteByUserIdNotIn(userIdList);
            requestADealSearchIndexRepository.deleteByUserIdNotIn(userIdList);
            log.debug("Completed updating request a deal & search index table.");
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating request a deal & search index table was failed.", e);
        }
    }

    /**
     * This method returns a list of userIds from a list of user response dto.
     *
     * @param userResponseDtoList userResponseDtoList
     * @return list of userIds.
     */
    private List<String> getUserIdList(List<BasicUserResponseDto> userResponseDtoList) {
        List<String> userIdList = new ArrayList<>();
        for (BasicUserResponseDto basicUserResponseDto : userResponseDtoList) {
            userIdList.add(basicUserResponseDto.getId());
        }
        return userIdList;
    }

    /**
     * Save new request a deal into database
     *
     * @param requestADeal RequestDeal
     * @return RequestDeal
     */
    public RequestADeal saveRequestADeal(RequestADeal requestADeal, BusinessMerchantResponseDto merchant, UserType userType) {
        try {
            if ((userType == UserType.BANK)) {
                requestADeal.setToUserType(UserType.BANK);
            } else {
                requestADeal.setToUserType(UserType.MERCHANT);
            }
            var savedRequestADeal = requestADealRepository.save(requestADeal);
            requestADealSearchIndexService.save(savedRequestADeal, merchant.getBusinessName());
            return savedRequestADeal;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving request deal into database was failed.", e);
        }
    }

    /**
     * Get request deals by user Id and toUserType.
     *
     * @param pageable pageable
     * @param userId   userId
     * @return page
     */
    public Page<RequestADeal> getRequestDealsByUserIdAndToUserType(Pageable pageable, String userId, UserType toUserType) {
        try {
            if (toUserType == UserType.BANK) {
                return requestADealRepository.findAllByUserIdAndToUserType(pageable, userId, UserType.BANK);
            } else {
                return requestADealRepository.findAllByUserIdAndToUserType(pageable, userId, UserType.MERCHANT);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading request a deal by user id from database was failed", e);
        }
    }

    /**
     * Get request deal by id
     *
     * @param id requestDeal id
     * @return requestDeal
     */
    public RequestADeal getRequestADealById(String id) {
        try {
            Optional<RequestADeal> optionalRequestADeal = requestADealRepository.findById(id);
            if (optionalRequestADeal.isPresent()) {
                return optionalRequestADeal.get();
            } else {
                throw new InvalidRequestADealException(INVALID_REQUEST_A_DEAL + id);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading request a deal by id from database was failed", e);
        }
    }

    /**
     * Get Combined Deal Requests
     *
     * @param pageable   pageable
     * @param merchantId merchant id
     * @param searchTerm search term
     * @return page CombinedDealRequest
     */
    public Page<CombinedDealRequest> getAllCombinedDealRequests(Pageable pageable,
                                                                String merchantId, String searchTerm, UserType userType) {
        try {

            return requestADealSearchIndexService
                    .getAllCombinedDealRequests(pageable, merchantId, searchTerm, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("fetching all combinedDealRequest from database was failed.", e);
        }
    }

    /**
     * This method returns count of combined deal request by merchant, category, brand.
     *
     * @param merchantId merchant id
     * @param category   category
     * @param brand      brand
     * @param toUserType toUserType
     * @return count
     */
    public long getCountForCombinedRequestADeal(String merchantId,
                                                Category category, Brand brand, UserType toUserType) {
        try {
            return (brand != null) ?
                    requestADealRepository
                            .countRequestADealsByMerchantIdAndCategoryAndBrandAndToUserType(merchantId,
                                    category, brand, toUserType)
                    : requestADealRepository
                    .countRequestADealsByMerchantIdAndCategoryAndBrandAndToUserType(merchantId,
                            category, null, toUserType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("returning count for the combined request a deal from database was failed.", e);
        }
    }

    /**
     * Get Combined Deal Requests by category id, brand id and merchant id
     *
     * @param pageable   pageable
     * @param merchantId merchant id
     * @param categoryId category id
     * @param brandId    brand id
     * @return RequestADeal List
     */
    public Page<RequestADeal> getAllRequestADealDetail(Pageable pageable, String merchantId,
                                                       String categoryId, String brandId, UserType userType) {
        try {
            return ((brandId != null) ?
                    requestADealRepository
                            .getAllByMerchantIdAndBrandIdCategoryIdAndUserType(pageable,
                                    merchantId, brandId, categoryId, userType)
                    : requestADealRepository
                    .getAllByMerchantIdCategoryIdAndUserType(pageable, merchantId, categoryId, userType));
        } catch (DataAccessException e) {
            throw new KupiCoreException("fetching combined request a deal note list from database was failed.", e);
        }
    }

    /**
     * Get request a deals for a merchant with optional filters.
     *
     * @param pageable   pageable
     * @param merchantId merchant id
     * @param categoryId category id or 'ALL'
     * @param searchTerm search term for note or 'ALL'
     * @param startDate  start date in epoch millis (null to ignore)
     * @param endDate    end date in epoch millis (null to ignore)
     * @param userType   toUserType
     * @return RequestADeal page
     */
    public Page<RequestADeal> getRequestADealsForMerchant(Pageable pageable,
                                                          String merchantId,
                                                          String categoryId,
                                                          String searchTerm,
                                                          Long startDate,
                                                          Long endDate,
                                                          UserType userType) {
        try {
            return requestADealRepository.getAllByMerchantIdWithFilters(pageable, merchantId, categoryId,
                    searchTerm, startDate, endDate, userType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching request a deals for merchant with filters from database was failed.",
                    e);
        }
    }

    /**
     * This method returns true when offer type associated with request a deal(s).
     *
     * @param offerType offerType
     * @return true/ false
     */
    public boolean isOfferTypeHasRequestADeals(OfferType offerType) {
        try {
            return requestADealRepository.existsByOfferType(offerType);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading request a deals by offer type, offer type id: " + offerType.getId()
                    + " from database was failed.", e);
        }
    }
}
