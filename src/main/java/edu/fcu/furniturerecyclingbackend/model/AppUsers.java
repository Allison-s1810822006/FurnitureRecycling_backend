package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import jakarta.persistence.GeneratedValue;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * AppUsers
 * 用戶資料 Entity，對應 app_users 資料表。
 * 包含基本資料、LINE 綁定資訊。
 */
@Getter
@Setter
@Entity
@Table(name = "app_users")
public class AppUsers {
    /** 用戶主鍵 UUID，對應 app_users.user_id */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;  // 主鍵，使用 UUID 類型

    /** 用戶姓名，對應 app_users.full_name */
    @Column(nullable = false)
    private String fullName;

    /** 是否為管理員，對應 app_users.is_admin */
    private Boolean isAdmin;
    /** 用戶 email，對應 app_users.email */
    private String email;
    /** 用戶電話，對應 app_users.phone */
    private String phone;

    /** LINE user id，對應 app_users.line_user_id */
    @Column(name = "line_user_id", unique = true)
    private String lineUserId;
    /** LINE 顯示名稱，對應 app_users.line_display_name */
    @Column(name = "line_display_name")
    private String lineDisplayName;
    /** LINE 頭像網址，對應 app_users.line_picture_url */
    @Column(name = "line_picture_url")
    private String linePictureUrl;
    /** LINE 綁定時間，對應 app_users.line_bound_at */
    @Column(name = "line_bound_at")
    private OffsetDateTime lineBoundAt;
    /** LINE email，對應 app_users.line_email */
    @Column(name = "line_email")
    private String lineEmail;

    /** 預設建構子，供 JPA 使用 */
    public AppUsers() {}
}
