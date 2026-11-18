package com.kupi.util.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class WhatsAppCheckResponse {
    private List<Contact> contacts;

    @Getter
    @Setter
    public static class Contact {
        private String input;
        private String status;
        private String wa_id;
    }
}
