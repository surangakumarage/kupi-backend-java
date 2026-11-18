package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceResponse extends ResponseDto{
    private static final long serialVersionUID = -5862019403729826602L;

    private String responseCode;
    private List<String> responseParameters;
    private boolean isSuccess;
}
