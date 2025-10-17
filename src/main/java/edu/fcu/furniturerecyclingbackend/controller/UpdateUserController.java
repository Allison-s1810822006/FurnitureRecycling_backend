package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.model.App_Users;
import edu.fcu.furniturerecyclingbackend.model.UpdateUserRequest;
import edu.fcu.furniturerecyclingbackend.service.UpdateUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UpdateUserController {

    @Autowired
    private UpdateUserService updateUserService;

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest updateRequest, @RequestHeader("Authorization") String token) {
        // 假設你有一個方法從 JWT 中提取用戶的 email
        String email = extractEmailFromToken(token);

        App_Users updatedUser = updateUserService.updateUser(email, updateRequest);
        if (updatedUser == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        return ResponseEntity.ok(updatedUser);  // 返回更新後的用戶資料
    }

    private String extractEmailFromToken(String token) {
        // 解析 token 並返回用戶的 email
        // 這裡的實現取決於你如何設計 JWT 和使用的庫
    }
}

