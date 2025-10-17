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
        // 這裡是你的 JWT 解析邏輯，假設你用的是 JJWT 或其他庫來處理
        try {
            // 這只是示例，根據你實際的 JWT 庫來解析 token
            // return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
            // 在這裡應該返回 email (subject)
            return "user@example.com";  // 這個是模擬返回的 email
        } catch (Exception e) {
            // 如果解析出錯，可以返回 null 或者抛出相應的異常
            return null;
        }
        //裡的實現取決於你如何設計 JWT 和使用的庫
    }
}

