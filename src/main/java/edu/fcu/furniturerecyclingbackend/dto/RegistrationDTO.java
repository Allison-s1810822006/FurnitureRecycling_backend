package edu.fcu.furniturerecyclingbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RegistrationDTO
 * 用於 LINE快速註冊 API，封裝前端補齊的會員資料。
 * 包含 LINE userId、displayName、email 及其他必要欄位。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO {
    /** LINE userId，綁定用 */
    private String lineUserId;
    /** 顯示名稱 */
    private String displayName;
    /** Email */
    private String email;
    /** 頭像網址 */
    private String pictureUrl;
    /** 手機號碼，LINE 不一定提供，需前端補齊 */
    private String phone;
    /** 姓名，LINE 不一定提供，需前端補齊 */
    private String fullName;
    // 可依需求擴充其他欄位
}
