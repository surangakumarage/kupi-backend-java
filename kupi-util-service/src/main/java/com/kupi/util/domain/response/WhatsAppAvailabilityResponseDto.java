package com.kupi.util.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppAvailabilityResponseDto extends ResponseDto {

    private boolean isOnWhatsApp;
    private String phoneNumber;

    @Override
    public String toString() {
        return "WhatsAppAvailabilityResponseDto{" +
                "isOnWhatsApp=" + isOnWhatsApp +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}