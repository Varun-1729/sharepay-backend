package com.splitwise.service;

import com.splitwise.entity.User;
import com.splitwise.repository.ExpenseSplitRepository;
import com.splitwise.repository.UserRepository;
import com.splitwise.util.SecurityUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final SecurityUtil securityUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, ExpenseSplitRepository expenseSplitRepository, SecurityUtil securityUtil) {
        this.userRepository = userRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.securityUtil = securityUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * Create a new user
     * @param user the user to create
     * @return the created user
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }

        // Set default password if not provided (for users created via API)
        if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().trim().isEmpty()) {
            String defaultPassword = passwordEncoder.encode("defaultpassword123");
            user.setPassword(defaultPassword);
        }

        // Set the creator of this user (for user isolation)
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser != null) {
            user.setCreatedBy(currentUser);
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * Get users visible to the current user (users they created + themselves)
     * @return list of users created by current user + current user, ordered by name
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser == null) {
            return List.of(); // Return empty list if no user logged in
        }

        // Return users created by the current user + the current user themselves
        return userRepository.findByCreatedByIdIncludingSelf(currentUser.getId());
    }

    /**
     * Get users that share groups with the current user (for viewing expenses/balances)
     * @return list of users in the same groups as current user
     */
    @Transactional(readOnly = true)
    public List<User> getUsersInSameGroups() {
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser == null) {
            return List.of(); // Return empty list if no user logged in
        }
        return userRepository.findUsersInSameGroupsAs(currentUser.getId());
    }
    
    /**
     * Get user by ID
     * @param id the user ID
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get user by email
     * @param email the email address
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Update an existing user
     * @param id the user ID
     * @param updatedUser the updated user data
     * @return the updated user
     * @throws IllegalArgumentException if user not found or email conflict
     */
    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Check if email is being changed and if new email already exists
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) && 
            userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("User with email " + updatedUser.getEmail() + " already exists");
        }
        
        // Update fields
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        
        return userRepository.save(existingUser);
    }
    
    /**
     * Delete a user by ID
     * @param id the user ID
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        // Delete all expense splits for this user first
        expenseSplitRepository.deleteByOwedById(id);

        // Delete all expenses paid by this user (this will cascade to splits)
        // Note: This is handled by the database cascade settings

        // Now delete the user (group memberships will be handled by cascade)
        userRepository.deleteById(id);
    }
    
    /**
     * Search users by name
     * @param name the name pattern to search for
     * @return list of users matching the pattern
     */
    @Transactional(readOnly = true)
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Check if user exists by email
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Get users in a specific group
     * @param groupId the group ID
     * @return list of users in the group
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByGroupId(Long groupId) {
        return userRepository.findUsersByGroupId(groupId);
    }
}
