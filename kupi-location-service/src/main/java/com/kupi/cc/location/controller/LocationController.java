package com.kupi.cc.location.controller;

import com.kupi.cc.location.configuration.Translator;
import com.kupi.cc.location.domain.response.MerchantDealsCountWithLocationListResponseDto;
import com.kupi.cc.location.domain.response.DealListResponseDto;
import com.kupi.cc.location.domain.response.MerchantDealsWithLocationListResponseDto;
import com.kupi.cc.location.domain.response.MerchantListWithDistanceAndDealsResponseDto;
import com.kupi.cc.location.domain.response.NearbyActiveDealsCountResponseDto;
import com.kupi.cc.location.enums.DealFilterType;
import com.kupi.cc.location.enums.SuccessResponseStatusType;
import com.kupi.cc.location.exception.KupiLocationException;
import com.kupi.cc.location.service.CoreService;
import com.kupi.cc.location.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Location Controller
 */
@CrossOrigin
@Validated
@RestController
@Slf4j
@RequestMapping("/api/v1/location")
public class LocationController extends Controller {

    private final CoreService coreService;

    @Autowired
    public LocationController(Translator translator, 
                             CoreService coreService) {
        super(translator);
        this.coreService = coreService;
    }

    /**
     * This method returns merchant deals with branch locations within the configured radius.
     * If range, page, or size parameters are provided, returns deal count instead of full deals list.
     * The search radius is configured in application.yml (location.search-radius-km).
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param range search radius in kilometers (optional)
     * @param page page number (0-indexed, optional)
     * @param size page size (optional)
     * @return merchant deals with branch locations, or deal count if pagination params provided
     */
    @GetMapping("/deals")
    public ResponseEntity<ResponseWrapper> getDealsByLocation(
            @RequestParam("latitude") @Min(-90) @Max(90) double latitude,
            @RequestParam("longitude") @Min(-180) @Max(180) double longitude,
            @RequestParam(value = "range", required = false) Double range,
            @RequestParam(value = "page", required = false) @Min(0) Integer page,
            @RequestParam(value = "size", required = false) @Min(1) Integer size) {
        try {
            // If any pagination parameter is provided, return deal count format
            if (range != null || page != null || size != null) {
                log.info("Getting deals count by location - lat: {}, lon: {}, range: {} km, page: {}, size: {}", 
                        latitude, longitude, range, page, size);
                
                MerchantDealsCountWithLocationListResponseDto responseDto = 
                        coreService.getMerchantDealsCountWithLocations(latitude, longitude, range, page, size);
                
                log.debug("Successfully returned {} merchants with deal counts for lat: {}, lon: {}, radius: {} km, page: {}, size: {}", 
                        responseDto.getDeals() != null ? responseDto.getDeals().size() : 0, 
                        latitude, longitude, responseDto.getSearchRadiusKm(), page, size);
                
                return getSuccessResponse(SuccessResponseStatusType.READ_DEALS_COUNT_BY_LOCATION, responseDto);
            } else {
                // Original behavior - return full deals list
                log.info("Getting deals by location - lat: {}, lon: {}", latitude, longitude);
                
                MerchantDealsWithLocationListResponseDto responseDto = coreService.getMerchantDealsWithLocations(latitude, longitude);
                
                log.debug("Successfully returned {} deals with locations for lat: {}, lon: {}, radius: {} km", 
                        responseDto.getDeals() != null ? responseDto.getDeals().size() : 0, 
                        latitude, longitude, responseDto.getSearchRadiusKm());
                
                return getSuccessResponse(SuccessResponseStatusType.READ_DEALS_BY_LOCATION, responseDto);
            }
        } catch (KupiLocationException e) {
            log.error("Getting deals by location failed for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        } catch (Exception e) {
            log.error("Unexpected error getting deals by location for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns merchant deals count with branch locations within the specified radius.
     * Returns deal count per merchant instead of full deals list, and includes merchant categories.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param range search radius in kilometers (optional)
     * @param page page number (0-indexed, optional, defaults to 0)
     * @param size page size (optional)
     * @return merchant deals count with branch locations and categories
     */
    @GetMapping("/deals/")
    public ResponseEntity<ResponseWrapper> getDealsCountByLocation(
            @RequestParam("latitude") @Min(-90) @Max(90) double latitude,
            @RequestParam("longitude") @Min(-180) @Max(180) double longitude,
            @RequestParam(value = "range", required = false) Double range,
            @RequestParam(value = "page", required = false) @Min(0) Integer page,
            @RequestParam(value = "size", required = false) @Min(1) Integer size) {
        try {
            log.info("Getting deals count by location - lat: {}, lon: {}, range: {} km, page: {}, size: {}", 
                    latitude, longitude, range, page, size);
            
            MerchantDealsCountWithLocationListResponseDto responseDto = 
                    coreService.getMerchantDealsCountWithLocations(latitude, longitude, range, page, size);
            
            log.debug("Successfully returned {} merchants with deal counts for lat: {}, lon: {}, radius: {} km, page: {}, size: {}", 
                    responseDto.getDeals() != null ? responseDto.getDeals().size() : 0, 
                    latitude, longitude, responseDto.getSearchRadiusKm(), page, size);
            
            return getSuccessResponse(SuccessResponseStatusType.READ_DEALS_COUNT_BY_LOCATION, responseDto);
        } catch (KupiLocationException e) {
            log.error("Getting deals count by location failed for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        } catch (Exception e) {
            log.error("Unexpected error getting deals count by location for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns nearby merchants with active deals count and distance from the user's location.
     * Only merchants with at least one active deal are returned.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param range search radius in kilometers (optional, defaults to configured search radius)
     * @param searchTerm search term to filter merchants by name (optional)
     * @param page page number (0-indexed, optional, defaults to 0)
     * @param size page size (optional)
     * @return merchants with categories, distance, and active deals count
     */
    @GetMapping("/merchants")
    public ResponseEntity<ResponseWrapper> getMerchantsByLocation(
            @RequestParam("latitude") @Min(-90) @Max(90) double latitude,
            @RequestParam("longitude") @Min(-180) @Max(180) double longitude,
            @RequestParam(value = "range", required = false) Double range,
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "page", required = false) @Min(0) Integer page,
            @RequestParam(value = "size", required = false) @Min(1) Integer size) {
        try {
            log.info("Getting merchants by location - lat: {}, lon: {}, range: {} km, searchTerm: {}, page: {}, size: {}",
                    latitude, longitude, range, searchTerm, page, size);

            MerchantListWithDistanceAndDealsResponseDto responseDto =
                    coreService.getNearbyMerchantsWithActiveDeals(latitude, longitude, range, searchTerm, page, size);

            log.debug("Successfully returned {} merchants for lat: {}, lon: {}, radius: {} km, page: {}, size: {}, totalElements: {}, totalPages: {}",
                    responseDto.getMerchants() != null ? responseDto.getMerchants().size() : 0,
                    latitude, longitude, responseDto.getSearchRadiusKm(), 
                    responseDto.getPage(), responseDto.getSize(), 
                    responseDto.getTotalElements(), responseDto.getTotalPages());

            return getSuccessResponse(SuccessResponseStatusType.READ_MERCHANTS_BY_LOCATION, responseDto);
        } catch (KupiLocationException e) {
            log.error("Getting merchants by location failed for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        } catch (Exception e) {
            log.error("Unexpected error getting merchants by location for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns nearby deals with filters (Deals of the Day, Recommended, Recently Expiring) and search term.
     * Fixed distance deals endpoint with filtering capabilities.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param range search radius in kilometers (optional, defaults to configured deals search radius)
     * @param searchTerm search term to filter deals by title (optional)
     * @param filterType filter type: DEALS_OF_THE_DAY, RECOMMENDED, or RECENTLY_EXPIRING
     * @param page page number (0-indexed, optional, defaults to 0)
     * @param size page size (optional)
     * @return filtered deals with locations and pagination
     */
    @GetMapping("/deals/filtered")
    public ResponseEntity<ResponseWrapper> getNearbyDealsWithFilters(
            @RequestParam("latitude") @Min(-90) @Max(90) double latitude,
            @RequestParam("longitude") @Min(-180) @Max(180) double longitude,
            @RequestParam(value = "range", required = false) Double range,
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "filterType", required = false, defaultValue = "RECOMMENDED") String filterType,
            @RequestParam(value = "page", required = false) @Min(0) Integer page,
            @RequestParam(value = "size", required = false) @Min(1) Integer size) {
        try {
            // Parse filter type
            DealFilterType dealFilterType;
            try {
                dealFilterType = DealFilterType.valueOf(filterType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid filterType: {}. Using RECOMMENDED as default.", filterType);
                dealFilterType = DealFilterType.RECOMMENDED;
            }

            log.info("Getting nearby deals with filters - lat: {}, lon: {}, range: {} km, searchTerm: {}, filterType: {}, page: {}, size: {}",
                    latitude, longitude, range, searchTerm, dealFilterType, page, size);

            MerchantDealsWithLocationListResponseDto responseDto =
                    coreService.getNearbyDealsWithFilters(latitude, longitude, range, searchTerm, dealFilterType, page, size);

            log.debug("Successfully returned {} deals for lat: {}, lon: {}, radius: {} km, filterType: {}, page: {}, size: {}, totalElements: {}, totalPages: {}",
                    responseDto.getDeals() != null ? responseDto.getDeals().size() : 0,
                    latitude, longitude, responseDto.getSearchRadiusKm(), dealFilterType,
                    responseDto.getPage(), responseDto.getSize(),
                    responseDto.getTotalElements(), responseDto.getTotalPages());

            return getSuccessResponse(SuccessResponseStatusType.READ_DEALS_BY_LOCATION, responseDto);
        } catch (KupiLocationException e) {
            log.error("Getting nearby deals with filters failed for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        } catch (Exception e) {
            log.error("Unexpected error getting nearby deals with filters for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        }
    }

    /**
     * Returns nearby deals with the same filters as /deals/filtered but as a flat deal list
     * (DealListResponseDto – same structure as core service's deal list) with pagination.
     *
     * @param latitude  latitude
     * @param longitude longitude
     * @param range    search radius in kilometers (optional)
     * @param searchTerm search term (optional)
     * @param filterType DEALS_OF_THE_DAY, RECOMMENDED, or RECENTLY_EXPIRING
     * @param page      page number (0-indexed, optional)
     * @param size      page size (optional)
     * @return DealListResponseDto (deals + totalItems, totalPages, page, size)
     */
    @GetMapping("/deals/filtered/list")
    public ResponseEntity<ResponseWrapper> getNearbyDealsWithFiltersAsDealList(
            @RequestHeader(name = USER_ID_HEADER, required = false) String userId,
            @RequestParam("latitude") @Min(-90) @Max(90) double latitude,
            @RequestParam("longitude") @Min(-180) @Max(180) double longitude,
            @RequestParam(value = "range", required = false) Double range,
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "filterType", required = false, defaultValue = "RECOMMENDED") String filterType,
            @RequestParam(value = "page", required = false) @Min(0) Integer page,
            @RequestParam(value = "size", required = false) @Min(1) Integer size) {
        try {
            DealFilterType dealFilterType;
            try {
                dealFilterType = DealFilterType.valueOf(filterType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid filterType: {}. Using RECOMMENDED as default.", filterType);
                dealFilterType = DealFilterType.RECOMMENDED;
            }

            log.info("Getting nearby deals as deal list - lat: {}, lon: {}, range: {} km, searchTerm: {}, filterType: {}, page: {}, size: {}",
                    latitude, longitude, range, searchTerm, dealFilterType, page, size);

            DealListResponseDto responseDto =
                    coreService.getNearbyDealsWithFiltersAsDealList(latitude, longitude, range, searchTerm, dealFilterType, page, size, userId);

            log.debug("Successfully returned {} deals for lat: {}, lon: {}, filterType: {}, page: {}, size: {}, totalItems: {}, totalPages: {}",
                    responseDto.getDeals() != null ? responseDto.getDeals().size() : 0,
                    latitude, longitude, dealFilterType,
                    responseDto.getPage(), responseDto.getSize(),
                    responseDto.getTotalItems(), responseDto.getTotalPages());

            return getSuccessResponse(SuccessResponseStatusType.READ_DEALS_BY_LOCATION, responseDto);
        } catch (KupiLocationException e) {
            log.error("Getting nearby deals as deal list failed for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        } catch (Exception e) {
            log.error("Unexpected error getting nearby deals as deal list for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns the total count of nearby active deals for a given location.
     * The search radius is configured in application.yml (location.deals-search-radius-km) or can be specified.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param range search radius in kilometers (optional, defaults to configured deals search radius)
     * @return total count of nearby active deals
     */
    @GetMapping("/deals/count")
    public ResponseEntity<ResponseWrapper> getNearbyActiveDealsCount(
            @RequestParam("latitude") @Min(-90) @Max(90) double latitude,
            @RequestParam("longitude") @Min(-180) @Max(180) double longitude,
            @RequestParam(value = "range", required = false) Double range) {
        try {
            log.info("Getting nearby active deals count - lat: {}, lon: {}, range: {} km",
                    latitude, longitude, range);

            NearbyActiveDealsCountResponseDto responseDto =
                    coreService.getNearbyActiveDealsCount(latitude, longitude, range);

            log.debug("Successfully returned active deals count: {} for lat: {}, lon: {}, radius: {} km",
                    responseDto.getActiveDealsCount(),
                    latitude, longitude, responseDto.getSearchRadiusKm());

            return getSuccessResponse(SuccessResponseStatusType.READ_NEARBY_ACTIVE_DEALS_COUNT, responseDto);
        } catch (KupiLocationException e) {
            log.error("Getting nearby active deals count failed for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        } catch (Exception e) {
            log.error("Unexpected error getting nearby active deals count for lat: {}, lon: {}", latitude, longitude, e);
            return getInternalServerError();
        }
    }
}

