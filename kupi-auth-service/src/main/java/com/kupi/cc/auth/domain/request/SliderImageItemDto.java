package com.kupi.cc.auth.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SliderImageItemDto {

    private String imageUrl;
    private int displayOrder;
}
