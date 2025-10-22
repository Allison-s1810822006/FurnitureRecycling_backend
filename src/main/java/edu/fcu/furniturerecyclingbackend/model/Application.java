package edu.fcu.furniturerecyclingbackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter
//與原先 @setter 的差異
//先前每個 setter 內有 isLocked() 判斷（會印錯誤訊息並 return），那是「欄位層級」的防護，允許程式進入編輯畫面但在嘗試改欄位時漸進式阻止。
//現在改為「流程一進入就阻止」（更符合：只要狀態為已受理，整筆訂單無法進入編輯狀態），更早期阻止，使用者或前端也可以不用處理單欄位被拒的混亂行為。
//若沒有拋例外，service 用這些「plain setters」改值
//ApplicationService.update() 讀取實體，第一件事呼叫 app.ensureEditable()（若已受理，立即 throw IllegalArgumentException，API 回 400，不會進入任何 setter）。
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

    // 關聯：清運站（雙向的一方 → 這裡是「回指」）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    @JsonBackReference("station-apps") // ← 防止 Jackson/Springdoc 遞迴
    private Station station;

    @Column(name = "drop_point_code")
    private String dropPointCode;

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

    // --------- Plain setters (不在 setter 內做鎖定判斷) ---------
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
    public void setStation(Station station) { this.station = station; }
    public void setDropPointCode(String dropPointCode) { this.dropPointCode = dropPointCode; }
    public void setRequestedDate(LocalDate requestedDate) { this.requestedDate = requestedDate; }
    public void setSuggestedVehicle(String suggestedVehicle) { this.suggestedVehicle = suggestedVehicle; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    public void setTotalVolumeM3(BigDecimal totalVolumeM3) { this.totalVolumeM3 = totalVolumeM3; }

    /**
     * setStatus 不受鎖定影響（需要有人可以從 SUBMITTED -> APPROVED）。
     */
    public void setStatus(ApplicationStatus status) { this.status = status; }

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
