package sk.ikts.server.dto;

/**
 * Data Transfer Object for Group Member
 * Includes user information and role in the group
 */
public class MemberDTO {
    private Long userId;
    private String email;
    private String name;
    private String role; // ADMIN or MEMBER

    public MemberDTO() {
    }

    public MemberDTO(Long userId, String email, String name, String role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

