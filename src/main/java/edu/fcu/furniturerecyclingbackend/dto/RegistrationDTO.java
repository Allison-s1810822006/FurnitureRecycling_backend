package edu.fcu.furniturerecyclingbackend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * RegistrationDTO
 * 用於接收使用者註冊資料的資料傳輸物件。
 * 包含姓名、email、密碼欄位。
 */
@Setter
@Getter
public class RegistrationDTO {
    /** 使用者姓名，對應 app_users.full_name */
    private String fullName;
    /** 使用者 email，對應 app_users.email */
    private String email;
    /** 使用者密碼（如不需密碼可移除） */
    private String password;
}
