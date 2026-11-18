package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class PromoTypeRequestDto {

    @NotBlank(message = "Promo type name is required")
    @Size(min = 1, max = 255, message = "Promo type name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    public boolean isRequiredAvailable() {
        return name != null && !name.trim().isEmpty() &&
                categoryId != null && !categoryId.trim().isEmpty();
    }
}
