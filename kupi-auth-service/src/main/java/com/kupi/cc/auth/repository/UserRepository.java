package com.kupi.cc.auth.repository;

import com.kupi.cc.auth.domain.entity.Role;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * User repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * This method finds and returns relevant user if given mobile no is existed.
     *
     * @param mobileNo mobile no
     * @return user / null
     */
    User findByMobileNo(String mobileNo);

    /**
     * This method finds and returns relevant user if given email address is existed.
     *
     * @param email email address
     * @return user/ null
     */
    User findByEmail(String email);

    /**
     * This method finds and returns relevant user if given userId is existed
     *
     * @param userId userId
     * @return user
     */
    Optional<User> findById(String userId);

    /**
     * This method find registered user by mobileNo
     *
     * @param mobileNo mobile no
     * @return user
     */
    User findByMobileNoOrEmailAndIsRegisteredUserTrue(String mobileNo, String email);

    /**
     * This method finds registered user by mobileNo, mobileNoAsUserName, or email
     * Searches by all three fields to support different user registration formats
     * where phone number may be stored in different fields
     *
     * @param username username (can be mobileNo, mobileNoAsUserName, or email)
     * @return user
     */
    @Query("SELECT u FROM User u WHERE (u.mobileNo = :username OR u.mobileNoAsUserName = :username OR u.email = :username) AND u.isRegisteredUser = true")
    User findRegisteredUserByMobileNoOrMobileNoAsUserNameOrEmail(@Param("username") String username);

    /**
     * This method finds any user (registered or guest) by mobileNo, mobileNoAsUserName, or email
     * Used for password grant authentication to allow both registered and guest users
     *
     * @param username username (can be mobileNo, mobileNoAsUserName, or email)
     * @return user
     */
    @Query("SELECT u FROM User u WHERE (u.mobileNo = :username OR u.mobileNoAsUserName = :username OR u.email = :username)")
    User findUserByMobileNoOrMobileNoAsUserNameOrEmail(@Param("username") String username);

    /**
     * This method finds registered user by mobileNoAsUserName or with email.
     *
     * @param mobileNo mobileNoAsUserName
     * @param email    email
     * @return user
     */
    User findByMobileNoAsUserNameOrEmail(String mobileNo, String email);

    /**
     * This method finds the users by role
     *
     * @param role role
     * @return list of users
     */
    List<User> findByRole(Role role);

    /**
     * This method creates active user page by role id.
     *
     * @param roleId   roleId
     * @param pageable pageable
     * @return user page.
     */
    Page<User> findByRoleIdAndEnabledTrue(int roleId, Pageable pageable);

    /**
     * This method creates a list of users by role id.
     *
     * @param roleId roleId
     * @return user list.
     */
    List<User> findByRoleId(int roleId);

    /**
     * This method creates active user page with role id & full name.
     *
     * @param roleId     roleId
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return user page.
     */
    Page<User> findByRoleIdAndEnabledTrueAndFullNameContaining(int roleId, String searchTerm, Pageable pageable);

    /**
     * This method will return all pending merchants.
     *
     * @param roleId         roleId
     * @param approvalStatus approvalStatus
     * @param pageable       pageable
     * @return pending merchants
     */
    Page<User> findByRoleIdAndApprovalStatus(int roleId, ApprovalStatus approvalStatus, Pageable pageable);

    /**
     * This method will find users with approval status & fullName.
     *
     * @param approvalStatus approvalStatus
     * @param searchTerm     searchTerm
     * @param pageable       pageable
     * @return users
     */
    Page<User> findByApprovalStatusAndFullNameContaining(ApprovalStatus approvalStatus, String searchTerm,
                                                         Pageable pageable);

    /**
     * This method is used to get total user count by user roleId.
     *
     * @param roleId roleId
     * @return total number of users for particular roleId.
     */
    int countByRoleIdAndEnabledTrue(int roleId);

    /**
     * This method is used to get total count of new users for a roleId.
     *
     * @param roleId roleId
     * @param date   date
     * @return total number of new users for particular roleId.
     */
    int countByRoleIdAndEnabledTrueAndCreatedAtGreaterThanEqual(int roleId, Date date);

    /**
     * This method is used to get active users.
     *
     * @param userId userId
     * @return user.
     */
    Optional<User> findByIdAndEnabledTrue(String userId);

    /**
     * This method is used to get active users by list of ids.
     *
     * @param userIds userIds
     * @return list of users.
     */
    List<User> findByIdInAndEnabledTrue(List<String> userIds);
}
