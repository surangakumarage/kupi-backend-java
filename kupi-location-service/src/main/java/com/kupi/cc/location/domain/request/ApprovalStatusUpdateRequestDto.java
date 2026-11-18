package com.kupi.cc.location.domain.request;

import com.kupi.cc.location.enums.ApprovalAction;
import lombok.Getter;
import lombok.Setter;

/**
 * Approval Status Update RequestDto
 */
@Getter
@Setter
public class ApprovalStatusUpdateRequestDto extends RequestDto {

    private String id;
    private ApprovalAction action;
    private String comment;

    /**
     * This method checks all required data are available.
     *
     * @return true/ false
     */
    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(id) && action != null &&
                (action.equals(ApprovalAction.APPROVE) || (action.equals(ApprovalAction.REJECT) && isNonEmpty(comment)));
    }

    /**
     * This method converts object to json string for logging purpose.
     * PII data should be obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}
