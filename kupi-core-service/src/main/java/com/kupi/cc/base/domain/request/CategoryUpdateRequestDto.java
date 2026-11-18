package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Request dto for category update
 */
@Setter
@Getter
public class CategoryUpdateRequestDto extends CategoryRequestDto {

    private String id;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(id) && super.isRequiredAvailable();
    }
}
