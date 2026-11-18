package com.kupi.cc.base.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlanStatusRequestDto extends RequestDto {

    private boolean isActive;


    @Override
    public String toLogJson() {
        return toJson();
    }

}
