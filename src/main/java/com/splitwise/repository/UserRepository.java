package com.splitwise.repository;

import com.splitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email address
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a user exists with the given email
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by name containing the given string (case-insensitive)
     * @param name the name pattern to search for
     * @return list of users matching the pattern
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find users by phone number
     * @param phone the phone number to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * Find all users ordered by name
     * @return list of users ordered by name
     */
    @Query("SELECT u FROM User u ORDER BY u.name ASC")
    List<User> findAllOrderByName();
    
    /**
     * Find users who are members of a specific group
     * @param groupId the group ID
     * @return list of users in the group
     */
    @Query("SELECT u FROM User u JOIN u.groupMemberships gm WHERE gm.group.id = :groupId")
    List<User> findUsersByGroupId(@Param("groupId") Long groupId);

    /**
     * Find users that are in the same groups as the specified user
     * @param userId the user ID
     * @return list of users in the same groups
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.groupMemberships gm1 " +
           "WHERE gm1.group.id IN (SELECT gm2.group.id FROM GroupMember gm2 WHERE gm2.user.id = :userId) " +
           "ORDER BY u.name")
    List<User> findUsersInSameGroupsAs(@Param("userId") Long userId);

    /**
     * Find users created by a specific user
     * @param createdBy the user who created the users
     * @return list of users created by the specified user
     */
    @Query("SELECT u FROM User u WHERE u.createdBy = :createdBy ORDER BY u.name")
    List<User> findByCreatedBy(@Param("createdBy") User createdBy);

    /**
     * Find users created by a specific user ID, including the creator themselves
     * @param createdById the ID of the user who created the users
     * @return list of users created by the specified user + the creator
     */
    @Query("SELECT u FROM User u WHERE u.createdBy.id = :createdById OR u.id = :createdById ORDER BY u.name")
    List<User> findByCreatedByIdIncludingSelf(@Param("createdById") Long createdById);
}
