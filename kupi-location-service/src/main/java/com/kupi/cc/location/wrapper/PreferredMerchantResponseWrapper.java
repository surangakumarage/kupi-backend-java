package com.kupi.cc.location.wrapper;

import com.kupi.cc.location.domain.response.PreferredMerchantResponseDto;
import lombok.Data;

@Data
public class PreferredMerchantResponseWrapper {
    private String status;
    private String message;
    private PreferredMerchantResponseDto data;
    private String displayMessage;
}