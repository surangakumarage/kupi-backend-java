package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.PreferredMerchant;
import com.kupi.cc.auth.domain.response.PreferredMerchantResponseDto;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.repository.PreferredMerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PreferredMerchantService {

    private static final String FAILED_TO_SAVE_PREFERRED_MERCHANT_LIST = "Saving preferred merchant list to  database was failed";
    private static final String RETURNING_PREFERRED_MERCHANT_LIST = "Get preferred merchant list by userId from database was failed";

    private final PreferredMerchantRepository preferredMerchantRepository;

    public PreferredMerchantService(PreferredMerchantRepository preferredMerchantRepository) {
        this.preferredMerchantRepository = preferredMerchantRepository;
    }


    @Transactional
    public void updatePreferredMerchants(String userId, List<String> merchantIds) {
        try {
            preferredMerchantRepository.deleteByUserId(userId);
            List<PreferredMerchant> preferredMerchants = merchantIds.stream()
                    .map(mid -> new PreferredMerchant(userId, mid))
                    .collect(Collectors.toList());

            preferredMerchantRepository.saveAll(preferredMerchants);
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_SAVE_PREFERRED_MERCHANT_LIST, e);
        }
    }

    public PreferredMerchantResponseDto getPreferredMerchantIds(String userId) {
        try {
            List<String> preferredMerchantIds = preferredMerchantRepository.findByUserId(userId)
                    .stream()
                    .map(PreferredMerchant::getMerchantId)
                    .collect(Collectors.toList());

            return new PreferredMerchantResponseDto(preferredMerchantIds, userId);
        } catch (DataAccessException e) {
            throw new AuthServiceException(RETURNING_PREFERRED_MERCHANT_LIST, e);
        }
    }
}
