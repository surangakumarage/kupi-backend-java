package com.kupi.cc.auth.service;


import com.kupi.cc.auth.domain.entity.BusinessMenuImage;
import com.kupi.cc.auth.domain.request.MenuImageRequestDto;
import com.kupi.cc.auth.domain.response.MenuImageResponseDto;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.repository.BusinessMenuImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class BusinessMenuImageService {

    private final BusinessMenuImageRepository menuImageRepository;
    private final MerchantService merchantService;


    public BusinessMenuImageService(BusinessMenuImageRepository menuImageRepository, MerchantService merchantService) {
        this.menuImageRepository = menuImageRepository;
        this.merchantService = merchantService;
    }

    @Transactional
    public MenuImageResponseDto saveMenuImage(MenuImageRequestDto requestDto) {
        try {
            merchantService.validateMerchantId(requestDto.getMerchantId());
            menuImageRepository.deleteByMerchantId(requestDto.getMerchantId());
            BusinessMenuImage image = new BusinessMenuImage(requestDto.getMerchantId(), requestDto.getImageUrl());
            BusinessMenuImage saved = menuImageRepository.save(image);
            return new MenuImageResponseDto(saved);
        } catch (InvalidUserException e) {
            throw new InvalidUserException("Invalid merchant id: " + requestDto.getMerchantId() + e);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to save menu images for merchantId: " + requestDto.getMerchantId(), e);
        }
    }

    public MenuImageResponseDto getMenuImage(String merchantId) {

        try {
            merchantService.validateMerchantId(merchantId);
            BusinessMenuImage image = menuImageRepository.findByMerchantId(merchantId).orElse(null);
            return image != null ? new MenuImageResponseDto(image) : null;
        } catch (InvalidUserException e) {
            throw new InvalidUserException("Invalid merchant id: " + merchantId + e);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get menu images for merchantId: " + merchantId, e);
        }
    }
}
