package sk.ikts.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing membership of a user in a group
 * Links users to groups with a role (member/admin)
 */
@Entity
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "membership_id")
    private Long membershipId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    // Enum for user roles in groups
    public enum Role {
        MEMBER,
        ADMIN
    }

    // Constructors
    public Membership() {
        this.joinedAt = LocalDateTime.now();
        this.role = Role.MEMBER;
    }

    public Membership(Long userId, Long groupId, Role role) {
        this.userId = userId;
        this.groupId = groupId;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}

