package com.kupi.cc.auth.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kupi.cc.auth.enums.ApprovalStatus;
import com.kupi.cc.auth.enums.Language;
import com.kupi.cc.auth.wrapper.RoleResponseWrapper;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Data implements Serializable {

    private String userId;
    private String fullName;
    private String imageUrl;
    private Language language;
    private ApprovalStatus approvalStatus;
    private boolean isActive;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BusinessProfile businessProfile;
    private RoleResponseWrapper role;
}