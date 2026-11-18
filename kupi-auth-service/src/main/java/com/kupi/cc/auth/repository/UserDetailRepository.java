package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailRepository extends JpaRepository<User, String> {

    /**
     * This method returns user based on mobileNo or email
     *
     * @param mobileNo mobile number
     * @param email    email
     * @return user/null
     */
    Optional<User> findByMobileNoAsUserNameOrEmail(String mobileNo, String email);

    /**
     * This method returns user based on mobileNo or mobileNoAsUserName or email
     * Searches by all three fields to support different user registration formats
     *
     * @param username username (can be mobileNo, mobileNoAsUserName, or email)
     * @return user/null
     */
    @Query("SELECT u FROM User u WHERE u.mobileNo = :username OR u.mobileNoAsUserName = :username OR u.email = :username")
    Optional<User> findByMobileNoOrMobileNoAsUserNameOrEmail(@Param("username") String username);

}
