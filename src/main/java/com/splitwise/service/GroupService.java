package com.splitwise.service;

import com.splitwise.entity.Expense;
import com.splitwise.entity.Group;
import com.splitwise.entity.GroupMember;
import com.splitwise.entity.User;
import com.splitwise.repository.ExpenseRepository;
import com.splitwise.repository.ExpenseSplitRepository;
import com.splitwise.repository.GroupRepository;
import com.splitwise.repository.GroupMemberRepository;
import com.splitwise.repository.UserRepository;
import com.splitwise.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public GroupService(GroupRepository groupRepository,
                       GroupMemberRepository groupMemberRepository,
                       UserRepository userRepository,
                       ExpenseRepository expenseRepository,
                       ExpenseSplitRepository expenseSplitRepository,
                       SecurityUtil securityUtil) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.securityUtil = securityUtil;
    }
    
    /**
     * Create a new group
     * @param group the group to create
     * @return the created group
     */
    @Transactional
    public Group createGroup(Group group) {
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        Group savedGroup = groupRepository.save(group);

        // Automatically add the current user as a member of the group
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser != null) {
            addUserToGroupInternal(savedGroup, currentUser);
        }

        return savedGroup;
    }

    /**
     * Internal method to add user to group without authorization checks
     * @param group the group
     * @param user the user
     * @return the created group membership
     */
    private GroupMember addUserToGroupInternal(Group group, User user) {
        if (groupMemberRepository.existsByUserIdAndGroupId(user.getId(), group.getId())) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        GroupMember groupMember = new GroupMember(user, group);
        return groupMemberRepository.save(groupMember);
    }
    
    /**
     * Get all groups for the current user
     * @return list of groups where current user is a member
     */
    @Transactional(readOnly = true)
    public List<Group> getAllGroups() {
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser == null) {
            return List.of(); // Return empty list if no user logged in
        }
        return groupRepository.findGroupsByUserId(currentUser.getId());
    }
    
    /**
     * Get group by ID
     * @param id the group ID
     * @return Optional containing the group if found
     */
    @Transactional(readOnly = true)
    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }
    
    /**
     * Update an existing group
     * @param id the group ID
     * @param updatedGroup the updated group data
     * @return the updated group
     * @throws IllegalArgumentException if group not found
     */
    public Group updateGroup(Long id, Group updatedGroup) {
        Group existingGroup = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));
        
        existingGroup.setName(updatedGroup.getName());
        existingGroup.setDescription(updatedGroup.getDescription());
        
        return groupRepository.save(existingGroup);
    }
    
    /**
     * Delete a group by ID
     * @param id the group ID
     * @throws IllegalArgumentException if group not found
     */
    @Transactional
    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new IllegalArgumentException("Group not found with id: " + id);
        }

        // Delete all expenses and their splits first
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByExpenseDateDesc(id);
        for (Expense expense : expenses) {
            expenseSplitRepository.deleteByExpenseId(expense.getId());
        }
        expenseRepository.deleteByGroupId(id);

        // Delete all group memberships
        groupMemberRepository.deleteByGroupId(id);

        // Then delete the group
        groupRepository.deleteById(id);
    }
    
    /**
     * Add a user to a group
     * @param groupId the group ID
     * @param userId the user ID
     * @return the created group membership
     * @throws IllegalArgumentException if group or user not found, or if user already in group
     */
    @Transactional
    public GroupMember addUserToGroup(Long groupId, Long userId) {
        // Check if current user is a member of the group (authorization)
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser != null && !groupMemberRepository.existsByUserIdAndGroupId(currentUser.getId(), groupId)) {
            throw new IllegalArgumentException("You are not authorized to add users to this group");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (groupMemberRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        GroupMember groupMember = new GroupMember(user, group);
        return groupMemberRepository.save(groupMember);
    }
    
    /**
     * Remove a user from a group
     * @param groupId the group ID
     * @param userId the user ID
     * @throws IllegalArgumentException if group membership not found
     */
    public void removeUserFromGroup(Long groupId, Long userId) {
        GroupMember groupMember = groupMemberRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this group"));
        
        groupMemberRepository.delete(groupMember);
    }
    
    /**
     * Get all members of a group
     * @param groupId the group ID
     * @return list of group members
     */
    @Transactional(readOnly = true)
    public List<GroupMember> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }
    
    /**
     * Get all users in a group
     * @param groupId the group ID
     * @return list of users in the group
     */
    @Transactional(readOnly = true)
    public List<User> getUsersInGroup(Long groupId) {
        return userRepository.findUsersByGroupId(groupId);
    }
    
    /**
     * Get all groups a user belongs to
     * @param userId the user ID
     * @return list of groups the user is a member of
     */
    @Transactional(readOnly = true)
    public List<Group> getGroupsByUserId(Long userId) {
        return groupRepository.findGroupsByUserId(userId);
    }
    
    /**
     * Search groups by name
     * @param name the name pattern to search for
     * @return list of groups matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Group> searchGroupsByName(String name) {
        return groupRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Check if a user is a member of a group
     * @param userId the user ID
     * @param groupId the group ID
     * @return true if user is a member, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isUserMemberOfGroup(Long userId, Long groupId) {
        return groupMemberRepository.existsByUserIdAndGroupId(userId, groupId);
    }
    
    /**
     * Get the number of members in a group
     * @param groupId the group ID
     * @return number of members
     */
    @Transactional(readOnly = true)
    public long getGroupMemberCount(Long groupId) {
        return groupMemberRepository.countMembersByGroupId(groupId);
    }
}
