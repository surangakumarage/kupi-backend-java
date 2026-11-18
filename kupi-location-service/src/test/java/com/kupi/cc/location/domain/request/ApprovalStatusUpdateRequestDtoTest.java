package com.kupi.cc.location.domain.request;

import com.kupi.cc.location.enums.ApprovalAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link ApprovalStatusUpdateRequestDto} class
 */
class ApprovalStatusUpdateRequestDtoTest {

    private static final String BRANCH_ID = "brid-123456789";
    private static final String COMMENT = "COMMENT";

    /**
     * Required fields existance check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto(BRANCH_ID,
                ApprovalAction.APPROVE, COMMENT);
        assertTrue(approvalStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IdIsEmpty() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto("",
                ApprovalAction.APPROVE, COMMENT);
        assertFalse(approvalStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_IdIsNull() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto(null,
                ApprovalAction.APPROVE, COMMENT);
        assertFalse(approvalStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ApprovalActionIsNull() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto(BRANCH_ID,
                null, COMMENT);
        assertFalse(approvalStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CommentIsEmpty() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto(BRANCH_ID,
                ApprovalAction.REJECT, "");
        assertFalse(approvalStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CommentIsNull() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto(BRANCH_ID,
                ApprovalAction.REJECT, null);
        assertFalse(approvalStatusUpdateRequestDto.isRequiredAvailable());
    }

    private ApprovalStatusUpdateRequestDto getSampleApprovalStatusUpdateRequestDto(String id, ApprovalAction approvalAction,
                                                                                   String comment) {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = new ApprovalStatusUpdateRequestDto();
        approvalStatusUpdateRequestDto.setId(id);
        approvalStatusUpdateRequestDto.setAction(approvalAction);
        approvalStatusUpdateRequestDto.setComment(comment);
        return approvalStatusUpdateRequestDto;
    }
}