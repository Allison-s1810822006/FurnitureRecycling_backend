package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.service.AppUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.fcu.furniturerecyclingbackend.dto.RegistrationDTO;

import java.util.Optional;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "app-users-controller", description = "使用者資料 API")
@RestController
@RequestMapping("/api/users")
public class AppUsersController {

    private final AppUsersService appUsersService;

    @Autowired
    public AppUsersController(AppUsersService appUsersService) {
        this.appUsersService = appUsersService;
    }

    // 根據 userId 查找使用者
    @GetMapping("/{userId}")
    public ResponseEntity<AppUsers> getUserById(@PathVariable UUID userId) {
        Optional<AppUsers> appUser = appUsersService.getUserById(userId);
        return appUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 根據 email 查找使用者
    @GetMapping("/email/{email}")
    public ResponseEntity<AppUsers> getUserByEmail(@PathVariable String email) {
        Optional<AppUsers> appUser = appUsersService.getUserByEmail(email);
        return appUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 創建新使用者
    @PostMapping
    public ResponseEntity<AppUsers> createUser(@RequestBody AppUsers appUsers) {
        AppUsers createdUser = appUsersService.createUser(appUsers);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // 更新現有使用者
    @PutMapping("/{userId}")
    public ResponseEntity<AppUsers> updateUser(@PathVariable UUID userId, @RequestBody AppUsers updatedUser) {
        AppUsers updated = appUsersService.updateUser(userId, updatedUser);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // 刪除使用者
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        appUsersService.deleteUser(userId);
        return ResponseEntity.noContent().build();  // 返回 204 No Content
    }

    /**
     * LINE快速註冊 API
     * 前端補齊資料後呼叫，建立新會員並登入。
     * @param registrationDTO 前端送來的註冊資料（含 LINE userId、displayName、email 等）
     * @param session 使用者 session
     * @return 新會員資料
     */
    @PostMapping("/line-register")
    public ResponseEntity<AppUsers> registerWithLine(@RequestBody RegistrationDTO registrationDTO, jakarta.servlet.http.HttpSession session) {
        // 建立新會員
        AppUsers createdUser = appUsersService.createUserFromLine(registrationDTO);
        // 建立登入狀態（可依需求產生 JWT 或 session）
        session.setAttribute("USER_ID", createdUser.getUserId().toString()); // 存成字串，避免型別問題
        // 根據前端按鈕導向（可由前端決定跳轉頁面）
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
