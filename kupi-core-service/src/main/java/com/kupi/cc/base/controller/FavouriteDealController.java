package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.InvalidDealException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.FavouriteDealService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Favourite Deal Controller
 * <p>
 * Provides endpoints for users to mark deals as favourite and remove them from favourites.
 * Later, these favourites can be used to enrich deal responses with an isFavourite flag.
 */
@Slf4j
@RestController
@RequestMapping("api/v1/favourites")
public class FavouriteDealController extends Controller {

    private final FavouriteDealService favouriteDealService;

    @Autowired
    public FavouriteDealController(Translator translator,
                                   FavouriteDealService favouriteDealService) {
        super(translator);
        this.favouriteDealService = favouriteDealService;
    }

    /**
     * Mark a deal as favourite for the authenticated user.
     *
     * @param userId user id from header
     * @param dealId deal id to mark as favourite
     * @return success or error response
     */
    @PostMapping("/deals/{dealId}")
    public ResponseEntity<ResponseWrapper> addFavouriteDeal(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @PathVariable String dealId) {
        try {
            log.debug("Adding favourite deal. userId: {}, dealId: {}", userId, dealId);
            favouriteDealService.addFavouriteDeal(userId, dealId);
            return getSuccessResponse(null, SuccessResponseStatusType.FAVOURITE_DEAL_ADDED);
        } catch (InvalidDealException e) {
            log.error("Invalid dealId: {} for userId: {} when adding favourite.", dealId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_DEAL_ID);
        } catch (KupiCoreException e) {
            log.error("Adding favourite deal failed. userId: {}, dealId: {}", userId, dealId, e);
            return getInternalServerError();
        }
    }

    /**
     * Remove a deal from favourites for the authenticated user.
     *
     * @param userId user id from header
     * @param dealId deal id to remove from favourites
     * @return success or error response
     */
    @DeleteMapping("/deals/{dealId}")
    public ResponseEntity<ResponseWrapper> removeFavouriteDeal(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @PathVariable String dealId) {
        try {
            log.debug("Removing favourite deal. userId: {}, dealId: {}", userId, dealId);
            favouriteDealService.removeFavouriteDeal(userId, dealId);
            return getSuccessResponse(null, SuccessResponseStatusType.FAVOURITE_DEAL_REMOVED);
        } catch (KupiCoreException e) {
            log.error("Removing favourite deal failed. userId: {}, dealId: {}", userId, dealId, e);
            return getInternalServerError();
        }
    }
}


