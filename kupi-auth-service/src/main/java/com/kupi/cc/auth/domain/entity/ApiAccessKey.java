package com.kupi.cc.auth.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Api access key
 */

@Entity
@Table(name = "api_access_key")
@Getter
@Setter
@NoArgsConstructor
public class ApiAccessKey {

    @Id
    private String userId;
    private String accessKey;

    public ApiAccessKey(String userId) {
        this.userId = userId;
        this.accessKey = UUID.randomUUID().toString();
    }
}
