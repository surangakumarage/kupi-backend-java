package com.kupi.cc.auth.domain.request;

import com.kupi.cc.auth.util.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link ContactRequestDto} class
 */
class ContactRequestDtoTest {

    private static final String MERCHANT_ID = "uid-bd0c8894-8ddb-4b1d-8383-de44bfaa297f";
    @Mock
    private Validator validator;
    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreProvided() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        assertTrue(contactRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MerchantIdIsEmpty() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        contactRequestDto.setMerchantId("");
        assertFalse(contactRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_MerchantIdIsNull() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        contactRequestDto.setMerchantId(null);
        assertFalse(contactRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ContactNameIsEmpty() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        contactRequestDto.setName("");
        assertFalse(contactRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ContactNameIsNull() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        contactRequestDto.setName("");
        assertFalse(contactRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_CountryCodeIsEmpty() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto("", "771107101");
        contactRequestDto.setTelephone(mobileNoRequestDto);
        assertFalse(contactRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_LocalNumberIsEmpty() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto("+94", "");
        contactRequestDto.setTelephone(mobileNoRequestDto);
        assertFalse(contactRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnTrue_When_EmailIdIsEmpty() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        contactRequestDto.setEmail("");
        assertTrue(contactRequestDto.isContainingValidEmail(validator));
    }

    @Test
    void Should_ReturnFalse_When_InvalidEmailProvided() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        assertFalse(contactRequestDto.isContainingValidEmail(validator));
    }

    /**
     * This method creates sample ContactRequestDto
     *
     * @return ContactRequestDto
     */
    private ContactRequestDto getSampleContactRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto("+94", "771107101");
        return new ContactRequestDto(MERCHANT_ID, "NAME", "DESIGNATION", mobileNoRequestDto, "EMAIL");
    }
}