package com.kupi.cc.base.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Deal view request dto
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DealViewRequestDto extends RequestDto {

    private String dealId;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(dealId);
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

