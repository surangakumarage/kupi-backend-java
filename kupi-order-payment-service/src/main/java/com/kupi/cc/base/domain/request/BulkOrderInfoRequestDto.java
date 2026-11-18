package com.kupi.cc.base.domain.request;

import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkOrderInfoRequestDto extends RequestDto {

    private static final long serialVersionUID = 7288857105007488809L;
    private List<String> customerIdList;
    private List<CurrencyType> currencyTypeList;
    private long createdDate;
    private long createdDateRangeStart;
    private long createdDateRangeEnd;
    private boolean includeExpired;
    private boolean includeNotExpired;
    private List<String> dealIdList;
    private List<String> merchantIdList;
    private List<OrderStatus> orderStatusList;
    private List<String> paymentBrokerIdList;

    @Override
    public String toLogJson() {
        return toJson();
    }

    @Override
    public boolean isRequiredAvailable() {
        return isStringListValid(customerIdList) && isStringListValid(dealIdList) && isStringListValid(merchantIdList)
                && isStringListValid(paymentBrokerIdList);
    }

    private boolean isStringListValid(List<String> list) {
        if (list != null && !list.isEmpty()) {
            return list.stream().map(element -> element != null && !element.isBlank()).reduce(true, Boolean::logicalAnd);
        }
        return true;
    }
}
