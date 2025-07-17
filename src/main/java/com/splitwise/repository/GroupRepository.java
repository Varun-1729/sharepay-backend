package com.splitwise.repository;

import com.splitwise.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    /**
     * Find groups by name containing the given string (case-insensitive)
     * @param name the name pattern to search for
     * @return list of groups matching the pattern
     */
    @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Group> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find all groups ordered by name
     * @return list of groups ordered by name
     */
    @Query("SELECT g FROM Group g ORDER BY g.name ASC")
    List<Group> findAllOrderByName();
    
    /**
     * Find groups that a specific user is a member of
     * @param userId the user ID
     * @return list of groups the user belongs to
     */
    @Query("SELECT g FROM Group g JOIN g.members gm WHERE gm.user.id = :userId")
    List<Group> findGroupsByUserId(@Param("userId") Long userId);
    
    /**
     * Check if a group exists with the given name
     * @param name the group name to check
     * @return true if group exists, false otherwise
     */
    boolean existsByName(String name);
}
