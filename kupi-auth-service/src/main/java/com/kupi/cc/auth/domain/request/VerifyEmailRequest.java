package com.kupi.cc.auth.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyEmailRequest extends RequestDto {

    private String email;

    @Override
    public String toLogJson() {
        return toJson();
    }
}
