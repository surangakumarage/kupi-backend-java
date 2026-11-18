package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.entity.TotalSavings;
import com.kupi.cc.base.enums.DeductionType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.DealSearchIndexRepository;
import com.kupi.cc.base.repository.TotalSavingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Scheduled job that calculates and stores total savings as the sum of currently active deals' deductionAmount.
 * Same notion of "active" as the active-deals API: valid date range, approved, active merchant.
 * Runs at midnight (12 AM) by default so the total updates after deals expire; also runs on application startup.
 */
@Slf4j
@Service
public class TotalSavingsScheduler implements CommandLineRunner {

    private static final String TOTAL_SAVINGS_ID = "totsav-ALL";
    private static final String TIME_ZONE = "GMT+05:30";
    private volatile boolean isRunning = false;

    private final DealSearchIndexRepository dealSearchIndexRepository;
    private final TotalSavingsRepository totalSavingsRepository;

    @Autowired
    public TotalSavingsScheduler(DealSearchIndexRepository dealSearchIndexRepository,
                                 TotalSavingsRepository totalSavingsRepository) {
        this.dealSearchIndexRepository = dealSearchIndexRepository;
        this.totalSavingsRepository = totalSavingsRepository;
    }

    /**
     * Runs on application startup to calculate total savings initially.
     */
    @Override
    public void run(String... args) {
        log.info("Application started. Calculating total savings (sum of active deals' deductionAmount) on startup...");
        calculateAndSaveTotalSavings();
    }

    /**
     * Scheduled job that runs at midnight (12 AM) by default to recalculate total savings from active deals.
     * When deals expire at 12 AM, the next run updates the stored total accordingly.
     */
    @Scheduled(cron = "${totalSavings.cron:0 0 0 * * *}", zone = TIME_ZONE)
    @Transactional
    public void scheduledCalculateTotalSavings() {
        calculateAndSaveTotalSavings();
    }

    /**
     * Triggers an asynchronous recalculation of total app savings.
     * Call this when a deal is created, updated, approved, or deleted so the stored total reflects active deals immediately.
     */
    public void triggerRecalculationAsync() {
        CompletableFuture.runAsync(this::calculateAndSaveTotalSavings);
    }

    /**
     * Calculates total savings as the sum of all currently active deals' deductionAmount and saves to database.
     * Active deals use the same criteria as the active-deals endpoint (valid date range, approved, active merchant).
     */
    private synchronized void calculateAndSaveTotalSavings() {
        if (isRunning) {
            log.warn("Total savings calculation is already running. Skipping this execution.");
            return;
        }

        isRunning = true;
        long startTime = System.currentTimeMillis();
        long currentMills = System.currentTimeMillis();

        try {
            log.info("Starting total savings calculation (active deals) at: {}", startTime);

            List<DealSearchIndex> activeDeals = dealSearchIndexRepository
                    .findAllActiveDealsWithActiveMerchantList(currentMills, UserType.MERCHANT);

            if (activeDeals == null || activeDeals.isEmpty()) {
                log.info("No active deals found. Setting total savings to zero.");
                saveTotalSavings(BigDecimal.ZERO);
                return;
            }

            log.debug("Found {} active deals. Summing deductionAmount...", activeDeals.size());

            BigDecimal totalSavings = activeDeals.stream()
                    .map(deal -> {
                        Double originalPrice = deal.getOriginalPrice();
                        if (originalPrice == null || originalPrice <= 0) {
                            log.debug("Skipping deal with invalid price. dealId={}, originalPrice={}",
                                    deal.getId(), originalPrice);
                            return BigDecimal.ZERO;
                        }
                        double deductionAmount = calculateDeductionAmount(deal, originalPrice);
                        return BigDecimal.valueOf(deductionAmount).setScale(2, RoundingMode.HALF_UP);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("Total savings calculated: {}. Processed {} active deals in {} ms",
                    totalSavings, activeDeals.size(), System.currentTimeMillis() - startTime);

            saveTotalSavings(totalSavings);

        } catch (DataAccessException e) {
            log.error("Database error while calculating total savings", e);
            throw new KupiCoreException("Failed to calculate total savings", e);
        } catch (Exception e) {
            log.error("Unexpected error while calculating total savings", e);
            throw new KupiCoreException("Unexpected error while calculating total savings", e);
        } finally {
            isRunning = false;
            log.info("Finished total savings calculation at: {}. Duration: {} ms",
                    System.currentTimeMillis(), System.currentTimeMillis() - startTime);
        }
    }

    private void saveTotalSavings(BigDecimal totalSavings) {
        try {
            Optional<TotalSavings> existingRecord = totalSavingsRepository.findById(TOTAL_SAVINGS_ID);

            if (existingRecord.isPresent()) {
                TotalSavings totalSavingRecord = existingRecord.get();
                totalSavingRecord.updateTotalSavings(totalSavings);
                totalSavingsRepository.save(totalSavingRecord);
                log.debug("Updated existing total savings record: {}", totalSavings);
            } else {
                TotalSavings newRecord = new TotalSavings(totalSavings);
                totalSavingsRepository.save(newRecord);
                log.debug("Created new total savings record: {}", totalSavings);
            }
        } catch (DataAccessException e) {
            log.error("Failed to save total savings to database", e);
            throw new KupiCoreException("Failed to save total savings", e);
        }
    }

    /**
     * Calculates deduction amount for a deal (search index) based on deduction type.
     */
    private double calculateDeductionAmount(DealSearchIndex deal, Double originalPrice) {
        DeductionType deductionType = deal.getDeductionType();

        if (deductionType == DeductionType.AMOUNT) {
            Double deduction = deal.getDeductionAmount();
            return deduction != null ? deduction : 0.0;
        }

        if (deductionType == DeductionType.PERCENTAGE) {
            Double deductionPercentage = deal.getDeductionPercentage();
            if (deductionPercentage != null && deductionPercentage > 0) {
                return originalPrice * (deductionPercentage / 100.0);
            }
        }

        return 0.0;
    }
}

