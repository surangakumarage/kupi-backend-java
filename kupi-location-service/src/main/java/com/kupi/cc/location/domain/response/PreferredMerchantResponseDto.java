package com.kupi.cc.location.domain.response;

import lombok.Data;

import java.util.List;

@Data
public class PreferredMerchantResponseDto {
    private List<String> preferredMerchantIds;
    private String userId;
}