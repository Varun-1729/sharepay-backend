package com.splitwise.repository;

import com.splitwise.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    /**
     * Find all members of a specific group
     * @param groupId the group ID
     * @return list of group members
     */
    List<GroupMember> findByGroupId(Long groupId);
    
    /**
     * Find all groups a user is a member of
     * @param userId the user ID
     * @return list of group memberships
     */
    List<GroupMember> findByUserId(Long userId);
    
    /**
     * Find a specific group membership
     * @param userId the user ID
     * @param groupId the group ID
     * @return Optional containing the group membership if found
     */
    Optional<GroupMember> findByUserIdAndGroupId(Long userId, Long groupId);
    
    /**
     * Check if a user is a member of a specific group
     * @param userId the user ID
     * @param groupId the group ID
     * @return true if user is a member, false otherwise
     */
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);
    
    /**
     * Count the number of members in a group
     * @param groupId the group ID
     * @return number of members
     */
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId")
    long countMembersByGroupId(@Param("groupId") Long groupId);
    
    /**
     * Delete all memberships for a specific group
     * @param groupId the group ID
     */
    void deleteByGroupId(Long groupId);
    
    /**
     * Delete all memberships for a specific user
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);
}
