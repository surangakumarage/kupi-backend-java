package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.BusinessSliderImage;
import com.kupi.cc.auth.domain.request.SliderImageCreateRequestDto;
import com.kupi.cc.auth.domain.request.SliderImageItemDto;
import com.kupi.cc.auth.domain.response.SliderImageListResponseDto;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.repository.BusinessSliderImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link SliderImageService} class.
 */
class SliderImageServiceTest {

    private static final String MERCHANT_ID = "uid-bd0c8894-8ddb-4b1d-8383-de44bfaa297f";
    private static final String IMAGE_URL = "https://www.singersl.com/image.jpg";
    private static final String FAILED = "failed";

    private SliderImageService sliderImageService;

    @Mock
    private BusinessSliderImageRepository sliderImageRepository;

    @Mock
    private MerchantService merchantService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        sliderImageService = new SliderImageService(sliderImageRepository, merchantService);
    }

    /**
     * Add slider images
     */
    @Test
    void Should_ReturnSliderImageListResponseDto_When_AddingSliderImagesIsSuccessful() {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        BusinessSliderImage savedImage = getSampleBusinessSliderImage();

        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        doNothing().when(sliderImageRepository).deleteByMerchantId(MERCHANT_ID);
        when(sliderImageRepository.save(any(BusinessSliderImage.class))).thenReturn(savedImage);

        SliderImageListResponseDto response = sliderImageService.addSliderImages(requestDto);

        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(sliderImageRepository).deleteByMerchantId(MERCHANT_ID);
        verify(sliderImageRepository, times(requestDto.getImages().size())).save(any(BusinessSliderImage.class));
        assertNotNull(response);
        assertEquals(MERCHANT_ID, response.getMerchantId());
        assertEquals(requestDto.getImages().size(), response.getImages().size());
    }

    @Test
    void Should_ThrowInvalidUserException_When_AddingSliderImagesWithInvalidMerchantId() {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        doThrow(new InvalidUserException("Invalid merchant id")).when(merchantService)
                .validateMerchantId(MERCHANT_ID);

        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                sliderImageService.addSliderImages(requestDto));

        assertTrue(exception.getMessage().contains("Invalid merchant id: " + MERCHANT_ID));
        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(sliderImageRepository, never()).deleteByMerchantId(anyString());
        verify(sliderImageRepository, never()).save(any(BusinessSliderImage.class));
    }

    @Test
    void Should_ThrowAuthServiceException_When_SavingSliderImagesIsFailed() {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        doNothing().when(sliderImageRepository).deleteByMerchantId(MERCHANT_ID);
        when(sliderImageRepository.save(any(BusinessSliderImage.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });

        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                sliderImageService.addSliderImages(requestDto));

        assertEquals("Failed to save slider images for merchantId: " + MERCHANT_ID, exception.getMessage());
        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(sliderImageRepository).deleteByMerchantId(MERCHANT_ID);
        verify(sliderImageRepository).save(any(BusinessSliderImage.class));
    }

    @Test
    void Should_ThrowAuthServiceException_When_DeletingSliderImagesIsFailed() {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        doThrow(new DataAccessException(FAILED) {
        }).when(sliderImageRepository).deleteByMerchantId(MERCHANT_ID);

        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                sliderImageService.addSliderImages(requestDto));

        assertEquals("Failed to save slider images for merchantId: " + MERCHANT_ID, exception.getMessage());
        verify(merchantService).validateMerchantId(MERCHANT_ID);
        verify(sliderImageRepository).deleteByMerchantId(MERCHANT_ID);
        verify(sliderImageRepository, never()).save(any(BusinessSliderImage.class));
    }

    /**
     * Get slider images by merchant id
     */
    @Test
    void Should_ReturnSliderImageListResponseDto_When_GettingSliderImagesByMerchantIdIsSuccessful() {
        List<BusinessSliderImage> images = getSampleBusinessSliderImageList();
        when(sliderImageRepository.findByMerchantId(MERCHANT_ID)).thenReturn(images);

        SliderImageListResponseDto response = sliderImageService.getSliderImagesByMerchantId(MERCHANT_ID);

        verify(sliderImageRepository).findByMerchantId(MERCHANT_ID);
        assertNotNull(response);
        assertEquals(MERCHANT_ID, response.getMerchantId());
        assertEquals(images.size(), response.getImages().size());
    }

    @Test
    void Should_ReturnEmptySliderImageListResponseDto_When_NoSliderImagesFoundForMerchantId() {
        List<BusinessSliderImage> emptyList = new ArrayList<>();
        when(sliderImageRepository.findByMerchantId(MERCHANT_ID)).thenReturn(emptyList);

        SliderImageListResponseDto response = sliderImageService.getSliderImagesByMerchantId(MERCHANT_ID);

        verify(sliderImageRepository).findByMerchantId(MERCHANT_ID);
        assertNotNull(response);
        assertEquals(MERCHANT_ID, response.getMerchantId());
        assertEquals(0, response.getImages().size());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingSliderImagesByMerchantIdIsFailed() {
        when(sliderImageRepository.findByMerchantId(MERCHANT_ID))
                .thenThrow(new DataAccessException(FAILED) {
                });

        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                sliderImageService.getSliderImagesByMerchantId(MERCHANT_ID));

        assertEquals("Failed to fetch slider images for merchantId: " + MERCHANT_ID, exception.getMessage());
        verify(sliderImageRepository).findByMerchantId(MERCHANT_ID);
    }

    /**
     * Helper methods
     */
    private SliderImageCreateRequestDto getSampleSliderImageCreateRequestDto() {
        SliderImageCreateRequestDto requestDto = new SliderImageCreateRequestDto();
        requestDto.setMerchantId(MERCHANT_ID);
        List<SliderImageItemDto> images = new ArrayList<>();
        SliderImageItemDto item1 = new SliderImageItemDto();
        item1.setImageUrl(IMAGE_URL);
        item1.setDisplayOrder(1);
        images.add(item1);
        SliderImageItemDto item2 = new SliderImageItemDto();
        item2.setImageUrl(IMAGE_URL);
        item2.setDisplayOrder(2);
        images.add(item2);
        requestDto.setImages(images);
        return requestDto;
    }

    private BusinessSliderImage getSampleBusinessSliderImage() {
        SliderImageItemDto itemDto = new SliderImageItemDto();
        itemDto.setImageUrl(IMAGE_URL);
        itemDto.setDisplayOrder(1);
        return new BusinessSliderImage(MERCHANT_ID, itemDto);
    }

    private List<BusinessSliderImage> getSampleBusinessSliderImageList() {
        List<BusinessSliderImage> images = new ArrayList<>();
        SliderImageItemDto itemDto1 = new SliderImageItemDto();
        itemDto1.setImageUrl(IMAGE_URL);
        itemDto1.setDisplayOrder(1);
        images.add(new BusinessSliderImage(MERCHANT_ID, itemDto1));
        SliderImageItemDto itemDto2 = new SliderImageItemDto();
        itemDto2.setImageUrl(IMAGE_URL);
        itemDto2.setDisplayOrder(2);
        images.add(new BusinessSliderImage(MERCHANT_ID, itemDto2));
        return images;
    }
}

