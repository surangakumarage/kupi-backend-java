package com.kupi.cc.location.wrapper;

import com.kupi.cc.location.domain.BaseDto;
import com.kupi.cc.location.enums.ResponseStatusType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Response wrapper for deal list (used for recently expiring deals endpoint).
 */
@Getter
@Setter
public class DealListResponseWrapper implements BaseDto {

    private ResponseStatusType status;
    private String message;
    private DealListResponseData data;
    private String displayMessage;

    @Getter
    @Setter
    public static class DealListResponseData {
        private Long totalItems;
        private Integer totalPages;
        private Integer page;
        private Integer size;
        private List<Map<String, Object>> deals;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

