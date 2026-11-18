package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.Role;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.enums.ApprovalStatus;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.repository.UserDetailRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class UserDetailsServiceTest {

    private static final String USER_EMAIL = "rajith@tokoin.io";
    private static final String USER_MOBILE_NO = "+6281123123";
    private static final String USER_ID = "uid-1234567890";

    @Mock
    private UserDetailRepository userDetailRepository;
    private UserDetailService userDetailService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        userDetailService = new UserDetailService(userDetailRepository);
    }

    @AfterEach
    void tearDown() {}

    @Test
    void Should_ThrowException_When_UserNotFound() {
        when(userDetailRepository.findByMobileNoOrMobileNoAsUserNameOrEmail(USER_EMAIL))
                .thenReturn(Optional.empty());
        when(userDetailRepository.findById(USER_EMAIL)).thenReturn(Optional.empty());
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailService.loadUserByUsername(USER_EMAIL));
        assertEquals("Invalid username or Password", exception.getMessage());
    }

    @Test
    void Should_ReturnUser_ValidUserNameIsProvided() {
        User user = getSampleMerchant();
        when(userDetailRepository.findByMobileNoOrMobileNoAsUserNameOrEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));
        assertEquals(user.getId(), userDetailService.loadUserByUsername(USER_EMAIL).getUsername());
    }
    /**
     * This method creates sample Merchant
     *
     * @return User
     */
    private User getSampleMerchant() {
        User merchant = new User();
        merchant.setId(USER_ID);
        merchant.setFullName("Full name");
        merchant.setEmail("merchant@kupi.lk");
        merchant.setMobileNo("94-771109101");
        merchant.setApprovalStatus(ApprovalStatus.PENDING);
        merchant.setRegisteredUser(true);
        merchant.setCreatedAt(new Date(System.currentTimeMillis()));
        Role role = new Role();
        role.setId(3);
        role.setName(RoleType.MERCHANT.name());
        merchant.setRole(role);
        return merchant;
    }
}
