package com.kupi.cc.auth.enums;


import lombok.Getter;

/**
 * Application supported roles types are listed here
 */
@Getter
public enum RoleType {

    ADMIN(1),
    USER(2),
    MERCHANT(3),
    BANK(4),
    GUEST(5);

    private int id;

    RoleType(int roleId) {
        this.id = roleId;
    }
}
