package com.splitwise.controller;

import com.splitwise.dto.GroupDTO;
import com.splitwise.dto.UserDTO;
import com.splitwise.entity.Group;
import com.splitwise.entity.GroupMember;
import com.splitwise.entity.User;
import com.splitwise.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*") // Allow CORS for frontend
public class GroupController {
    
    private final GroupService groupService;
    
    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }
    
    /**
     * Create a new group
     * POST /api/groups
     */
    @PostMapping
    public ResponseEntity<?> createGroup(@Valid @RequestBody Group group) {
        try {
            Group createdGroup = groupService.createGroup(group);
            GroupDTO groupDTO = new GroupDTO(createdGroup);
            return ResponseEntity.status(HttpStatus.CREATED).body(groupDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to create group: " + e.getMessage()));
        }
    }
    
    /**
     * Get all groups
     * GET /api/groups
     */
    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        List<GroupDTO> groupDTOs = groups.stream()
                .map(GroupDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groupDTOs);
    }
    
    /**
     * Get group by ID
     * GET /api/groups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Long id) {
        Optional<Group> group = groupService.getGroupById(id);
        if (group.isPresent()) {
            return ResponseEntity.ok(group.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Group not found with id: " + id));
        }
    }
    
    /**
     * Update group
     * PUT /api/groups/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Long id, @Valid @RequestBody Group group) {
        try {
            Group updatedGroup = groupService.updateGroup(id, group);
            return ResponseEntity.ok(updatedGroup);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update group: " + e.getMessage()));
        }
    }
    
    /**
     * Delete group
     * DELETE /api/groups/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.ok(new SuccessResponse("Group deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to delete group: " + e.getMessage()));
        }
    }
    
    /**
     * Add user to group
     * POST /api/groups/{groupId}/users/{userId}
     */
    @PostMapping("/{groupId}/users/{userId}")
    public ResponseEntity<?> addUserToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            GroupMember groupMember = groupService.addUserToGroup(groupId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body("User added to group successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to add user to group: " + e.getMessage()));
        }
    }
    
    /**
     * Remove user from group
     * DELETE /api/groups/{groupId}/users/{userId}
     */
    @DeleteMapping("/{groupId}/users/{userId}")
    public ResponseEntity<?> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            groupService.removeUserFromGroup(groupId, userId);
            return ResponseEntity.ok(new SuccessResponse("User removed from group successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to remove user from group: " + e.getMessage()));
        }
    }
    
    /**
     * Get all users in a group
     * GET /api/groups/{groupId}/users
     */
    @GetMapping("/{groupId}/users")
    public ResponseEntity<?> getUsersInGroup(@PathVariable Long groupId) {
        try {
            List<User> users = groupService.getUsersInGroup(groupId);
            List<UserDTO> userDTOs = users.stream()
                    .map(UserDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to get users in group: " + e.getMessage()));
        }
    }
    
    /**
     * Get all groups a user belongs to
     * GET /api/groups/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getGroupsByUserId(@PathVariable Long userId) {
        try {
            List<Group> groups = groupService.getGroupsByUserId(userId);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to get groups for user: " + e.getMessage()));
        }
    }
    
    /**
     * Search groups by name
     * GET /api/groups/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Group>> searchGroups(@RequestParam String name) {
        List<Group> groups = groupService.searchGroupsByName(name);
        return ResponseEntity.ok(groups);
    }
    
    /**
     * Get group member count
     * GET /api/groups/{groupId}/count
     */
    @GetMapping("/{groupId}/count")
    public ResponseEntity<?> getGroupMemberCount(@PathVariable Long groupId) {
        try {
            long count = groupService.getGroupMemberCount(groupId);
            return ResponseEntity.ok(new CountResponse(count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to get group member count: " + e.getMessage()));
        }
    }
    
    // Response classes
    public static class ErrorResponse {
        private String error;
        private long timestamp;
        
        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getError() { return error; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class SuccessResponse {
        private String message;
        private long timestamp;
        
        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class CountResponse {
        private long count;
        private long timestamp;
        
        public CountResponse(long count) {
            this.count = count;
            this.timestamp = System.currentTimeMillis();
        }
        
        public long getCount() { return count; }
        public long getTimestamp() { return timestamp; }
    }
}
