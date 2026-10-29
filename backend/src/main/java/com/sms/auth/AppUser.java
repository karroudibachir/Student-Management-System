package com.sms.auth;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id @Column(length = 255) private String subject;
    @Column(length = 254) private String email;
    @Column(name = "display_name", length = 160) private String displayName;
    @Column(name = "password_hash", length = 100) private String passwordHash;
    @Column(name = "student_id", unique = true) private Long studentId;
    @Column(name = "last_seen_at", nullable = false) private OffsetDateTime lastSeenAt;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_roles", joinColumns = @JoinColumn(name = "subject"))
    @Enumerated(EnumType.STRING) @Column(name = "role", nullable = false, length = 16)
    private Set<AppRole> roles = new LinkedHashSet<>();

    protected AppUser() {}
    public AppUser(String subject, String email, String displayName) {
        this.subject = subject; this.email = email; this.displayName = displayName;
        this.lastSeenAt = OffsetDateTime.now();
    }
    public String getSubject() { return subject; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public OffsetDateTime getLastSeenAt() { return lastSeenAt; }
    public void touch() { this.lastSeenAt = OffsetDateTime.now(); }
    public Set<AppRole> getRoles() { return roles; }
    public void setRoles(Set<AppRole> roles) { this.roles = new LinkedHashSet<>(roles); }
}
