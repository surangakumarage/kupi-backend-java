package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Subscription;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SubscriptionList ResponseDto
 */

@Getter
@Setter
public class SubscriptionListResponseDto extends PageResponseDto {
    private List<SubscriptionOrderResponseDto> subscriptions;

    public SubscriptionListResponseDto(Page<Subscription> subscriptionPage, String timeZone,
                                       Map<String, BasicBusinessResponseDto> userMap) {
        super(subscriptionPage);
        this.subscriptions = new ArrayList<>();
        subscriptionPage.getContent().forEach(subscription -> {
            var subscriptionOrderResponseDto = new SubscriptionOrderResponseDto(subscription, timeZone,
                    userMap.get(subscription.getUserId()));
            subscriptions.add(subscriptionOrderResponseDto);
        });
    }

    /**
     * This method converts object to json string for logging purpose.
     * PII data should be obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}