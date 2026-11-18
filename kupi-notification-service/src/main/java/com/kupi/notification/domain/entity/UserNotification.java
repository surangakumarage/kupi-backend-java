package com.kupi.notification.domain.entity;

import com.kupi.notification.domain.request.AddTokenRequestDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "user_notification")
@Data
@NoArgsConstructor
public class UserNotification implements Serializable {

    @Transient
    private static final String USER_NOTIFICATION_PREFIX = "unid-";

    @Id
    private String id;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "token", length = 1000)
    private String token;
    @Column(name = "deviceId")
    private String deviceId;
    @Column(name = "createdAt")
    private Date createdAt;
    @Column(name = "updatedAt")
    private Date updatedAt;
    private boolean notificationEnabled = true;

    public UserNotification(AddTokenRequestDto addTokenRequestDto, String token) {
        this.id = USER_NOTIFICATION_PREFIX + UUID.randomUUID();
        this.userId = addTokenRequestDto.getUserId();
        this.token = token;
        this.deviceId = addTokenRequestDto.getDeviceId();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
}
