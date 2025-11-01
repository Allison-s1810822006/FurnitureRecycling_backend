package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Application
 * 家具回收申請單 Entity，對應 applications 資料表。
 * 包含申請人、站點、行程、申請日期、狀態、統計欄位、細項等。
 */
@Getter
@Setter
@Entity
@Table(name = "applications")
public class Application {
    /** 申請單主鍵，UUID */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "application_id")
    private UUID applicationId;

    /** 申請人主鍵，對應 app_users.user_id */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * 關聯：行程（Schedule），外鍵 schedule_id
     * 單向關聯，Schedule 端無回指集合
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    /**
     * 關聯：清運站（Station），外鍵 station_id
     * 對應 stations 資料表主鍵
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", referencedColumnName = "station_id", nullable = false)
    private Station station;

    /** 申請日期，對應 requested_date 欄位 */
    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    /** 申請狀態，enum 對應 status 欄位 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    /** 建議車型，對應 suggested_vehicle 欄位 */
    @Column(name = "suggested_vehicle")
    private String suggestedVehicle;

    /** 家具總件數，對應 total_items 欄位 */
    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    /** 家具總體積（立方米），對應 total_volume_m3 欄位 */
    @Column(name = "total_volume_m3", nullable = false)
    private BigDecimal totalVolumeM3 = BigDecimal.ZERO;

    /** 申請建立時間，對應 created_at 欄位 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** 申請更新時間，對應 updated_at 欄位 */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 家具細項清單（申請單關聯的所有家具細項）
     * 一對多關聯，mappedBy = "application"
     */
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationItem> applicationItems;

    /**
     * 檢查此申請單是否被鎖定（已受理）。
     * 回傳 true 表示不能被修改或刪除。
     */
    public boolean isLocked() {
        return this.status == ApplicationStatus.APPROVED;
    }

    /**
     * 在嘗試進入編輯流程前呼叫此方法以驗證是否允許編輯；
     * 若已受理則拋出 IllegalArgumentException（由 GlobalExceptionHandler 轉成 400）。
     */
    public void ensureEditable() {
        if (isLocked()) {
            throw new IllegalArgumentException("錯誤：案件 (ID: " + this.applicationId + ") 已受理，無法進入編輯狀態。");
        }
    }

    /**
     * 新增時自動設定建立/更新時間
     */
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * 更新時自動設定更新時間
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
