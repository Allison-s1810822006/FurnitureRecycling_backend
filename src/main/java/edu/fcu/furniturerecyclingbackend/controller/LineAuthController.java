package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.dto.LineProfile;
import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import edu.fcu.furniturerecyclingbackend.service.LineAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LineAuthController
 * 處理 LINE OAuth2 登入流程的 API，包含登入與 callback。
 */
@RestController
// Deprecated mapping: use /api/auth/line (ApiLineAuthController) instead
@RequestMapping("/_deprecated/auth/line")
@RequiredArgsConstructor
public class LineAuthController {
    private static final Logger logger = LoggerFactory.getLogger(LineAuthController.class);

    private final LineAuthService lineAuthService;
    private final AppUsersRepository appUsersRepository;

    // ⚠️ 注意：前端 base URL 不要帶 /line-callback
    // Provide a sensible default so tests won't fail when the property is absent
    @Value("${frontend.base-url:/}")
    private String frontendBase; // 例: http://localhost:5180/FurnitureRecycling_Frontend

    // ===========================================================
    // LINE Login Callback
    // ===========================================================
    @GetMapping("/callback")
    public ResponseEntity<?> handleLineCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state) {

        try {
            LineProfile profile = lineAuthService.getProfileFromCode(code, state);

            // Use findOrCreateUser to either create a new user or update existing
            var result = lineAuthService.findOrCreateUser(
                    profile.getLineUserId(),
                    profile.getDisplayName(),
                    profile.getEmail(),
                    profile.getPictureUrl()
            );

            var user = result.user();
            boolean isMember = result.isMember();

            String redirectUrl;
            String base = (frontendBase == null || frontendBase.isBlank()) ? "/" : frontendBase;

            if (!isMember) {
                // 還不是會員 → 導到 register 頁（帶 userId）
                redirectUrl = base + "/register" + "?userId=" + encode(user.getUserId() == null ? "" : user.getUserId().toString());
            } else {
                // 已是會員 → 導到 apply（或首頁）
                redirectUrl = base + "/apply" + "?userId=" + encode(user.getUserId() == null ? "" : user.getUserId().toString());
            }

            return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();

        } catch (Exception e) {
            logger.error("Error in LINE callback", e);
            return ResponseEntity.badRequest().body("LINE callback failed: " + e.getMessage());
        }
    }

    private String encode(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }
}
