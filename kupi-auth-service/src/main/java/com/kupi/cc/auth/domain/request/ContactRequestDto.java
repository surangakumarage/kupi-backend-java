package com.kupi.cc.auth.domain.request;

import com.kupi.cc.auth.util.Validator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Merchant contact details request dto
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactRequestDto extends RequestDto {

    private String merchantId;
    private String name;
    private String designation;
    private MobileNoRequestDto telephone;
    private String email;

    @Override
    public String toLogJson() {
        return toJson();
    }

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(merchantId) && isNonEmpty(name) && telephone.isRequiredAvailable();
    }

    /**
     * This method checks if email is valid.
     *
     * @param validator validator
     * @return true/false
     */
    public boolean isContainingValidEmail(Validator validator) {
        return !isNonEmpty(email) || validator.isValidEmail(email);
    }
}
