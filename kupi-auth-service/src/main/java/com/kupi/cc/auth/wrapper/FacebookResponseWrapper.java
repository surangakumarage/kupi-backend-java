package com.kupi.cc.auth.wrapper;

import com.kupi.cc.auth.domain.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class FacebookResponseWrapper implements BaseDto {

    private String id;
    private String name;
    private String email;


    @Override
    public String toLogJson() {
        return toJson();
    }
}
