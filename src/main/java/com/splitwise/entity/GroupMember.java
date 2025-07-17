package com.splitwise.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
public class GroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Prevent circular reference in JSON serialization
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore // Prevent circular reference in JSON serialization
    private Group group;
    
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
    
    // Constructors
    public GroupMember() {
    }
    
    public GroupMember(User user, Group group) {
        this.user = user;
        this.group = group;
        this.joinedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Group getGroup() {
        return group;
    }
    
    public void setGroup(Group group) {
        this.group = group;
    }
    
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
    
    @Override
    public String toString() {
        return "GroupMember{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", groupId=" + (group != null ? group.getId() : null) +
                ", joinedAt=" + joinedAt +
                '}';
    }
}
