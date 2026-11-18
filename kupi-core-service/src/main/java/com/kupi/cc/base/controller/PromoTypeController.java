package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.PromoType;
import com.kupi.cc.base.domain.request.PromoTypeRequestDto;
import com.kupi.cc.base.domain.request.PromoTypeUpdateRequestDto;
import com.kupi.cc.base.domain.response.PromoTypeResponseDto;
import com.kupi.cc.base.domain.response.PromoTypeListResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.ExistingNameException;
import com.kupi.cc.base.exception.InvalidCategoryException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.PromoTypeService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * PromoType Controller
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/promo-types")
public class PromoTypeController extends Controller {

    private final PromoTypeService promoTypeService;
    Logger logger = LoggerFactory.getLogger(PromoTypeController.class);

    @Autowired
    public PromoTypeController(PromoTypeService promoTypeService, Translator translator) {
        super(translator);
        this.promoTypeService = promoTypeService;
    }


    /**
     * Create a new promo type
     *
     * @param timeZone            time zone
     * @param promoTypeRequestDto promo type request
     * @return created promo type response
     */
    @Secured({ADMIN_ROLE})
    @PostMapping("")
    public ResponseEntity<ResponseWrapper> createPromoType(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Valid @RequestBody PromoTypeRequestDto promoTypeRequestDto
    ) {
        try {
            if (!promoTypeRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            PromoType promoType = promoTypeService.createPromoType(promoTypeRequestDto);
            var promoTypeResponseDto = new PromoTypeResponseDto(promoType, timeZone);
            String objectToJson = promoTypeResponseDto.toLogJson();
            logger.debug("Successfully created promo type. Promo type response {}",
                    objectToJson);
            return getSuccessResponse(promoTypeResponseDto, SuccessResponseStatusType.CREATE_PROMO_TYPE);
        } catch (ExistingNameException e) {
            logger.error("Existing promo type name: {} for create promo type.", promoTypeRequestDto.getName(), e);
            return getErrorResponse(ErrorResponseStatusType.EXISTING_PROMO_TYPE_NAME);
        } catch (KupiCoreException e) {
            logger.error("Creating new promo type was failed.", e);
            return getInternalServerError();
        }
    }

    /**
     * Update an existing promo type
     *
     * @param timeZone                  time zone
     * @param promoTypeUpdateRequestDto promo type update request
     * @return updated promo type response
     */
    @Secured({ADMIN_ROLE})
    @PutMapping("")
    public ResponseEntity<ResponseWrapper> updatePromoType(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @Valid @RequestBody PromoTypeUpdateRequestDto promoTypeUpdateRequestDto
    ) {
        try {
            if (!promoTypeUpdateRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            PromoType updatedPromoType = promoTypeService.updatePromoType(promoTypeUpdateRequestDto);
            var promoTypeResponseDto = new PromoTypeResponseDto(updatedPromoType, timeZone);
            String objectToJson = promoTypeResponseDto.toLogJson();
            logger.debug("Successfully updated promo type. Promo type response {}", objectToJson);
            return getSuccessResponse(promoTypeResponseDto, SuccessResponseStatusType.UPDATE_PROMO_TYPE);
        } catch (ExistingNameException e) {
            logger.error("Existing promo type name: {} for update promo type.", promoTypeUpdateRequestDto.getName(), e);
            return getErrorResponse(ErrorResponseStatusType.EXISTING_PROMO_TYPE_NAME);
        } catch (InvalidCategoryException e) {
            logger.error("Invalid promo type Id: {} for update promo type.",
                    promoTypeUpdateRequestDto.getId(), e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            logger.error("Updating existing promo type by promoTypeId: {} was failed.",
                    promoTypeUpdateRequestDto.getId(), e);
            return getInternalServerError();
        }
    }

    /**
     * Get all promo types by category ID
     *
     * @param timeZone   time zone
     * @param categoryId category ID
     * @return list of promo types response
     */
    @Secured({ADMIN_ROLE, MERCHANT_ROLE})
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ResponseWrapper> getPromoTypesByCategoryId(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable String categoryId
    ) {
        try {
            List<PromoType> promoTypes = promoTypeService.getPromoTypesByCategoryId(categoryId);
            PromoTypeListResponseDto promoTypeListResponseDto = new PromoTypeListResponseDto(promoTypes, timeZone);

            logger.debug("Successfully retrieved promo types by category ID: {}. Total promo types: {}",
                    categoryId, promoTypes.size());

            return getSuccessResponse(promoTypeListResponseDto, SuccessResponseStatusType.READ_PROMO_TYPES);
        } catch (InvalidCategoryException e) {
            logger.error("Invalid category ID: {} for get promo types by category.", categoryId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_CATEGORY_ID);
        } catch (KupiCoreException e) {
            logger.error("Getting promo types by category ID: {} was failed.", categoryId, e);
            return getInternalServerError();
        }
    }
}
