package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "app_users")
public class AppUsers {
    @Id
    private UUID userId;  // 主鍵，使用 UUID 類型

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String passwordHash;

    private Boolean isAdmin;

    private String email;

    private String phone;

    private String address;

    @Column(name = "line_user_id", unique = true)
    private String lineUserId;

    @Column(name = "line_display_name")
    private String lineDisplayName;

    @Column(name = "line_picture_url")
    private String linePictureUrl;

    @Column(name = "line_bound_at")
    private OffsetDateTime lineBoundAt;

    @Column(name = "line_email")
    private String lineEmail;

    // Constructor
    public AppUsers() {
    }

    public AppUsers(String fullName, String email, String encodedPassword) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = encodedPassword;
    }

    // Override toString, equals, and hashCode if necessary
}
