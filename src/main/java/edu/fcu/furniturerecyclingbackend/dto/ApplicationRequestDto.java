package edu.fcu.furniturerecyclingbackend.dto;

import edu.fcu.furniturerecyclingbackend.model.ApplicationStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    private String stationId;            // 必填：站點（DP001~DP005）

    @NotNull(message = "scheduleId 為必填欄位")
    private UUID scheduleId;           // 必填：清運時間（對應班表）

    @NotNull(message = "requestedDate 為必填欄位")
    private LocalDate requestedDate;   // 必填：申請清運日期

    @Min(value = 0, message = "totalItems 不可為負數")
    private Integer totalItems;        // 可選（預設 0）

    @DecimalMin(value = "0.0", message = "totalVolumeM3 不可為負數")
    private BigDecimal totalVolumeM3;  // 可選（預設 0.000）

    private String suggestedVehicle;   // 可選（預設 FLATBED）
    private ApplicationStatus status;

    /**
     * 家具申請項目列表
     * 每個項目包含：主類型、細分選項、數量、照片網址
     */
    @NotNull(message = "items 為必填欄位")
    private List<FurnitureItemDto> items;

    /**
     * 家具項目 DTO
     * 對應 application_items 與 furniture 資料表
     * furniture_item_id: 對應 furniture.item_id
     * item_name: 名稱
     * main_type: 大類
     * sub_type: 子類
     * quantity: 數量
     * length_m, width_m, height_m: 尺寸
     * variant_code: 版本/型號
     * item_count: 計數欄位
     * photos: 照片網址清單
     */
    @Data
    public static class FurnitureItemDto {
        @NotNull(message = "furnitureItemId 為必填欄位")
        private Integer furnitureItemId;
        @NotNull(message = "itemName 為必填欄位")
        private String itemName;
        @Min(value = 1, message = "quantity 必須大於 0")
        private Integer quantity;
        @NotNull(message = "photos 為必填欄位")
        private List<String> photos;
        private Double lengthM;
        private Double widthM;
        private Double heightM;
        @NotNull(message = "type 為必填欄位")
        private String type;
    }
}
