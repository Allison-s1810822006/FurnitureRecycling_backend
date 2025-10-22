package edu.fcu.furniturerecyclingbackend.dto;

import edu.fcu.furniturerecyclingbackend.model.ApplicationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ApplicationResponseDto {
    private UUID applicationId;
    private UUID userId;
    private UUID stationId;     // 從關聯取出 id
    private UUID scheduleId;    // 從關聯取出 id
    private String dropPointCode;
    private LocalDate requestedDate;
    private Integer totalItems;
    private BigDecimal totalVolumeM3;
    private String suggestedVehicle;
    private ApplicationStatus status;
    // 表示前端是否可以進入編輯畫面（true = 可編輯，false = 已受理/鎖定）
    private boolean editable;
    private Instant createdAt;
    private Instant updatedAt;
}
