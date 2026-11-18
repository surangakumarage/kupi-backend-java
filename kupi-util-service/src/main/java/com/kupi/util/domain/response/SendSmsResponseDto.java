package com.kupi.util.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * This class use as send sms response dto
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendSmsResponseDto extends ResponseDto {
    private String request;
    private String status;
    private String group_id;
    private String date;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
