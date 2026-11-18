package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.AuthUserDetail;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.repository.UserDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * UserDetails Service
 */
@Slf4j
@Service("userDetailsService")
public class UserDetailService implements UserDetailsService {

    private final UserDetailRepository userDetailRepository;

    @Autowired
    public UserDetailService(UserDetailRepository userDetailRepository) {
        this.userDetailRepository = userDetailRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String name) {

        // Normalize phone number by removing spaces and hyphens to match stored format
        // Java 11 compatibility: handles phone numbers with spaces/hyphens in requests
        String normalizedName = name;
        if (name != null && name.contains("+")) {
            // Phone number: remove spaces and hyphens
            normalizedName = name.replaceAll("[\\s-]", "");
        }

        // Search by mobileNo, mobileNoAsUserName, or email
        // This supports different user registration formats where phone number may be stored differently
        Optional<User> user = userDetailRepository.findByMobileNoOrMobileNoAsUserNameOrEmail(normalizedName);

        if (user.isEmpty()) {
            // Try original name in case it's an ID
            user = userDetailRepository.findById(name);
            if (user.isEmpty()) {
                throw new UsernameNotFoundException("Invalid username or Password");
            }
        }
        return new AuthUserDetail(user.get());
    }
}
