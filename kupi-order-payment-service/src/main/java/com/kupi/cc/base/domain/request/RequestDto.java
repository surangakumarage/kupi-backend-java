package com.kupi.cc.base.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kupi.cc.base.domain.BaseDto;

/**
 * RequestDto - All requestDto classes are needed to extend this class.
 */
public abstract class RequestDto implements BaseDto {

    /**
     * This method checks all required fields are available for a request.
     *
     * @return true/ false
     */
    @JsonIgnore
    public boolean isRequiredAvailable() {
        return true;
    }

    /**
     * This method checks the given field is non empty.
     *
     * @param field field
     * @return true/ false
     */
    protected boolean isNonEmpty(String field) {
        return field != null && !field.trim().isEmpty();
    }
}
