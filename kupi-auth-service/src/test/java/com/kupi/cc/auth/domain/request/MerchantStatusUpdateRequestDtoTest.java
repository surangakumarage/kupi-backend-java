package com.kupi.cc.auth.domain.request;

import com.kupi.cc.auth.enums.ApprovalAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link MerchantStatusUpdateRequestDto} class
 */
class MerchantStatusUpdateRequestDtoTest {

    private static final String MERCHANT_ID = "MERCHANT";
    private static final String COMMENT = "COMMENT";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        assertTrue(merchantStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MerchantIdIsNull() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setMerchantId(null);
        assertFalse(merchantStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MerchantIdIsEmpty() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setMerchantId("");
        assertFalse(merchantStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ApproveActionIsNull() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(null);
        assertFalse(merchantStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ApproveActionAndCommentAreNull() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(null);
        merchantStatusUpdateRequestDto.setComment(null);
        assertFalse(merchantStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ApproveActionIsNullAndCommentIsEmpty() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(null);
        merchantStatusUpdateRequestDto.setComment("");
        assertFalse(merchantStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnTrue_When_ApproveActionIsEitherUnBlockOrApproveAndCommentIsEmpty() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.UNBLOCK);
        merchantStatusUpdateRequestDto.setComment("");
        assertTrue(merchantStatusUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ApproveActionIsEitherBlockOrRejectAndCommentIsEmpty() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.BLOCK);
        merchantStatusUpdateRequestDto.setComment("");
        assertFalse(merchantStatusUpdateRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample MerchantStatusUpdateRequestDto
     *
     * @return MerchantStatusUpdateRequestDto
     */
    private MerchantStatusUpdateRequestDto getSampleMerchantStatusUpdateRequestDto() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = new MerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setMerchantId(MERCHANT_ID);
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.APPROVE);
        merchantStatusUpdateRequestDto.setComment(COMMENT);
        return merchantStatusUpdateRequestDto;
    }
}