package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.service.AppUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

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
}
