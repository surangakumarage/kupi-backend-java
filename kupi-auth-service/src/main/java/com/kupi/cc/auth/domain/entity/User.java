package com.kupi.cc.auth.domain.entity;

import com.kupi.cc.auth.domain.request.UnregisterUserDto;
import com.kupi.cc.auth.domain.request.UserRequestDto;
import com.kupi.cc.auth.domain.request.UserUpdateRequestDto;
import com.kupi.cc.auth.enums.ApprovalStatus;
import com.kupi.cc.auth.enums.Language;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.wrapper.FacebookResponseWrapper;
import com.kupi.cc.auth.wrapper.GoogleResponseWrapper;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "user")
public class User implements Serializable {

    @Transient
    private static final String USER_ID_PREFIX = "uid-";
    @Transient
    private static final String GUEST_USERNAME_PREFIX = "G#";
    @Transient
    private static final String GUEST_PW_PREFIX = "PW#";
    @Transient
    private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Id
    private String id;
    @Column(name = "fullname")
    private String fullName;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "mobileNo")
    private String mobileNo;
    @Column(name = "mobileNoAsUserName")
    private String mobileNoAsUserName;
    @Column(name = "imageUrl")
    private String imageUrl;
    @Column(name = "language")
    @Enumerated(EnumType.STRING)
    private Language language;
    @Column(name = "approvalStatus")
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;
    @Column(name = "createdAt")
    private Date createdAt;
    @Column(name = "updatedAt")
    private Date updatedAt;
    @Column(name = "enabled")
    private boolean enabled;
    @Column(name = "is_registered")
    private boolean isRegisteredUser;
    @Column(name = "is_demo_video_watched", nullable = false)
    private boolean demoVideoWatched;

    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;

    @Column(name = "is_email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "is_mobile_verified", nullable = false)
    private boolean mobileVerified;

    @Column(name = "has_seen_early_bird_offer", nullable = false)
    private boolean hasSeenEarlyBirdOffer;

    public User() {
    }

    public User(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.mobileNo = user.getMobileNo();
        this.mobileNoAsUserName = user.getMobileNoAsUserName();
        this.imageUrl = user.getImageUrl();
        this.language = user.getLanguage();
        this.approvalStatus = user.getApprovalStatus();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.enabled = user.isEnabled();
        this.isRegisteredUser = user.isRegisteredUser();
        this.role = user.getRole();
    }

    public User(GoogleResponseWrapper googleResponseWrapper) {
        this.id = USER_ID_PREFIX + UUID.randomUUID();
        this.fullName = googleResponseWrapper.getName();
        this.email = googleResponseWrapper.getEmail();
        this.imageUrl = googleResponseWrapper.getPicture();
        this.language = googleResponseWrapper.getLocale().equalsIgnoreCase("en") ?
                Language.ENGLISH : null;
        this.isRegisteredUser = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.enabled = googleResponseWrapper.isEmail_verified();
        this.emailVerified = googleResponseWrapper.isEmail_verified();
        this.mobileVerified = false;

    }


    public User(UserRequestDto userRequestDto) {
        this.id = USER_ID_PREFIX + UUID.randomUUID();
        this.fullName = userRequestDto.getFullName().trim();
        this.email = userRequestDto.getEmail() != null && !userRequestDto.getEmail().isEmpty() ?
                userRequestDto.getEmail().trim() : null;
        this.mobileNo = userRequestDto.getMobileNo().getNo();
        this.mobileNoAsUserName = userRequestDto.getMobileNo().getNo().replace("-", "");
        this.password = bCryptPasswordEncoder.encode(userRequestDto.getPassword());
        this.language = Language.getLanguage(userRequestDto.getLanguage());
        this.role = new Role(userRequestDto.getRoleType());
        this.isRegisteredUser = true;
        this.enabled = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.emailVerified = false;
        this.mobileVerified = true;
    }

    public User(UnregisterUserDto unregisterUserDto) {
        this.id = USER_ID_PREFIX + UUID.randomUUID();
        this.mobileNo = unregisterUserDto.getMobileNo().getNo();
        this.language = Language.getLanguage(unregisterUserDto.getLanguage());
        this.isRegisteredUser = false;
        this.enabled = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public User(FacebookResponseWrapper facebookResponseWrapper) {
        this.id = USER_ID_PREFIX + facebookResponseWrapper.getId();
        this.fullName = facebookResponseWrapper.getName();
        this.email = facebookResponseWrapper.getEmail();
        this.language = Language.ENGLISH;
        this.isRegisteredUser = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public User(UserUpdateRequestDto userUpdateRequestDto) {
        this.fullName = userUpdateRequestDto.getFullName();
    }

    /**
     * This method is used to create guest user.
     */
    public void createGuestUser() {
        String uuid = String.valueOf(UUID.randomUUID());
        this.id = USER_ID_PREFIX + uuid;
        this.fullName = RoleType.GUEST.toString();
        this.mobileNoAsUserName = GUEST_USERNAME_PREFIX + uuid;
        this.password = bCryptPasswordEncoder.encode(GUEST_PW_PREFIX + uuid);
        this.language = Language.ENGLISH;
        this.role = new Role(RoleType.GUEST);
        this.isRegisteredUser = false;
        this.enabled = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.emailVerified = false;
        this.mobileVerified = false;
    }
}
