package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.dto.RegistrationDTO;
import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.service.AppUsersService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/app-users")
@RequiredArgsConstructor
public class AppUsersApiController {

    private final AppUsersService appUsersService;

    /**
     * Get user by id - used by frontend ProfilePage
     * GET /api/app-users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getAppUser(@PathVariable UUID userId) {
        var opt = appUsersService.getUserById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(opt.get()); // 直接回 AppUsers JSON，前端會 normalize
    }

    /**
     * Update existing user (do not change lineUserId).
     * PUT /api/app-users/{userId}
     */
    @PutMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUserFromRegister(@PathVariable UUID userId,
                                                    @RequestBody RegistrationDTO req,
                                                    HttpSession session) {
        var userOpt = appUsersService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        var user = userOpt.get();
        // update fields from RegistrationDTO
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getPhone() != null) user.setPhone(req.getPhone());

        user.setIsMember(true); // ⭐ 按「加入會員」後升級成正式會員
        user.setUpdatedAt(OffsetDateTime.now());

        var saved = appUsersService.updateUser(userId, user);
        // set session so this browser session recognized as logged-in
        if (session != null && saved != null && saved.getLineUserId() != null) {
            session.setAttribute("LINE_USER_ID", saved.getLineUserId());
        }
        return ResponseEntity.ok(saved);
    }

    /**
     * Delete user by id - physical delete
     * DELETE /api/app-users/{userId}
     *
     * Perform a downgrade + clear personally identifiable information so that the same LINE account
     * can re-register later. This keeps the DB row but removes identifying data and marks is_member=false.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        if (appUsersService.getUserById(userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        appUsersService.deleteUser(userId);  // 用上面那個軟刪方法
        return ResponseEntity.noContent().build();
    }

}
