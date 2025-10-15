package edu.fcu.furniturerecyclingbackend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;        // 對應 Postgres 的 date
import java.time.OffsetDateTime;  // 對應 Postgres 的 timestamptz
import java.util.UUID;

@Entity
@Table(name = "applications")
public class Application {

    // PK: application_id (uuid)
    @Id
    @GeneratedValue                 // 讓 Hibernate 自動產生 UUID（或用 DB 預設也可）
    @UuidGenerator
    @Column(name = "application_id")
    private UUID applicationId;

    // FK / 參考鍵：都用 uuid
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "station_id")
    private UUID stationId;

    @Column(name = "schedule_id")
    private UUID scheduleId;

    @Column(name = "furniture_id")
    private UUID furnitureId;

    // 申請日期：date -> LocalDate
    @Column(name = "requested_date")
    private LocalDate requestedDate;

    // 狀態：text -> String
    @Column(name = "status")
    private String status;

    // 系統時間：timestamptz -> OffsetDateTime
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // 附件連結或文字：text -> String
    @Column(name = "photo")
    private String photo;

    // ---- getters & setters ----
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getStationId() { return stationId; }
    public void setStationId(UUID stationId) { this.stationId = stationId; }

    public UUID getScheduleId() { return scheduleId; }
    public void setScheduleId(UUID scheduleId) { this.scheduleId = scheduleId; }

    public UUID getFurnitureId() { return furnitureId; }
    public void setFurnitureId(UUID furnitureId) { this.furnitureId = furnitureId; }

    public LocalDate getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDate requestedDate) { this.requestedDate = requestedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
}
