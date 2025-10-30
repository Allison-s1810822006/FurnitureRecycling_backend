package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // 關聯：行程（單向；Schedule 端沒有回指集合，故不需 Json*Reference）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    // 關聯：清運站（外鍵，station_id 對應 DP001~DP005）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", referencedColumnName = "station_id", nullable = false)
    private Station station;

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(name = "suggested_vehicle")
    private String suggestedVehicle;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    @Column(name = "total_volume_m3", nullable = false)
    private BigDecimal totalVolumeM3 = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 家具項目清單，與 FurnitureItem 一對多關聯
     * 申請單可包含多個家具細分選項
     */
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<FurnitureItem> furnitureItems = new java.util.ArrayList<>();

    /** 家具細項清單（申請單關聯的所有家具細項） */
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

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
