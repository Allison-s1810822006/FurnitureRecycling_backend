package edu.fcu.furniturerecyclingbackend.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * ApplicationItemDto
 * 用於前後端資料傳輸，封裝 ApplicationItem 相關欄位
 */
@Data
public class ApplicationItemDto {
    /** ApplicationItem 主鍵 UUID */
    private UUID itemId;
    /** 對應申請單 ID */
    private UUID applicationId;
    /** 家具類型 ID */
    private Integer furnitureItemId;
    /** 家具細分名稱 */
    private String itemName;
    /** 家具數量 */
    private Integer quantity;
    /** 家具照片（多張 URL），直接存成 JSON 字串在 photos 欄位 */
    private List<String> photos;
    /** 建立時間 */
    private Instant createdAt;
    /** 更新時間 */
    private Instant updatedAt;
}
