package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.OfferType;
import com.kupi.cc.base.domain.request.OfferTypeCreateRequestDto;
import com.kupi.cc.base.domain.request.OfferTypeUpdateRequestDto;
import com.kupi.cc.base.domain.response.OfferTypeListResponseDto;
import com.kupi.cc.base.domain.response.OfferTypeRespondDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.ExistingNameException;
import com.kupi.cc.base.exception.InvalidOfferTypeException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.UnsupportedDeleteAction;
import com.kupi.cc.base.service.OfferTypeService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * OfferType controller
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/request-a-deal/offer-type")
public class OfferTypeController extends Controller {

    private final OfferTypeService offerTypeService;

    @Autowired
    public OfferTypeController(Translator translator, OfferTypeService offerTypeService) {
        super(translator);
        this.offerTypeService = offerTypeService;
    }

    /**
     * Create offer type
     *
     * @param timeZone                  time zone
     * @param userId                    user id
     * @param offerTypeCreateRequestDto offerTypeRequestDto
     * @return OfferTypeCreateRespondDto
     */
    @PostMapping
    public ResponseEntity<ResponseWrapper> createOfferType(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                           @RequestHeader(name = USER_ID_HEADER) String userId,
                                                           @RequestBody OfferTypeCreateRequestDto offerTypeCreateRequestDto) {
        try {
            if (!offerTypeCreateRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            var offerType = new OfferType(offerTypeCreateRequestDto.getName());
            offerTypeService.saveOfferType(offerType);
            log.debug("Successfully created offer type. user id: {}, offer type response: {}", userId, offerType);
            return getSuccessResponse(new OfferTypeRespondDto(offerType.getId(), offerType.getName()),
                    SuccessResponseStatusType.CREATE_OFFER_TYPE);
        } catch (ExistingNameException e) {
            log.error("Cannot create offer type with existing offerType name: {}.", offerTypeCreateRequestDto.getName(), e);
            return getErrorResponse(ErrorResponseStatusType.EXISTING_OFFER_TYPE_NAME);
        } catch (KupiCoreException e) {
            log.error("Creating offer type was failed. userId: {}, time zone: {}, OfferRequest: {}", userId, timeZone,
                    offerTypeCreateRequestDto.toLogJson(), e);
            return getInternalServerError();
        }
    }

    /**
     * Get All offer types.
     *
     * @param timeZone time zone
     * @param userId   user id
     * @return offerTypeListResponseDto
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper> getAllOfferTypes(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                            @RequestHeader(name = USER_ID_HEADER) String userId) {
        try {
            var allOfferTypes = offerTypeService.getAllOfferTypes();
            log.debug("Successfully returned offer type list with the size of :{}. user id: {}",
                    allOfferTypes.size(), userId);
            var offerTypeListResponseDto = new OfferTypeListResponseDto(allOfferTypes);
            return getSuccessResponse(offerTypeListResponseDto, SuccessResponseStatusType.READ_OFFER_TYPE_LIST);

        } catch (KupiCoreException e) {
            log.error("Returning List of offer types was failed. userId: {}, time zone: {}.", userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * Update offerType.
     *
     * @param timeZone                  timezone
     * @param userId                    user id
     * @param offerTypeUpdateRequestDto offerTypeUpdateRequestDto
     * @return OfferTypeRespondDto
     */
    @PutMapping
    public ResponseEntity<ResponseWrapper> updateOfferType(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                           @RequestHeader(name = USER_ID_HEADER) String userId,
                                                           @RequestBody OfferTypeUpdateRequestDto offerTypeUpdateRequestDto) {
        try {
            if (!offerTypeUpdateRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            var offerTypeById = offerTypeService.getOfferTypeById(offerTypeUpdateRequestDto.getId());
            offerTypeService.updateOfferType(offerTypeById, offerTypeUpdateRequestDto.getName());
            return getSuccessResponse(new OfferTypeRespondDto(offerTypeById.getId(), offerTypeUpdateRequestDto.getName()),
                    SuccessResponseStatusType.UPDATE_OFFER_TYPE);
        } catch (ExistingNameException e) {
            log.error("Cannot update offer type with existing offerType name: {}.", offerTypeUpdateRequestDto.getName(), e);
            return getErrorResponse(ErrorResponseStatusType.EXISTING_OFFER_TYPE_NAME);
        } catch (InvalidOfferTypeException e) {
            log.error("Invalid OfferType Id: {} for update Offer Type.", offerTypeUpdateRequestDto.getId(), e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_OFFER_TYPE_ID);
        } catch (KupiCoreException e) {
            log.error("Updating offer type was failed. userId: {}, time zone: {}, OfferType Update Request: {}",
                    userId, timeZone, offerTypeUpdateRequestDto);
            return getInternalServerError();
        }
    }

    /**
     * Delete offer type.
     *
     * @param timeZone    time zone
     * @param userId      user id
     * @param offerTypeId offer type id
     * @return SuccessResponse / ErrorResponse
     */
    @DeleteMapping("{offerTypeId}")
    public ResponseEntity<ResponseWrapper> deleteOfferType(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                           @RequestHeader(name = USER_ID_HEADER) String userId,
                                                           @PathVariable String offerTypeId) {
        try {
            offerTypeService.deleteOfferType(offerTypeId);
            return getSuccessResponse(null, SuccessResponseStatusType.DELETE_OFFER_TYPE);
        } catch (UnsupportedDeleteAction e) {
            log.error("OfferType id: {} has request a deals", offerTypeId, e);
            return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_OFFER_TYPE_DELETE);
        } catch (InvalidOfferTypeException e) {
            log.error("Invalid OfferType Id: {} for delete offer type.", offerTypeId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_OFFER_TYPE_ID);
        } catch (KupiCoreException e) {
            log.error("Deleting offer type was failed by offerTypeId: {}, userId: {} and time zone: {}.", offerTypeId,
                    userId, timeZone);
            return getInternalServerError();
        }
    }
}
