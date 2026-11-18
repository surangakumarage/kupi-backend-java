package com.kupi.cc.base.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kupi.cc.base.enums.CategoryType;
import lombok.Getter;
import lombok.Setter;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * Request dto for category
 */
@Setter
@Getter
public class CategoryRequestDto extends RequestDto {

    private String name;
    private String description;
    private CategoryType categoryType;
    private String imageUrl;
    private Long startDate;
    private Long expiryDate;
    private List<String> relatedCategories;
    @JsonProperty
    private boolean isPopular;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(name) && isNonEmpty(description) && isNonEmpty(categoryType.name())
                && isNonEmpty(imageUrl);
    }

    /**
     * This method checks whether startDate & expiryDate are valid.
     *
     * @return true / false
     */
    public boolean isValidDateRange(String timeZone) {
        Date today = new Date();
        long todayInMillis = Date.from(today.toInstant().atZone(ZoneId.of(timeZone))
                .truncatedTo(ChronoUnit.DAYS).toInstant()).getTime();
        return startDate != null && expiryDate != null && expiryDate > startDate &&
                startDate >= todayInMillis && expiryDate > System.currentTimeMillis();
    }

    /**
     * This method check expiry date valid.
     *
     * @return true / false
     */
    public boolean isValidDateRangeForUpdateCategory() {
        return startDate != null && expiryDate != null && expiryDate > startDate
                && expiryDate > System.currentTimeMillis();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
