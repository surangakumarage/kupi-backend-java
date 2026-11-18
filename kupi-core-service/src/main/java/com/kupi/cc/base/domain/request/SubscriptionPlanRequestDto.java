package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.PlanType;
import com.kupi.cc.base.enums.UserType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SubscriptionPlanRequestDto extends RequestDto {

    private static final int SHORT_TEXT_LENGTH = 100;
    private static final int LONG_TEXT_LENGTH = 500;

    private String title;
    private String subTitle;
    private String description;
    private String imageUrl;
    private String termsAndCondition;
    private UserType userRole;
    private PlanType planType;
    private Integer noOfDealsPerMonth;
    private Integer noOfDealsOfTheDayEventsPerMonth;
    private Integer noOfDealsOfTheDayPerEvent;
    private Boolean isFree;
    private BigDecimal amount;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(title) && userRole != null && planType != null && noOfDealsPerMonth != null
                && noOfDealsOfTheDayEventsPerMonth != null && noOfDealsOfTheDayPerEvent != null && isFree != null;
    }

    /**
     * This method return true when plan's free & amount is valid.
     *
     * @return true/ false
     */
    public boolean isValidAmount() {
        if (isFree) {
            return BigDecimal.ZERO.compareTo(amount) == 0;
        } else {
            return amount.compareTo(BigDecimal.ZERO) > 0;
        }
    }

    /**
     * This method returns true when noOfDealsPerMonth is valid.
     *
     * @return true/ false
     */
    public boolean isValidNoOfDealsPerMonth() {
        return noOfDealsPerMonth > 0;
    }

    /**
     * This method returns true when noOfDealsOfTheDayEventsPerMonth is valid.
     *
     * @return true/ false
     */
    public boolean isValidNoOfDealsOfTheDayEventsPerMonth() {
        return noOfDealsOfTheDayEventsPerMonth >= 0;
    }

    /**
     * This method returns true when noOfDealsOfTheDayEventsPerMonth is valid.
     *
     * @return true/ false
     */
    public boolean isValidNoOfDealsOfTheDayPerEvent() {
        return noOfDealsOfTheDayPerEvent >= 1;
    }

    /**
     * This method checks if text are in valid character limit.
     *
     * @return true/ false
     */
    public boolean isValidCharacterLimit() {
        return title.length() <= SHORT_TEXT_LENGTH && subTitle.length() <= SHORT_TEXT_LENGTH &&
                description.length() <= LONG_TEXT_LENGTH;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
