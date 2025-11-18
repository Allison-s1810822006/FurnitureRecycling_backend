package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * AppUsers
 * 用戶資料 Entity，對應 app_users 資料表。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_users")
public class AppUsers {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "is_admin")
    private Boolean isAdmin;

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

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "is_member", nullable = false)
    private Boolean isMember;

    // No-args constructor provided by Lombok @NoArgsConstructor
}
