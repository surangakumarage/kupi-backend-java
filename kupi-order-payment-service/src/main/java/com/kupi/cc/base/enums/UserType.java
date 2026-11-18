package com.kupi.cc.base.enums;

import com.kupi.cc.base.exception.KupiOrderPaymentException;
import lombok.Getter;

/**
 * User type for deals
 */
@Getter
public enum UserType {

    ADMIN("ADMIN"),
    BANK("BANK"),
    MERCHANT("MERCHANT");

    private final String type;

    UserType(String type) {
        this.type = type;
    }

    /**
     * This method returns relevant user type for string.
     *
     * @param userTypeInString user type in string.
     * @return BANK/MERCHANT
     */
    public static UserType getUserType(String userTypeInString) {
        if (userTypeInString != null) {
            for (UserType userType : UserType.values()) {
                if (userType.type.equalsIgnoreCase(userTypeInString.trim())) {
                    return userType;
                }
            }
        }
        throw new KupiOrderPaymentException("Invalid user type: " + userTypeInString);
    }
}
