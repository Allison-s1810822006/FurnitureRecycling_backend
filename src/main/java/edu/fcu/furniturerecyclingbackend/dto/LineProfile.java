package edu.fcu.furniturerecyclingbackend.dto;

import lombok.Builder;
import lombok.Value;

/**
 * LineProfile
 * 封裝 LINE OAuth2 登入後取得的使用者資料。
 * 用於 app_users 資料表的 LINE 欄位對應。
 */
@Value // Lombok: 產生不可變物件（getter、equals、hashCode、toString）
@Builder // Lombok: 產生建構器模式
public class LineProfile {
    /** LINE user id，對應 app_users.line_user_id */
    String lineUserId;
    /** LINE 顯示名稱，對應 app_users.line_display_name */
    String displayName;
    /** LINE 頭像網址，對應 app_users.line_picture_url */
    String pictureUrl;
    /** LINE email，對應 app_users.line_email */
    String email;
}
