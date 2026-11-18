package com.kupi.cc.base.domain.Container;

import com.kupi.cc.base.enums.ErrorResponseStatusType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ValidityContainerDto {

    private boolean isValid;
    private ErrorResponseStatusType errorResponseStatusType;
}
