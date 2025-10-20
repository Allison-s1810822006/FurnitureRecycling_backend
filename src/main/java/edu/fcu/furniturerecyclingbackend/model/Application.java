package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @Column(name = "application_id", columnDefinition = "uuid")
    private UUID applicationId; // 對齊資料表欄位名稱

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "station_id", columnDefinition = "uuid")
    private UUID stationId;

    @Column(name = "schedule_id", columnDefinition = "uuid")
    private UUID scheduleId;

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Column(name = "status", nullable = false)
    private String status = "SUBMITTED";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "drop_point_code")
    private String dropPointCode;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    @Column(name = "total_volume_m3", precision = 10, scale = 3, nullable = false)
    private BigDecimal totalVolumeM3 = BigDecimal.ZERO;

    @Column(name = "suggested_vehicle", nullable = false)
    private String suggestedVehicle = "FLATBED";

    // 一對多：一張申請有多筆家具
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FurnitureItem> items = new ArrayList<>();

    /** 自動帶入 UUID / 時間戳 */
    @PrePersist
    public void prePersist() {
        if (this.applicationId == null) {
            this.applicationId = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    /** 關聯維護方法 */
    public void addItem(FurnitureItem item) {
        if (item == null) return;
        item.setApplication(this);   // 反向關聯
        this.items.add(item);
    }

    public void setItems(List<FurnitureItem> items) {
        this.items.clear();
        if (items != null) {
            for (FurnitureItem it : items) addItem(it);
        }
    }
}
