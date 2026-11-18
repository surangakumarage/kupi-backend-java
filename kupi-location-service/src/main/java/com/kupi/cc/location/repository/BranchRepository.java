package com.kupi.cc.location.repository;

import com.kupi.cc.location.domain.entity.Branch;
import com.kupi.cc.location.domain.entity.GeoLocation;
import com.kupi.cc.location.enums.ApprovalStatus;
import com.kupi.cc.location.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Branch repository
 */
public interface BranchRepository extends JpaRepository<Branch, String> {

    /**
     * This method finds if the branch exists by the given name
     *
     * @param name     branch name
     * @param userId   userId
     * @param branchId branchId
     * @return true/false
     */
    boolean existsByNameAndUserIdAndIdNotAndIsDeletedFalse(String name, String userId, String branchId);

    /**
     * This method finds if the branch address with branch name
     *
     * @param address  branch address
     * @param branchId branchId
     * @return true/false
     */
    boolean existsByAddressAndIdNotAndIsDeletedFalse(String address, String branchId);

    /**
     * This method returns all branches by userId.
     *
     * @param userId   userId
     * @param pageable pageable
     * @return branch page.
     */
    @Query(value = "SELECT b FROM Branch b WHERE b.userId =?1 AND b.isDeleted = false")
    Page<Branch> findByUserId(String userId, Pageable pageable);

    /**
     * This method returns all branches by userId & with searchTerm.
     *
     * @param userId     userId
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return branch page.
     */
    @Query("SELECT b FROM Branch b WHERE b.userId = :userId AND b.isDeleted = false AND (b.name LIKE %:searchTerm% OR " +
            " b.address LIKE %:searchTerm%)")
    Page<Branch> getBranchesByUserIdAndSearchTerm(String userId, String searchTerm, Pageable pageable);

    /**
     * This method returns the geoLocation page.
     *
     * @param pageable  pageable
     * @param userType  userType
     * @param status    status
     * @param globeCode globeCode
     * @return GeoLocation Page
     */
    @Query(value = "select new GeoLocation (b.userId, b.userType,b.globalCode) FROM Branch b WHERE " +
            "b.userType=:userType AND b.approvalStatus=:status AND b.globalCode=:globeCode")
    Page<GeoLocation> getDistinctByUserIdAndUserTypeAndGlobalCode(Pageable pageable, UserType userType,
                                                                  ApprovalStatus status, String globeCode);

    @Query(value = "SELECT DISTINCT b.user_id " +
            "FROM branch b " +
            "WHERE b.user_type = :userType " +
            "AND b.approval_status = :status " +
            "AND b.is_deleted = 0 " +
            "AND b.user_id IN (:preferredMerchantIds) " +
            "AND ST_Distance_Sphere(POINT(b.longitude, b.latitude), POINT(:longitude, :latitude)) <= :radiusMeters",
            nativeQuery = true)
    Page<String> getPreferredMerchantIdListByLocation(Pageable pageable,
                                                      @Param("latitude") double latitude,
                                                      @Param("longitude") double longitude,
                                                      @Param("radiusMeters") double radiusMeters,
                                                      @Param("userType") String userType,
                                                      @Param("status") String status,
                                                      @Param("preferredMerchantIds") List<String> preferredMerchantIds);

    /**
     * This method returns specific merchant IDs by location for newly added preferred merchants.
     * Only searches for the specified merchant IDs within the radius.
     *
     * @param pageable pageable
     * @param latitude latitude
     * @param longitude longitude
     * @param radiusMeters radius in meters
     * @param userType user type
     * @param status approval status
     * @param specificMerchantIds list of specific merchant IDs to search for
     * @return Page of merchant IDs
     */
    @Query(value = "SELECT DISTINCT b.user_id " +
            "FROM branch b " +
            "WHERE b.user_type = :userType " +
            "AND b.approval_status = :status " +
            "AND b.is_deleted = 0 " +
            "AND b.user_id IN (:specificMerchantIds) " +
            "AND ST_Distance_Sphere(POINT(b.longitude, b.latitude), POINT(:longitude, :latitude)) <= :radiusMeters",
            nativeQuery = true)
    Page<String> getSpecificMerchantIdListByLocation(Pageable pageable,
                                                     @Param("latitude") double latitude,
                                                     @Param("longitude") double longitude,
                                                     @Param("radiusMeters") double radiusMeters,
                                                     @Param("userType") String userType,
                                                     @Param("status") String status,
                                                     @Param("specificMerchantIds") List<String> specificMerchantIds);

    /**
     * This method returns all merchant IDs within the specified radius.
     * Unlike getPreferredMerchantIdListByLocation, this doesn't filter by preferred merchants.
     *
     * @param pageable pageable
     * @param latitude latitude
     * @param longitude longitude
     * @param radiusMeters radius in meters
     * @param userType user type
     * @param status approval status
     * @return Page of merchant IDs
     */
    @Query(value = "SELECT DISTINCT b.user_id " +
            "FROM branch b " +
            "WHERE b.user_type = :userType " +
            "AND b.approval_status = :status " +
            "AND b.is_deleted = 0 " +
            "AND ST_Distance_Sphere(POINT(b.longitude, b.latitude), POINT(:longitude, :latitude)) <= :radiusMeters",
            nativeQuery = true)
    Page<String> getAllMerchantIdListByLocation(Pageable pageable,
                                                @Param("latitude") double latitude,
                                                @Param("longitude") double longitude,
                                                @Param("radiusMeters") double radiusMeters,
                                                @Param("userType") String userType,
                                                @Param("status") String status);

    /**
     * This method returns branches for given merchant IDs within the specified radius.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param radiusMeters radius in meters
     * @param userType user type
     * @param status approval status
     * @param merchantIds list of merchant IDs
     * @return List of branches
     */
    @Query(value = "SELECT * FROM branch b " +
            "WHERE b.user_type = :userType " +
            "AND b.approval_status = :status " +
            "AND b.is_deleted = 0 " +
            "AND b.user_id IN (:merchantIds) " +
            "AND ST_Distance_Sphere(POINT(b.longitude, b.latitude), POINT(:longitude, :latitude)) <= :radiusMeters",
            nativeQuery = true)
    List<Branch> getBranchesByMerchantIdsAndLocation(@Param("latitude") double latitude,
                                                     @Param("longitude") double longitude,
                                                     @Param("radiusMeters") double radiusMeters,
                                                     @Param("userType") String userType,
                                                     @Param("status") String status,
                                                     @Param("merchantIds") List<String> merchantIds);

}