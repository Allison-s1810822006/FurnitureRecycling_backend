package edu.fcu.furniturerecyclingbackend.dto;

import edu.fcu.furniturerecyclingbackend.model.ApplicationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class ApplicationResponseDto {
    private UUID applicationId;
    private UUID userId;
    private String stationId;     // 從關聯取出 id，型別改為 String（DP001~DP005）
    private String stationAddress; // 新增：站點地址，從 Station.address 取得，便利前端
    private UUID scheduleId;    // 從關聯取出 id
    private LocalDate requestedDate;
    private Integer totalItems;
    private BigDecimal totalVolumeM3;
    private String suggestedVehicle;
    private ApplicationStatus status;
    // 表示前端是否可以進入編輯畫面（true = 可編輯，false = 已受理/鎖定）
    private boolean editable;
    private Instant createdAt;
    private Instant updatedAt;
    /** 家具申請項目列表，對應 application_items 表 */
    private List<ApplicationItemDto> items;
}
