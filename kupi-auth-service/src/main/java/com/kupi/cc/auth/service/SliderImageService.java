package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.BusinessSliderImage;
import com.kupi.cc.auth.domain.request.SliderImageCreateRequestDto;
import com.kupi.cc.auth.domain.request.SliderImageItemDto;
import com.kupi.cc.auth.domain.response.BulkSliderImageListResponseDto;
import com.kupi.cc.auth.domain.response.SliderImageItemResponseDto;
import com.kupi.cc.auth.domain.response.SliderImageListResponseDto;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.repository.BusinessSliderImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SliderImageService {

    private final BusinessSliderImageRepository sliderImageRepository;
    private final MerchantService merchantService;

    public SliderImageService(BusinessSliderImageRepository sliderImageRepository, MerchantService merchantService) {
        this.sliderImageRepository = sliderImageRepository;
        this.merchantService = merchantService;
    }

    @Transactional
    public SliderImageListResponseDto addSliderImages(SliderImageCreateRequestDto requestDto) {
        try {
            merchantService.validateMerchantId(requestDto.getMerchantId());
            sliderImageRepository.deleteByMerchantId(requestDto.getMerchantId());
            List<BusinessSliderImage> saved = new ArrayList<>();
            for (SliderImageItemDto dto : requestDto.getImages()) {
                BusinessSliderImage image = new BusinessSliderImage(requestDto.getMerchantId(), dto);
                saved.add(sliderImageRepository.save(image));
            }
            List<SliderImageItemResponseDto> responseItems = saved.stream()
                    .map(SliderImageItemResponseDto::new)
                    .collect(Collectors.toList());
            return new SliderImageListResponseDto(requestDto.getMerchantId(), responseItems);
        } catch (InvalidUserException e) {
            throw new InvalidUserException("Invalid merchant id: " + requestDto.getMerchantId() + e);
        } catch (
                DataAccessException e) {
            throw new AuthServiceException("Failed to save slider images for merchantId: " + requestDto.getMerchantId(), e);
        }
    }

    public SliderImageListResponseDto getSliderImagesByMerchantId(String merchantId) {

        try {
            List<BusinessSliderImage> images =
                    sliderImageRepository.findByMerchantId(merchantId);

            List<SliderImageItemResponseDto> responseItems = images.stream()
                    .map(SliderImageItemResponseDto::new)
                    .collect(Collectors.toList());

            return new SliderImageListResponseDto(merchantId, responseItems);

        } catch (DataAccessException e) {
            throw new AuthServiceException(
                    "Failed to fetch slider images for merchantId: " + merchantId, e
            );
        }
    }

    /**
     * This method returns slider images for multiple merchants in bulk.
     *
     * @param merchantIds list of merchant ids
     * @return BulkSliderImageListResponseDto mapping merchantId -> images
     */
    public BulkSliderImageListResponseDto getSliderImagesByMerchantIds(List<String> merchantIds) {
        try {
            var resultMap = new HashMap<String, List<SliderImageItemResponseDto>>();
            if (merchantIds == null || merchantIds.isEmpty()) {
                return new BulkSliderImageListResponseDto(resultMap);
            }

            List<BusinessSliderImage> images = sliderImageRepository.findByMerchantIdIn(merchantIds);

            for (String merchantId : merchantIds) {
                resultMap.put(merchantId, new ArrayList<>());
            }

            for (BusinessSliderImage image : images) {
                String merchantId = image.getMerchantId();
                resultMap.computeIfAbsent(merchantId, k -> new ArrayList<>())
                        .add(new SliderImageItemResponseDto(image));
            }

            return new BulkSliderImageListResponseDto(resultMap);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to fetch bulk slider images for merchants", e);
        }
    }

}
