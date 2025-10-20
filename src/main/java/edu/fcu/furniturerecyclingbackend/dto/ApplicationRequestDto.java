package edu.fcu.furniturerecyclingbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ApplicationRequestDto {
    private UUID userId;               // 必填：申請人
    private UUID stationId;            // 可選
    private UUID scheduleId;           // 可選
    private String dropPointCode;      // 可選
    private LocalDate requestedDate;   // 建議填
    private Integer totalItems;        // 可選（預設 0）
    private BigDecimal totalVolumeM3;  // 可選（預設 0.000）
    private String suggestedVehicle;   // 可選（預設 FLATBED）
    private String status;             // 可選（預設 SUBMITTED）
}

