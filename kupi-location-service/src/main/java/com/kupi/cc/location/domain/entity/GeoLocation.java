package com.kupi.cc.location.domain.entity;

import com.kupi.cc.location.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class GeoLocation {
    @Id
    private String userId;
    private UserType userType;
    private String globalCode;
}
