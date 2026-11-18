package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Deal view count response
 */
@Getter
@Setter
@AllArgsConstructor
public class DealViewCountResponse {

    private DealSearchIndex deal;
    private String merchantId;
    private long viewCount;
    private String displayDate;
    private UserType dealSource;
    private int totalRows;
}
