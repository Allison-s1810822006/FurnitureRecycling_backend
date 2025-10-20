package edu.fcu.furniturerecyclingbackend.dto;

import edu.fcu.furniturerecyclingbackend.model.ApplicationStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 新增清運申請 DTO
 * 用於市民端提交「定點放置區＋清運日期」申請
 */
@Data
public class ApplicationRequestDto {

    @NotNull(message = "userId 為必填欄位")
    private UUID userId;               // 必填：申請人

    @NotNull(message = "stationId 為必填欄位")
    private UUID stationId;            // 必填：站點

    @NotNull(message = "scheduleId 為必填欄位")
    private UUID scheduleId;           // 必填：清運時間（對應班表）

    private String dropPointCode;      // 可選：五個定點代碼（若使用下拉選單提供）

    @NotNull(message = "requestedDate 為必填欄位")
    private LocalDate requestedDate;   // 必填：申請清運日期

    @Min(value = 0, message = "totalItems 不可為負數")
    private Integer totalItems;        // 可選（預設 0）

    @DecimalMin(value = "0.0", message = "totalVolumeM3 不可為負數")
    private BigDecimal totalVolumeM3;  // 可選（預設 0.000）

    private String suggestedVehicle;   // 可選（預設 FLATBED）
    private ApplicationStatus status;
}
