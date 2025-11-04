package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.model.UpdateUserRequest;
import edu.fcu.furniturerecyclingbackend.service.UpdateUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UpdateUserController
 * 處理 app_users 用戶資料的更新 API。
 * 只允許更新姓名、電話，不處理地址與密碼。
 */
@RestController
@RequestMapping("/user")
public class UpdateUserController {

    @Autowired
    private UpdateUserService updateUserService;

    /**
     * 用戶資料更新 API
     * @param updateRequest 用戶更新資料（不含密碼）
     * @param email 由 header 傳入的 email
     * @return 更新後的用戶資料，或錯誤訊息
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest updateRequest,
                                        @RequestHeader(value = "X-User-Email", required = false) String email) {
        // 檢查 email header
        if (email == null) {
            return ResponseEntity.status(401).body("Missing email header");
        }
        // 執行更新（只更新非密碼欄位）
        AppUsers updatedUser = updateUserService.updateUser(email, updateRequest);
        if (updatedUser == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        return ResponseEntity.ok(updatedUser);
    }

}