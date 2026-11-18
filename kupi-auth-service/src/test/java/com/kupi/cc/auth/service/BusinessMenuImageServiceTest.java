package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.BusinessMenuImage;
import com.kupi.cc.auth.domain.request.MenuImageRequestDto;
import com.kupi.cc.auth.domain.response.MenuImageResponseDto;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.repository.BusinessMenuImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link BusinessMenuImageService} class.
 */
class BusinessMenuImageServiceTest {

    private static final String MERCHANT_ID = "uid-bd0c8894-8ddb-4b1d-8383-de44bfaa297f";
    private static final String IMAGE_URL = "https://www.singersl.com/image.jpg";
    private static final String FAILED = "failed";

    private BusinessMenuImageService businessMenuImageService;

    @Mock
    private BusinessMenuImageRepository menuImageRepository;

    @Mock
    private MerchantService merchantService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        businessMenuImageService = new BusinessMenuImageService(menuImageRepository, merchantService);
    }

    /**
     * Save menu image
     */
    @Test
    void Should_ReturnMenuImageResponseDto_When_SavingMenuImageIsSuccessful() {
        MenuImageRequestDto requestDto = getSampleMenuImageRequestDto();
        BusinessMenuImage savedImage = getSampleBusinessMenuImage();

        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        doNothing().when(menuImageRepository).deleteByMerchantId(MERCHANT_ID);
        when(menuImageRepository.save(any(BusinessMenuImage.class))).thenReturn(savedImage);

        MenuImageResponseDto response = businessMenuImageService.saveMenuImage(requestDto);

        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(menuImageRepository).deleteByMerchantId(MERCHANT_ID);
        verify(menuImageRepository).save(any(BusinessMenuImage.class));
        assertNotNull(response);
        assertEquals(IMAGE_URL, response.getMenuImageUrl());
    }

    @Test
    void Should_ThrowInvalidUserException_When_SavingMenuImageWithInvalidMerchantId() {
        MenuImageRequestDto requestDto = getSampleMenuImageRequestDto();
        doThrow(new InvalidUserException("Invalid merchant id")).when(merchantService)
                .validateMerchantId(MERCHANT_ID);

        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                businessMenuImageService.saveMenuImage(requestDto));

        assertTrue(exception.getMessage().contains("Invalid merchant id: " + MERCHANT_ID));
        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(menuImageRepository, never()).deleteByMerchantId(anyString());
        verify(menuImageRepository, never()).save(any(BusinessMenuImage.class));
    }

    @Test
    void Should_ThrowAuthServiceException_When_SavingMenuImageIsFailed() {
        MenuImageRequestDto requestDto = getSampleMenuImageRequestDto();
        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        doNothing().when(menuImageRepository).deleteByMerchantId(MERCHANT_ID);
        when(menuImageRepository.save(any(BusinessMenuImage.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });

        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                businessMenuImageService.saveMenuImage(requestDto));

        assertEquals("Failed to save menu images for merchantId: " + MERCHANT_ID, exception.getMessage());
        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(menuImageRepository).deleteByMerchantId(MERCHANT_ID);
        verify(menuImageRepository).save(any(BusinessMenuImage.class));
    }

    @Test
    void Should_ThrowAuthServiceException_When_DeletingMenuImageIsFailed() {
        MenuImageRequestDto requestDto = getSampleMenuImageRequestDto();
        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        doThrow(new DataAccessException(FAILED) {
        }).when(menuImageRepository).deleteByMerchantId(MERCHANT_ID);

        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                businessMenuImageService.saveMenuImage(requestDto));

        assertEquals("Failed to save menu images for merchantId: " + MERCHANT_ID, exception.getMessage());
        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(menuImageRepository).deleteByMerchantId(MERCHANT_ID);
        verify(menuImageRepository, never()).save(any(BusinessMenuImage.class));
    }

    /**
     * Get menu image by merchant id
     */
    @Test
    void Should_ReturnMenuImageResponseDto_When_GettingMenuImageByMerchantIdIsSuccessful() {
        BusinessMenuImage image = getSampleBusinessMenuImage();
        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        when(menuImageRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(image));

        MenuImageResponseDto response = businessMenuImageService.getMenuImage(MERCHANT_ID);

        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(menuImageRepository).findByMerchantId(MERCHANT_ID);
        assertNotNull(response);
        assertEquals(IMAGE_URL, response.getMenuImageUrl());
    }

    @Test
    void Should_ReturnNull_When_NoMenuImageFoundForMerchantId() {
        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        when(menuImageRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.empty());

        MenuImageResponseDto response = businessMenuImageService.getMenuImage(MERCHANT_ID);

        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(menuImageRepository).findByMerchantId(MERCHANT_ID);
        assertNull(response);
    }

    @Test
    void Should_ThrowInvalidUserException_When_GettingMenuImageWithInvalidMerchantId() {
        doThrow(new InvalidUserException("Invalid merchant id")).when(merchantService)
                .validateMerchantId(MERCHANT_ID);

        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                businessMenuImageService.getMenuImage(MERCHANT_ID));

        assertTrue(exception.getMessage().contains("Invalid merchant id: " + MERCHANT_ID));
        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(menuImageRepository, never()).findByMerchantId(anyString());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingMenuImageByMerchantIdIsFailed() {
        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        when(menuImageRepository.findByMerchantId(MERCHANT_ID))
                .thenThrow(new DataAccessException(FAILED) {
                });

        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                businessMenuImageService.getMenuImage(MERCHANT_ID));

        assertEquals("Failed to get menu images for merchantId: " + MERCHANT_ID, exception.getMessage());
        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(menuImageRepository).findByMerchantId(MERCHANT_ID);
    }

    /**
     * Helper methods
     */
    private MenuImageRequestDto getSampleMenuImageRequestDto() {
        MenuImageRequestDto requestDto = new MenuImageRequestDto();
        requestDto.setMerchantId(MERCHANT_ID);
        requestDto.setImageUrl(IMAGE_URL);
        return requestDto;
    }

    private BusinessMenuImage getSampleBusinessMenuImage() {
        return new BusinessMenuImage(MERCHANT_ID, IMAGE_URL);
    }
}

