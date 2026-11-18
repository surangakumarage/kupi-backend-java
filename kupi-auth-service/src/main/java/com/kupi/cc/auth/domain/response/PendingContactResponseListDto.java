package com.kupi.cc.auth.domain.response;

import com.kupi.cc.auth.domain.entity.Contact;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Pending contact response list dto
 */
@Getter
public class PendingContactResponseListDto extends PageResponseDto {

    private final List<PendingContactResponseDto> allPendingContactInfo;

    public PendingContactResponseListDto(Page<Contact> page, List<PendingContactResponseDto> contactsList) {
        super(page);
        this.allPendingContactInfo = contactsList;
    }
}
