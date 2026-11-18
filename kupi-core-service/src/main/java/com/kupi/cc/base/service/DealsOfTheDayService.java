package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.*;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.DealsOfTheDayEventRequestDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.domain.response.DealsOfTheDayBasicEventResponseDto;
import com.kupi.cc.base.domain.response.DealsOfTheDayEventSummaryResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.UpcomingDealsOfTheDayHistoryGroupResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.DealsOfTheDayHistoryFilter;
import com.kupi.cc.base.enums.LimitExceededType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.aop.AopInvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Deals of the day service
 */
@Service
@Slf4j
public class DealsOfTheDayService {

    private static final String ALL = "ALL";
    private static final String VALID_ON = "validOn";
    private static final String SEPARATOR = ", ";
    private static final String EMPTY_TEXT = "";
    private static final int SIZE = 250;
    private static final int WEEK_DAYS = 7;
    private static final int SIX = 6;
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int MINUS_DAYS = -1;
    private static final String INVALID_DEAL = "Invalid deal Id: ";
    private static final String FAILED_TO_READ_DEAL = "Read deals of the day from database was failed.";
    private final DealsOfTheDayRepository dealsOfTheDayRepository;
    private final BankDealsOfTheDayRepository bankDealsOfTheDayRepository;
    private final DealRepository dealRepository;
    private final BankDealRepository bankDealRepository;
    private final DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService;
    private final DealSearchIndexRepository dealSearchIndexRepository;
    private final DealsOfTheDayEventRepository dealsOfTheDayEventRepository;
    private final DealsOfTheDayEventSearchIndexRepository dealsOfTheDayEventSearchIndexRepository;
    private final AuthUserService authUserService;
    private final DealService dealService;
    private final DealSearchService dealSearchService;
    private final SubscriberService subscriberService;
    private final long dealsOfTheDayDealLimit;


    @Autowired
    public DealsOfTheDayService(@Value("${dealsOfTheDayEvent.dealLimit}") long dealsOfTheDayDealLimit,
                                DealsOfTheDayRepository dealsOfTheDayRepository,
                                BankDealsOfTheDayRepository bankDealsOfTheDayRepository, DealRepository dealRepository,
                                BankDealRepository bankDealRepository, DealSearchService dealSearchService,
                                DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService,
                                DealSearchIndexRepository dealSearchIndexRepository, DealService dealService,
                                DealsOfTheDayEventRepository dealsOfTheDayEventRepository, AuthUserService authUserService,
                                DealsOfTheDayEventSearchIndexRepository dealsOfTheDayEventSearchIndexRepository,
                                SubscriberService subscriberService) {
        this.dealsOfTheDayRepository = dealsOfTheDayRepository;
        this.bankDealsOfTheDayRepository = bankDealsOfTheDayRepository;
        this.dealRepository = dealRepository;
        this.bankDealRepository = bankDealRepository;
        this.dealsOfTheDaySearchIndexService = dealsOfTheDaySearchIndexService;
        this.dealSearchIndexRepository = dealSearchIndexRepository;
        this.dealsOfTheDayEventRepository = dealsOfTheDayEventRepository;
        this.dealsOfTheDayEventSearchIndexRepository = dealsOfTheDayEventSearchIndexRepository;
        this.authUserService = authUserService;
        this.dealService = dealService;
        this.dealSearchService = dealSearchService;
        this.dealsOfTheDayDealLimit = dealsOfTheDayDealLimit;
        this.subscriberService = subscriberService;
    }

    /**
     * This method processes deals of the day for a specific date and user type.
     * Only processes MERCHANT deals (ignores BANK deals).
     * This is called immediately when deals of the day events are created or updated.
     *
     * @param eventDate eventDate
     * @param userType  MERCHANT/BANK (only MERCHANT is processed)
     */
    @Transactional
    public void processDealsOfTheDayForDate(java.sql.Date eventDate, UserType userType) {
        try {
            // Only process MERCHANT deals
            if (userType != UserType.MERCHANT) {
                log.debug("Skipping deals of the day processing for userType: {}. Only MERCHANT deals are processed.", userType);
                return;
            }
            
            log.info("Processing deals of the day for date: {} and userType: {}", eventDate, userType);
            
            // Get all events for this date and user type
            List<DealsOfTheDayEvent> dealsOfTheDayEventList = 
                    dealsOfTheDayEventRepository.findByValidOnAndUserType(eventDate, userType);
            
            if (dealsOfTheDayEventList.isEmpty()) {
                log.debug("No deals of the day events found for date: {} and userType: {}", eventDate, userType);
                return;
            }
            
            // Get all deal IDs from all events
            List<String> allDealIds = new ArrayList<>();
            for (DealsOfTheDayEvent event : dealsOfTheDayEventList) {
                allDealIds.addAll(event.getDeals());
            }
            
            if (allDealIds.isEmpty()) {
                log.debug("No deals found in events for date: {} and userType: {}", eventDate, userType);
                return;
            }
            
            // Get deals from repository
            List<Deal> dealList = new ArrayList<>();
            for (int currentPage = ZERO; currentPage < 1; currentPage++) {
                Pageable pageable = PageRequest.of(currentPage, SIZE);
                Page<Deal> dealPage = dealRepository.findByIdIn(pageable, allDealIds);
                dealList.addAll(dealPage.getContent());
                if (dealPage.getTotalPages() > 1) {
                    // Process all pages
                    for (int page = 1; page < dealPage.getTotalPages(); page++) {
                        Pageable nextPageable = PageRequest.of(page, SIZE);
                        Page<Deal> nextDealPage = dealRepository.findByIdIn(nextPageable, allDealIds);
                        dealList.addAll(nextDealPage.getContent());
                    }
                }
            }
            
            if (dealList.isEmpty()) {
                log.debug("No deals found in database for dealIds: {}", allDealIds);
                return;
            }
            
            // Remove existing deals of the day for this date (MERCHANT only)
            dealsOfTheDayRepository.disableActiveDealsForDate(eventDate);
            
            // Create new deals of the day entries
            List<DealSearchIndex> dealSearchIndexList = getDealSearchIndexListByDealList(dealList);
            List<DealsOfTheDay> dealsOfTheDayList = createDealsOfTheDayListForDate(dealList, eventDate);
            List<DealOfTheDaySearchIndex> dealOfTheDaySearchIndexList =
                    createDealsOfTheDaySearchIndexListForDate(dealSearchIndexList, eventDate);
            
            // Save new deals of the day
            dealsOfTheDayRepository.saveAll(dealsOfTheDayList);
            dealsOfTheDaySearchIndexService.saveAll(dealOfTheDaySearchIndexList);
            dealService.setDealsOfTheDayFlagForMerchantDeal(dealList);
            dealSearchService.setDealsOfTheDayFlagForDealSearch(dealSearchIndexList);
            
            log.info("Successfully processed {} deals of the day for date: {} and userType: {}", 
                    dealList.size(), eventDate, userType);
        } catch (DataAccessException e) {
            log.error("Processing deals of the day for date: {} and userType: {} failed.", eventDate, userType, e);
            throw new KupiCoreException("Processing deals of the day was failed for date: " + eventDate, e);
        }
    }

    /**
     * This method executes based on the configured cron expression to
     * add selected active deals
     * and deactivate existing "deals of the day".
     * 
     * NOTE: This scheduler is kept for manual trigger only. 
     * Deals of the day are now processed immediately when events are created/updated.
     *
     * <p>
     * Examples:
     * - Run daily at midnight → cron = "0 0 0 * * *"
     * - Run every 30 minutes → cron = "0 0/30 * * * *"
     * - Run every 20 seconds → fixedRate = 20000 (if using fixedRate)
     */
    // @Scheduled(cron = "${dealsOfTheDayEvent.schedule}", zone = "GMT+05:30")
    @Transactional
    public void saveDealsOfTheDay() {
        try {
            log.info("Deals of the day scheduler started.");
            dealsOfTheDayRepository.disableActiveDeals();
            bankDealsOfTheDayRepository.disableActiveDeals();
            dealsOfTheDaySearchIndexService.deleteAll();
            dealService.disableIsDealOfTheDayFromMerchantDeals();
            dealService.disableIsDealOfTheDayFromBankDeals();
            dealSearchService.disableIsDealOfTheDayFromDealSearch();

            List<DealsOfTheDayEvent> dealsOfTheDayEventList = getDealsOfTheDayEvent(UserType.MERCHANT);
            List<Deal> dealList = getDealsFromDealsOfTheDayEvents(dealsOfTheDayEventList);
            List<DealSearchIndex> dealSearchIndexList = getDealSearchIndexListByDealList(dealList);
            List<DealsOfTheDay> dealsOfTheDayList = createDealsOfTheDayList(dealList);
            List<DealOfTheDaySearchIndex> dealOfTheDaySearchIndexList =
                    createDealsOfTheDaySearchIndexList(dealSearchIndexList);
            dealsOfTheDayRepository.saveAll(dealsOfTheDayList);
            dealsOfTheDaySearchIndexService.saveAll(dealOfTheDaySearchIndexList);
            dealService.setDealsOfTheDayFlagForMerchantDeal(dealList);
            dealSearchService.setDealsOfTheDayFlagForDealSearch(dealSearchIndexList);
            saveBankDealsOfTheDay();
            log.info("Deals of the day scheduler ended successfully.");
        } catch (DataAccessException e) {
            log.error("Deals of the day scheduler failed to run.", e);
            throw new KupiCoreException(FAILED_TO_READ_DEAL, e);
        }
    }

    /**
     * Save randomly selected active 10 bank deals and update the existing active bank deals of the days to false.
     */
    @Transactional
    public void saveBankDealsOfTheDay() {
        try {
            List<DealsOfTheDayEvent> dealsOfTheDayEventList = getDealsOfTheDayEvent(UserType.BANK);
            List<BankDeal> bankDealList = getBankDealsFromDealsOfTheDayEvents(dealsOfTheDayEventList);
            List<DealSearchIndex> dealSearchIndexList = getDealSearchIndexListByBankDealList(bankDealList);
            List<BankDealsOfTheDay> dealsOfTheDayListForBank = createDealsOfTheDayListForBank(bankDealList);
            List<DealOfTheDaySearchIndex> dealOfTheDaySearchIndexList =
                    createDealsOfTheDaySearchIndexList(dealSearchIndexList);
            bankDealsOfTheDayRepository.saveAll(dealsOfTheDayListForBank);
            dealsOfTheDaySearchIndexService.saveAll(dealOfTheDaySearchIndexList);
            dealService.setDealsOfTheDayFlagForBankDeal(bankDealList);
            dealSearchService.setDealsOfTheDayFlagForDealSearch(dealSearchIndexList);
        } catch (DataAccessException e) {
            log.error("Deals of the day scheduler for bank deals failed to run.", e);
            throw new KupiCoreException(FAILED_TO_READ_DEAL, e);
        }
    }

    /**
     * This method get deals of the day page and convert it into deals page
     *
     * @return deal page
     */
    public Page<Deal> dealsOfTheDayPageForMerchant(Pageable pageable, String categoryId,
                                                   String merchantId, String searchTerm, UserType dealSource) {
        try {
            var dealOfTheDaySearchIndexPage =
                    dealsOfTheDaySearchIndexService
                            .getAllSearchDeals(pageable, categoryId, merchantId, searchTerm, dealSource);
            List<Deal> dealList = new ArrayList<>();
            dealOfTheDaySearchIndexPage.forEach(dealOfTheDaySearchIndex -> {

                if (UserType.MERCHANT.equals(dealSource)) {
                    var optionalDealFromDb = dealRepository.findById(dealOfTheDaySearchIndex.getId());
                    if (optionalDealFromDb.isPresent()) {
                        dealList.add(optionalDealFromDb.get());
                    } else {
                        throw new InvalidDealException(INVALID_DEAL + dealOfTheDaySearchIndex.getId());
                    }
                } else {
                    Optional<BankDeal> optionalBankDealFromDb = bankDealRepository.findById(dealOfTheDaySearchIndex.getId());
                    if (optionalBankDealFromDb.isPresent()) {
                        dealList.add(new Deal(optionalBankDealFromDb.get()));
                    } else {
                        throw new InvalidDealException(INVALID_DEAL + dealOfTheDaySearchIndex.getId());
                    }
                }
            });
            return new PageImpl<>(dealList);
        } catch (DataAccessException e) {
            throw new KupiCoreException(FAILED_TO_READ_DEAL, e);
        }
    }

    /**
     * This method create deals of the day list from deals list
     *
     * @param dealList dealList
     * @return deals of the day list
     */
    private List<DealsOfTheDay> createDealsOfTheDayList(List<Deal> dealList) {
        var dealsOfTheDays = new ArrayList<DealsOfTheDay>();
        dealList.forEach(deal -> dealsOfTheDays.add(new DealsOfTheDay(deal)));
        return dealsOfTheDays;
    }

    /**
     * This method create deals of the day list from deals list for a specific date
     *
     * @param dealList dealList
     * @param applyOn  applyOn date (java.sql.Date)
     * @return deals of the day list
     */
    private List<DealsOfTheDay> createDealsOfTheDayListForDate(List<Deal> dealList, java.sql.Date applyOn) {
        var dealsOfTheDays = new ArrayList<DealsOfTheDay>();
        dealList.forEach(deal -> dealsOfTheDays.add(new DealsOfTheDay(deal, applyOn)));
        return dealsOfTheDays;
    }

    /**
     * This method create deals of the day list from bank deals list.
     *
     * @param bankDealList bankDealList
     * @return bank deals of the day list.
     */
    private List<BankDealsOfTheDay> createDealsOfTheDayListForBank(List<BankDeal> bankDealList) {
        List<BankDealsOfTheDay> bankDealsOfTheDay = new ArrayList<>();
        bankDealList.forEach(bankDeal -> bankDealsOfTheDay.add(new BankDealsOfTheDay(bankDeal)));
        return bankDealsOfTheDay;
    }

    /**
     * This method create deals of the day search index from deals list
     *
     * @param dealSearchIndexList dealSearchIndexList
     * @return DealOfTheDaySearchIndex list
     */
    private List<DealOfTheDaySearchIndex> createDealsOfTheDaySearchIndexList(List<DealSearchIndex> dealSearchIndexList) {
        var dealOfTheDaySearchIndexArrayList = new ArrayList<DealOfTheDaySearchIndex>();
        dealSearchIndexList.forEach(dealSearchIndex ->
                dealOfTheDaySearchIndexArrayList.add(new DealOfTheDaySearchIndex(dealSearchIndex)));
        return dealOfTheDaySearchIndexArrayList;
    }

    /**
     * This method create deals of the day search index from deals list for a specific date
     *
     * @param dealSearchIndexList dealSearchIndexList
     * @param applyOn applyOn date
     * @return DealOfTheDaySearchIndex list
     */
    private List<DealOfTheDaySearchIndex> createDealsOfTheDaySearchIndexListForDate(List<DealSearchIndex> dealSearchIndexList, java.sql.Date applyOn) {
        var dealOfTheDaySearchIndexArrayList = new ArrayList<DealOfTheDaySearchIndex>();
        dealSearchIndexList.forEach(dealSearchIndex ->
                dealOfTheDaySearchIndexArrayList.add(new DealOfTheDaySearchIndex(dealSearchIndex, applyOn)));
        return dealOfTheDaySearchIndexArrayList;
    }

    /**
     * This method is used to get deals of the day history.
     *
     * @param timeZone   timeZone
     * @param searchTerm searchTerm
     * @param filter     filter
     * @param userId     userId
     * @return deals of the day list.
     */
    public List<DealsOfTheDayBasicEventResponseDto> getDealsOfTheDayHistory(String timeZone, String searchTerm,
                                                                            DealsOfTheDayHistoryFilter filter,
                                                                            String userId, UserType userType) {

        LocalDate thisWeekMonday =
                LocalDate.now(ZoneId.of(timeZone)).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Date today = new Date();
        Date yesterday = DateUtils.addDays(today, MINUS_DAYS);
        List<DealsOfTheDayBasicEventResponseDto> historyResponseDto = new ArrayList<>();
        try {
            switch (filter) {
                case TODAY:
                    return createDealsOfTheDayHistoryResponse(timeZone, searchTerm, userId, today, userType);
                case YESTERDAY:
                    return createDealsOfTheDayHistoryResponse(timeZone, searchTerm, userId, yesterday, userType);
                case LAST_WEEK:
                    return createDealsOfTheDayLastWeekHistoryResponse(timeZone, searchTerm, userId,
                            thisWeekMonday, userType);
            }
            return historyResponseDto;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to get deals of the day history from db", e);
        }
    }

    /**
     * This method is used to get upcoming deals of the day history (today and future dates),
     * grouped by deals-of-the-day date.
     *
     * @param timeZone   timeZone
     * @param searchTerm searchTerm
     * @param userId     userId
     * @param userType   userType
     * @return deals of the day list grouped by date.
     */
    public List<UpcomingDealsOfTheDayHistoryGroupResponseDto> getUpcomingDealsOfTheDayHistory(String timeZone,
                                                                                              String searchTerm,
                                                                                              String userId,
                                                                                              UserType userType) {

        List<UpcomingDealsOfTheDayHistoryGroupResponseDto> responseDto = new ArrayList<>();
        try {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DATE, -ONE);
            Date yesterday = calendar.getTime();

            int currentPage = ZERO;
            int totalPages = ONE;
            Set<java.sql.Date> upcomingDates = new TreeSet<>();

            while (currentPage < totalPages) {
                Pageable pageable = PageRequest.of(currentPage, SIZE, Sort.by(VALID_ON).ascending());
                Page<DealsOfTheDayEventSearchIndex> upcomingEventsPage;

                if (userId.equals(ALL) && searchTerm.equals(ALL)) {
                    upcomingEventsPage = dealsOfTheDayEventSearchIndexRepository
                            .findByValidOnGreaterThanAndUserType(pageable, yesterday, userType);
                } else if (!userId.equals(ALL) && searchTerm.equals(ALL)) {
                    upcomingEventsPage = dealsOfTheDayEventSearchIndexRepository
                            .findByValidOnGreaterThanAndUserIdAndUserType(pageable, yesterday, userId, userType);
                } else if (userId.equals(ALL)) {
                    upcomingEventsPage = dealsOfTheDayEventSearchIndexRepository
                            .findByValidOnGreaterThanAndUserTypeAndDealTitlesContainsOrUserNameContains(pageable,
                                    yesterday, userType, searchTerm, searchTerm);
                } else {
                    upcomingEventsPage = dealsOfTheDayEventSearchIndexRepository
                            .findByValidOnGreaterThanAndUserIdAndUserTypeAndDealTitlesContains(pageable, yesterday,
                                    userId, userType, searchTerm);
                }

                upcomingEventsPage.getContent().forEach(eventIndex -> {
                    java.sql.Date validOnDate = new java.sql.Date(eventIndex.getValidOn().getTime());
                    upcomingDates.add(validOnDate);
                });

                totalPages = upcomingEventsPage.getTotalPages();
                currentPage++;
            }

            for (java.sql.Date validOnDate : upcomingDates) {
                List<Deal> dealList = getDealList(searchTerm, userType, validOnDate, userId);
                if (!dealList.isEmpty()) {
                    Map<String, MerchantBusinessResponseDto> merchantMap = getMerchantFromAuth(dealList, userType);
                    responseDto.add(new UpcomingDealsOfTheDayHistoryGroupResponseDto(validOnDate.getTime(), timeZone,
                            dealList, merchantMap));
                }
            }

            return responseDto;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to get upcoming deals of the day history from db", e);
        }
    }

    /**
     * This method is used to get deals-of-the-day by date.
     *
     * @param timeZone   timeZone
     * @param searchTerm searchTerm
     * @param userId     userId
     * @param date       date
     * @return deals-of-the-day by date.
     */
    private List<DealsOfTheDayBasicEventResponseDto> createDealsOfTheDayHistoryResponse(String timeZone, String searchTerm,
                                                                                        String userId, Date date,
                                                                                        UserType userType) {

        List<DealsOfTheDayBasicEventResponseDto> responseDto = new ArrayList<>();
        List<Deal> dealList = getDealList(searchTerm, userType, date, userId);
        Map<String, MerchantBusinessResponseDto> merchantMap = getMerchantFromAuth(dealList, userType);
        responseDto.add(new DealsOfTheDayBasicEventResponseDto(date.getTime(), timeZone, dealList, merchantMap));
        return responseDto;
    }

    /**
     * This method is used to get last week's deals-of-the-day.
     *
     * @param timeZone       timeZone
     * @param searchTerm     searchTerm
     * @param userId         userId
     * @param thisWeekMonday monday this week.
     * @return last week's deals-of-the-day.
     */
    private List<DealsOfTheDayBasicEventResponseDto> createDealsOfTheDayLastWeekHistoryResponse(String timeZone,
                                                                                                String searchTerm,
                                                                                                String userId,
                                                                                                LocalDate thisWeekMonday,
                                                                                                UserType userType) {
        LocalDate lastWeekMonday = thisWeekMonday.minusDays(WEEK_DAYS);
        List<Deal> dealList;
        List<DealsOfTheDayBasicEventResponseDto> responseDto = new ArrayList<>();
        Map<String, MerchantBusinessResponseDto> merchantMap;
        Date lastWeekDate;

        for (int day = SIX; day >= ZERO; day--) {
            lastWeekDate = Date.from(lastWeekMonday.plusDays(day).atStartOfDay(ZoneId.of(timeZone)).toInstant());
            dealList = getDealList(searchTerm, userType, lastWeekDate, userId);
            if (!dealList.isEmpty()) {
                merchantMap = getMerchantFromAuth(dealList, userType);
                responseDto.add(new DealsOfTheDayBasicEventResponseDto(lastWeekDate.getTime(), timeZone,
                        dealList, merchantMap));
            }
        }
        return responseDto;
    }

    /**
     * This method is used to get maximum 250 deals from deals of the day database.
     *
     * @param searchTerm searchTerm
     * @param userType   merchant/bank
     * @param date       date
     * @param userId     userId
     * @return list of deals.
     */
    private List<Deal> getDealList(String searchTerm, UserType userType, Date date, String userId) {
        int totalPages = 1;
        List<Deal> dealList = new ArrayList<>();
        Page<Deal> dealPage;
        for (int currentPage = ZERO; currentPage < totalPages; currentPage++) {
            Pageable pageable = PageRequest.of(currentPage, SIZE);
            dealPage = searchDealList(searchTerm, userType, date, userId, pageable);
            totalPages = dealPage.getTotalPages();
            dealList.addAll(dealPage.getContent());
        }
        return dealList;
    }

    /**
     * This method queries deals from deals of the day table.
     *
     * @param searchTerm searchTerm
     * @param userType   MERCHANT/BANK
     * @param date       date
     * @param userId     userId
     * @param pageable   pageable
     * @return deal page.
     */
    private Page<Deal> searchDealList(String searchTerm, UserType userType, Date date,
                                      String userId, Pageable pageable) {
        if (userId.equals(ALL) && !searchTerm.equals(ALL)) {
            return userType.equals(UserType.MERCHANT) ?
                    dealsOfTheDayRepository.getDealsOfTheDayByDateAndSearchTerm(pageable, searchTerm, date) :
                    bankDealsOfTheDayRepository.getDealsOfTheDayByDateAndSearchTerm(pageable, searchTerm, date);
        } else if (!userId.equals(ALL) && searchTerm.equals(ALL)) {
            return userType.equals(UserType.MERCHANT) ?
                    dealsOfTheDayRepository.getDealsByMerchantIdAndAppliedOn(pageable, userId, date) :
                    bankDealsOfTheDayRepository.getDealsByBankIdAndAppliedOn(pageable, userId, date);
        } else if (userId.equals(ALL)) {
            return userType.equals(UserType.MERCHANT) ?
                    dealsOfTheDayRepository.getDealByAppliedOn(pageable, date) :
                    bankDealsOfTheDayRepository.getDealByAppliedOn(pageable, date);
        } else {
            return userType.equals(UserType.MERCHANT) ?
                    dealsOfTheDayRepository
                            .getDealsOfTheDayByMerchantDateAndSearchTerm(pageable, userId, searchTerm, date) :
                    bankDealsOfTheDayRepository
                            .getDealsOfTheDayByBankIdDateAndSearchTerm(pageable, userId, searchTerm, date);
        }
    }

    /**
     * Used to get merchant details from auth.
     *
     * @param dealList dealList
     * @return merchant map.
     */
    private Map<String, MerchantBusinessResponseDto> getMerchantFromAuth(List<Deal> dealList,
                                                                         UserType userType) {
        List<String> merchantIds = new ArrayList<>();
        if (dealList.isEmpty())
            return new HashMap<>();
        for (Deal deal : dealList) {
            merchantIds.add(deal.getMerchantId());
        }
        return authUserService.getMerchantMap(new BulkUserRequestDto(merchantIds), userType);
    }

    /**
     * This method will get existing event for the date, or return empty if not exists.
     *
     * @param userId    userId
     * @param eventDate eventDate
     * @return Optional of existing event
     */
    private Optional<DealsOfTheDayEvent> getExistingEvent(String userId, Date eventDate) {
        try {
            return dealsOfTheDayEventRepository.findByUserIdAndValidOn(userId, eventDate);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking deals of the day event exist in db was failed for userId: " +
                    userId + e);
        }
    }

    /**
     * This method will add deals of the day event to db.
     * If an event already exists for the date, it will add the new deals to the existing event.
     * Only processes MERCHANT deals immediately (ignores BANK deals).
     *
     * @param dealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto
     * @param userType                     MERCHANT/BANK
     * @param businessProfile              businessProfile
     */
    public DealsOfTheDayEvent addDealsOfTheDayEvent(DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto,
                                                    UserType userType, BusinessMerchantResponseDto businessProfile) {
        String userId = businessProfile.getMerchantId();
        try {
            Date eventDate = new Date(dealsOfTheDayEventRequestDto.getValidOn());
            
            // Check if event already exists for this date
            Optional<DealsOfTheDayEvent> existingEventOpt = getExistingEvent(userId, eventDate);
            DealsOfTheDayEvent dealsOfTheDayEvent;
            
            if (existingEventOpt.isPresent()) {
                // Add deals to existing event
                dealsOfTheDayEvent = existingEventOpt.get();
                Set<String> existingDeals = new HashSet<>(dealsOfTheDayEvent.getDeals());
                Set<String> newDealIds = dealsOfTheDayEventRequestDto.getDealIds();
                
                // Check for duplicates
                Set<String> duplicateDeals = new HashSet<>(existingDeals);
                duplicateDeals.retainAll(newDealIds);
                if (!duplicateDeals.isEmpty()) {
                    // Remove duplicates from new deals
                    newDealIds.removeAll(duplicateDeals);
                }
                
                if (!newDealIds.isEmpty()) {
                    // Check slot availability for new deals only
                    checkDealsOfTheDayEventSlotAvailable(eventDate, userType, newDealIds.size());
                    checkIfValidDealIds(userId, newDealIds, dealsOfTheDayEventRequestDto.getValidOn());
                    
                    // Add new deals to existing event
                    existingDeals.addAll(newDealIds);
                    dealsOfTheDayEvent.setDeals(existingDeals);
                    dealsOfTheDayEvent.setUpdatedAt(System.currentTimeMillis());
                    
                    // Update subscription check with total deals
                    SubscriptionRenewal subscriptionRenewal = subscriberService.getSubscriptionRenewalByUserId(userId);
                    if (existingDeals.size() > subscriptionRenewal.getNoOfDealsPerEvent())
                        throw new LimitExceededException("Number of deals per event limit exceeded for userId: " + userId,
                                LimitExceededType.DEALS_PER_DOD_EVENT);
                    
                    String dealTitles = createDealTitles(dealsOfTheDayEvent.getDeals());
                    DealsOfTheDayEventSearchIndex dealsOfTheDayEventSearchIndex = getDealsOfTheDayEventSearchIndex(dealsOfTheDayEvent.getId());
                    dealsOfTheDayEventSearchIndex.update(dealsOfTheDayEvent, dealTitles);
                    dealsOfTheDayEventRepository.save(dealsOfTheDayEvent);
                    dealsOfTheDayEventSearchIndexRepository.save(dealsOfTheDayEventSearchIndex);
                }
            } else {
                // Create new event
                checkDealsOfTheDayEventSlotAvailable(eventDate, userType, dealsOfTheDayEventRequestDto.getDealIds().size());
                checkIfValidDealIds(userId, dealsOfTheDayEventRequestDto.getDealIds(),
                        dealsOfTheDayEventRequestDto.getValidOn());
                SubscriptionRenewal subscriptionRenewal =
                        checkSubscriptionPlan(userId, dealsOfTheDayEventRequestDto.getDealIds());
                dealsOfTheDayEvent =
                        new DealsOfTheDayEvent(dealsOfTheDayEventRequestDto, userId, userType);
                String dealTitles = createDealTitles(dealsOfTheDayEvent.getDeals());
                DealsOfTheDayEventSearchIndex dealsOfTheDayEventSearchIndex =
                        new DealsOfTheDayEventSearchIndex(dealsOfTheDayEvent, businessProfile.getBusinessName(),
                                dealTitles, businessProfile.getImageUrl());
                dealsOfTheDayEventRepository.save(dealsOfTheDayEvent);
                dealsOfTheDayEventSearchIndexRepository.save(dealsOfTheDayEventSearchIndex);
                subscriptionRenewal.updateRemainingDealsOfTheDayEventsPerMonth(-ONE);
                subscriberService.saveSubscriptionRenewal(subscriptionRenewal);
            }
            
            // Process deals of the day immediately for MERCHANT deals only
            if (userType == UserType.MERCHANT) {
                processDealsOfTheDayForDate(new java.sql.Date(eventDate.getTime()), userType);
            }
            
            return dealsOfTheDayEvent;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving deals of the day event in db was failed for userId: " + userId + e);
        }
    }

    /**
     * This method checks if deals of the day event slots are available or not.
     *
     * @param validOn       validOn
     * @param userType      userType
     * @param newDealsCount newDealsCount
     */
    private void checkDealsOfTheDayEventSlotAvailable(Date validOn, UserType userType, int newDealsCount) {
        try {
            long noOfDeals = 0;
            List<DealsOfTheDayEvent> eventList =
                    dealsOfTheDayEventRepository.findByValidOnAndUserType(validOn, userType);
            for (DealsOfTheDayEvent dealsOfTheDayEvent : eventList) {
                noOfDeals = noOfDeals + dealsOfTheDayEvent.getDeals().size();
            }
            if (noOfDeals + newDealsCount > dealsOfTheDayDealLimit)
                throw new LimitExceededException("Deals of the day slot is not available on: " + validOn,
                        LimitExceededType.DEALS_PER_DOD);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking deals of the day event slot's availability was failed for userType: "
                    + userType + " on date: " + validOn, e);
        }
    }

    /**
     * This method combines all deal titles and creates a string with a set of dealIds.
     *
     * @param dealIds dealIds
     * @return combined deal title.
     */
    private String createDealTitles(Set<String> dealIds) {
        try {
            List<DealSearchIndex> dealSearchIndexList = dealSearchIndexRepository.findAllById(dealIds);
            StringBuilder dealTitles = new StringBuilder(EMPTY_TEXT);
            for (DealSearchIndex dealSearchIndex : dealSearchIndexList) {
                dealTitles.append(dealSearchIndex.getTitle()).append(SEPARATOR);
            }
            return String.valueOf(dealTitles.substring(ZERO, dealTitles.length() - TWO));
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting deal search indexes failed when creating deal titles.", e);
        }
    }

    /**
     * This method will validate user subscription plan.
     *
     * @param userId  userId
     * @param dealIds dealIds
     */
    private SubscriptionRenewal checkSubscriptionPlan(String userId, Set<String> dealIds) {
        try {
            SubscriptionRenewal subscription = subscriberService.getSubscriptionRenewalByUserId(userId);
            if (dealIds.size() > subscription.getNoOfDealsPerEvent())
                throw new LimitExceededException("Number of deals per event limit exceeded for userId: " + userId,
                        LimitExceededType.DEALS_PER_DOD_EVENT);
            if (subscription.getRemainingDealsOfTheDayEventsPerMonth() == ZERO)
                throw new LimitExceededException("Number of deals of the day events per month/year limit exceeded " +
                        "for userId: " + userId, LimitExceededType.DOD_EVENTS_PER_PLAN);
            return subscription;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Validating subscription was failed for userId: " + userId + e);
        }
    }

    /**
     * This method will check if dealIds are valid.
     * Only active or inactive (approved) deals can be used for deals of the day.
     * Expired, deactivated, or draft deals are not allowed.
     *
     * @param userId    userId
     * @param dealIds   dealIds
     * @param timeStamp timeStamp
     */
    private void checkIfValidDealIds(String userId, Set<String> dealIds, long timeStamp) {
        try {
            List<String> invalidDealIds = new ArrayList<>();
            List<String> invalidStatuses = new ArrayList<>();
            
            for (String dealId : dealIds) {
                Optional<DealSearchIndex> optionalDeal = dealSearchIndexRepository.findById(dealId);
                if (!optionalDeal.isPresent()) {
                    throw new InvalidDealException("DealId not found in database. DealId: " + dealId);
                }
                
                DealSearchIndex dealSearchIndex = optionalDeal.get();
                
                // Check if deal belongs to the user
                if (!dealSearchIndex.getMerchantId().equals(userId)) {
                    throw new InvalidDealException("Deal does not belong to the user. dealId: " + dealId);
                }
                
                // Check approval status
                ApprovalStatus approvalStatus = dealSearchIndex.getApprovalStatus();
                
                // Reject deactivated deals
                if (approvalStatus == ApprovalStatus.DEACTIVATE) {
                    invalidDealIds.add(dealId);
                    invalidStatuses.add("Deactivated");
                    continue;
                }
                
                // Reject draft deals
                if (approvalStatus == ApprovalStatus.DRAFT) {
                    invalidDealIds.add(dealId);
                    invalidStatuses.add("Draft");
                    continue;
                }
                
                // For approved deals, check if expired
                if (approvalStatus == ApprovalStatus.APPROVED) {
                    if (dealSearchIndex.getExpiredOn() < timeStamp) {
                        invalidDealIds.add(dealId);
                        invalidStatuses.add("Expired");
                        continue;
                    }
                    // Approved and not expired - check if active or inactive (both are allowed)
                    // Active: validFrom <= timeStamp && expiredOn > timeStamp
                    // Inactive: validFrom > timeStamp && expiredOn > timeStamp
                    // Both are valid for deals of the day
                } else {
                    // Other statuses (PENDING, REJECTED, etc.) are not allowed
                    invalidDealIds.add(dealId);
                    invalidStatuses.add(approvalStatus.toString());
                }
            }
            
            if (!invalidDealIds.isEmpty()) {
                String statusMessage = String.join(", ", invalidStatuses);
                throw new InvalidDealException("Expired, deactivated, or draft deals cannot be used for deals of the day. " +
                        "Invalid dealIds: " + String.join(", ", invalidDealIds) + ". Statuses: " + statusMessage);
            }
        } catch (InvalidDealException e) {
            throw e;
        } catch (AopInvocationException e) {
            throw new InvalidDealException("DealId not found in database. DealIds: " + dealIds, e);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Validating dealIds for deals of the day event was failed for userId: "
                    + userId + e);
        }
    }

    /**
     * This method will update deals of the day event.
     * Processes deals immediately for MERCHANT deals only.
     *
     * @param dealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto
     * @param userId                       userId
     * @param userType                     userType
     */
    public DealsOfTheDayEvent updateDealsOfTheDayEvent(DealsOfTheDayEventRequestDto dealsOfTheDayEventRequestDto,
                                                       String userId, UserType userType) {
        try {
            Date eventDate = new Date(dealsOfTheDayEventRequestDto.getValidOn());
            Set<String> dealIds = dealsOfTheDayEventRequestDto.getDealIds();
            String eventId = dealsOfTheDayEventRequestDto.getEventId();
            Optional<DealsOfTheDayEvent> optionalEvent =
                    dealsOfTheDayEventRepository.findById(eventId);
            if (optionalEvent.isPresent() && optionalEvent.get().getUserId().equals(userId)) {
                DealsOfTheDayEvent dealsOfTheDayEvent = optionalEvent.get();
                checkDealsOfTheDayEventSlotAvailable(eventDate, userType,
                        dealIds.size() - dealsOfTheDayEvent.getDeals().size());
                checkIfValidDealIds(userId, dealIds, dealsOfTheDayEvent.getValidOn().getTime());
                SubscriptionRenewal subscription = subscriberService.getSubscriptionRenewalByUserId(userId);
                if (dealIds.size() > subscription.getNoOfDealsPerEvent())
                    throw new LimitExceededException("Number of deals per event limit exceeded for userId: " + userId,
                            LimitExceededType.DEALS_PER_DOD_EVENT);
                dealsOfTheDayEvent.getDeals().clear();
                dealsOfTheDayEvent.setDeals(dealIds);
                dealsOfTheDayEvent.setUpdatedAt(System.currentTimeMillis());
                DealsOfTheDayEventSearchIndex dealsOfTheDayEventSearchIndex = getDealsOfTheDayEventSearchIndex(eventId);
                String dealTitles = createDealTitles(dealsOfTheDayEvent.getDeals());
                dealsOfTheDayEventSearchIndex.update(dealsOfTheDayEvent, dealTitles);
                dealsOfTheDayEventRepository.save(dealsOfTheDayEvent);
                dealsOfTheDayEventSearchIndexRepository.save(dealsOfTheDayEventSearchIndex);
                
                // Process deals of the day immediately for MERCHANT deals only
                if (userType == UserType.MERCHANT) {
                    processDealsOfTheDayForDate(new java.sql.Date(eventDate.getTime()), userType);
                }
                
                return dealsOfTheDayEvent;
            } else {
                throw new InvalidDealsOfTheDayEventException("Deals of the day event not found for eventId: " + eventId);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving deals of the day event in db was failed for userId: " + userId + e);
        }
    }

    /**
     * This method is used to delete deals of the day event.
     *
     * @param eventId eventId
     * @param userId  userId
     */
    public void deleteDealsOfTheDayEvent(String eventId, String userId) {
        try {
            Optional<DealsOfTheDayEvent> optionalEvent = dealsOfTheDayEventRepository.findById(eventId);
            if (optionalEvent.isPresent() && optionalEvent.get().getUserId().equals(userId) &&
                    optionalEvent.get().getValidOn().after(new Date())) {
                dealsOfTheDayEventRepository.deleteById(eventId);
                dealsOfTheDayEventSearchIndexRepository.deleteById(eventId);
                SubscriptionRenewal subscriptionRenewal = subscriberService.getSubscriptionRenewalByUserId(userId);
                subscriptionRenewal.updateRemainingDealsOfTheDayEventsPerMonth(ONE);
                subscriberService.saveSubscriptionRenewal(subscriptionRenewal);
            } else
                throw new InvalidDealsOfTheDayEventException("Deals of the day event not found.");
        } catch (DataAccessException e) {
            throw new KupiCoreException("Deleting deals of the day event was failed for eventId: " + eventId + e);
        }
    }

    /**
     * This method is used to get deal search index list by list of deals.
     *
     * @param dealList dealList
     * @return deal search index list.
     */
    private List<DealSearchIndex> getDealSearchIndexListByDealList(List<Deal> dealList) {
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        for (Deal deal : dealList) {
            Optional<DealSearchIndex> optionalDealSearchIndex = dealSearchIndexRepository.findById(deal.getId());
            optionalDealSearchIndex.ifPresent(dealSearchIndexList::add);
        }
        return dealSearchIndexList;
    }

    /**
     * This method is used to get deals of the day event.
     *
     * @param userType MERCHANT/BANK
     * @return list of deals of the day event.
     */
    private List<DealsOfTheDayEvent> getDealsOfTheDayEvent(UserType userType) {
        int totalPages = 1;
        List<DealsOfTheDayEvent> dealsOfTheDayEventList = new ArrayList<>();
        for (int currentPage = ZERO; currentPage < totalPages; currentPage++) {
            Pageable pageable = PageRequest.of(currentPage, SIZE);
            Page<DealsOfTheDayEvent> dealsOfTheDayEventPage =
                    dealsOfTheDayEventRepository.findByValidOnAndUserType(pageable, new Date(), userType);
            dealsOfTheDayEventList.addAll(dealsOfTheDayEventPage.getContent());
            totalPages = dealsOfTheDayEventPage.getTotalPages();
        }
        return dealsOfTheDayEventList;
    }

    /**
     * This method is used to get deals list from deals of the day event.
     *
     * @param dealsOfTheDayEventList dealsOfTheDayEventList
     * @return list of deals.
     */
    @Transactional
    public List<Deal> getDealsFromDealsOfTheDayEvents(List<DealsOfTheDayEvent> dealsOfTheDayEventList) {
        int totalPages = ONE;
        List<Deal> dealList = new ArrayList<>();
        List<String> dealIds = new ArrayList<>();
        for (DealsOfTheDayEvent dealsOfTheDayEvent : dealsOfTheDayEventList) {
            dealIds.addAll(dealsOfTheDayEvent.getDeals());
        }
        for (int currentPage = ZERO; currentPage < totalPages; currentPage++) {
            Pageable pageable = PageRequest.of(currentPage, SIZE);
            Page<Deal> dealPage = dealRepository.findByIdIn(pageable, dealIds);
            dealList.addAll(dealPage.getContent());
            totalPages = dealPage.getTotalPages();
        }
        return dealList;
    }

    /**
     * This method is used to get deal search index list by list of bank deals.
     *
     * @param bankDealList bankDealList
     * @return deal search index list.
     */
    private List<DealSearchIndex> getDealSearchIndexListByBankDealList(List<BankDeal> bankDealList) {
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        for (BankDeal bankDeal : bankDealList) {
            Optional<DealSearchIndex> optionalDealSearchIndex = dealSearchIndexRepository.findById(bankDeal.getId());
            optionalDealSearchIndex.ifPresent(dealSearchIndexList::add);
        }
        return dealSearchIndexList;
    }

    /**
     * This method is used to get bank deals list from deals of the day event.
     *
     * @param dealsOfTheDayEventList dealsOfTheDayEventList
     * @return list of bank deals.
     */
    @Transactional
    public List<BankDeal> getBankDealsFromDealsOfTheDayEvents(List<DealsOfTheDayEvent> dealsOfTheDayEventList) {
        int totalPages = 1;
        List<BankDeal> bankDealList = new ArrayList<>();
        List<String> dealIds = new ArrayList<>();
        for (DealsOfTheDayEvent dealsOfTheDayEvent : dealsOfTheDayEventList) {
            dealIds.addAll(dealsOfTheDayEvent.getDeals());
        }
        for (int currentPage = ZERO; currentPage < totalPages; currentPage++) {
            Pageable pageable = PageRequest.of(currentPage, SIZE);
            Page<BankDeal> dealPage = bankDealRepository.findByIdIn(pageable, dealIds);
            bankDealList.addAll(dealPage.getContent());
            totalPages = dealPage.getTotalPages();
        }
        return bankDealList;
    }

    /**
     * This method is used to get deal of the day event by id.
     *
     * @param eventId event id
     * @return DealsOfTheDayEvent
     */
    public DealsOfTheDayEvent getDealsOfTheDayEventById(String eventId) {
        try {
            return dealsOfTheDayEventRepository.findById(eventId).orElseThrow(() ->
                    new InvalidDealsOfTheDayEventException("Invalid Deal of the day event id: {}" + eventId));
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning deal of the day event by id: {} was failed. " + eventId, e);
        }
    }

    /**
     * This method returns deals of the day summary for admin.
     *
     * @return deals of the day event summary.
     */
    public DealsOfTheDayEventSummaryResponseDto getDealsOfTheDaySummaryForAdmin() {
        try {
            Date today = new Date();
            long noOfBankEvents = dealsOfTheDayEventSearchIndexRepository
                    .countByValidOnGreaterThanAndUserTypeEquals(today, UserType.BANK);
            long noOfMerchantEvents = dealsOfTheDayEventSearchIndexRepository
                    .countByValidOnGreaterThanAndUserTypeEquals(today, UserType.MERCHANT);
            long noOfTotalEvents = noOfBankEvents + noOfMerchantEvents;
            long noOfBanks = dealsOfTheDayEventSearchIndexRepository.getUserCountByUserType(UserType.BANK, today);
            long noOfMerchants = dealsOfTheDayEventSearchIndexRepository.getUserCountByUserType(UserType.MERCHANT, today);
            return new DealsOfTheDayEventSummaryResponseDto(noOfBankEvents, noOfMerchantEvents, noOfTotalEvents,
                    noOfBanks, noOfMerchants);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning deals of the day summary for admin was failed", e);
        }
    }

    /**
     * This method gets deals of the day event search index from database.
     *
     * @param eventId eventId
     * @return Deals of the day event search index.
     */
    private DealsOfTheDayEventSearchIndex getDealsOfTheDayEventSearchIndex(String eventId) {
        try {
            Optional<DealsOfTheDayEventSearchIndex> dealsOfTheDayEventSearchIndex =
                    dealsOfTheDayEventSearchIndexRepository.findById(eventId);
            if (dealsOfTheDayEventSearchIndex.isPresent())
                return dealsOfTheDayEventSearchIndex.get();
            else
                throw new InvalidDealsOfTheDayEventException("Deals of the day event search index not found for" +
                        " eventId: " + eventId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting deals of the day event search index from db was failed for eventId: " +
                    eventId, e);
        }
    }

    /**
     * This method is used to get upcoming deals of the day events.
     *
     * @param page       page
     * @param size       size
     * @param userType   MERCHANT/BANK
     * @param userId     userId
     * @param searchTerm searchTerm
     * @return page of deals of the day events.
     */
    public Page<DealsOfTheDayEventSearchIndex> getUpcomingDealsOfTheDayEvents(int page, int size, UserType userType,
                                                                              String userId, String searchTerm) {
        try {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DATE, -1);
            Date yesterday = calendar.getTime();
            Pageable pageable = PageRequest.of(page, size, Sort.by(VALID_ON).ascending());
            if (userId.equals(ALL) && searchTerm.equals(ALL))
                return dealsOfTheDayEventSearchIndexRepository
                        .findByValidOnGreaterThanAndUserType(pageable, yesterday, userType);
            if (!userId.equals(ALL) && searchTerm.equals(ALL))
                return dealsOfTheDayEventSearchIndexRepository
                        .findByValidOnGreaterThanAndUserIdAndUserType(pageable, yesterday, userId, userType);
            if (userId.equals(ALL))
                return dealsOfTheDayEventSearchIndexRepository
                        .findByValidOnGreaterThanAndUserTypeAndDealTitlesContainsOrUserNameContains(pageable, yesterday,
                                userType, searchTerm, searchTerm);
            else
                return dealsOfTheDayEventSearchIndexRepository
                        .findByValidOnGreaterThanAndUserIdAndUserTypeAndDealTitlesContains(pageable, yesterday,
                                userId, userType, searchTerm);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting upcoming deals of the day events from db was failed.");
        }
    }

    /**
     * This method creates a deal page based on coupon availability.
     *
     * @param dealListPage         dealListPage
     * @param couponStatisticsList couponStatisticsList
     * @return dealPage
     */
    public Page<Deal> removeCouponNotAvailableDeals(Page<Deal> dealListPage,
                                                    List<CouponStatistics> couponStatisticsList) {
        List<Deal> dealList = new ArrayList<>();
        dealListPage.get().forEach(deal -> {
            if (!deal.isCouponAvailable()) {
                dealList.add(deal);
            } else {
                for (var couponStatistic : couponStatisticsList) {
                    if (couponStatistic.getDeal().getId().equals(deal.getId())) {
                        dealList.add(deal);
                    }
                }
            }
        });
        return new PageImpl<>(dealList);
    }
}
