package com.kupi.cc.location.util;

import org.springframework.stereotype.Component;

/**
 * This class is used to validate fields.
 */
@Component
public class Validator {
    private static final String URL_REGEX = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)";

    /**
     * This method checks the validity of the given url.
     *
     * @param url url
     * @return true/ false
     */
    public boolean isValidUrl(String url) {
        return url != null && !url.isBlank() && url.trim().matches(URL_REGEX);
    }
}