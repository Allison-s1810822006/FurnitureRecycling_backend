package edu.fcu.furniturerecyclingbackend.model;

import lombok.Getter;
import lombok.Setter;

/**
 * UpdateUserRequest
 * 用戶資料更新請求物件，只允許更新 fullName、phone。
 * 不包含密碼與地址欄位。
 */
@Setter
@Getter
public class UpdateUserRequest {
    // Getter/Setter
    private String fullName;   // 用戶姓名
    private String phone;      // 用戶電話

}
