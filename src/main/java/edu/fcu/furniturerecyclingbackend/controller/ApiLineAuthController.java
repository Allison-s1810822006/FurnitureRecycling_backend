package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.service.LineAuthService;
import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.dto.LineUserResult;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供前端取得 LINE 登入網址與查詢目前登入狀態的 API Controller
 * 路徑前綴：/api/auth/line
 */
@RestController // 標註為 REST API Controller，回傳 JSON
@RequestMapping("/api/auth/line") // 所有 API 路徑前綴
@RequiredArgsConstructor // 自動產生建構子注入 lineAuthService
public class ApiLineAuthController {
    // 注入 LINE 登入服務層，負責組合 URL 與查詢 user
    private final LineAuthService lineAuthService;
    private static final Logger logger = LoggerFactory.getLogger(ApiLineAuthController.class);

    // frontend base URL (used for redirects)
    @Value("${frontend.base-url:http://localhost:5180/FurnitureRecycling_Frontend}")
    private String frontendBase;

    /**
     * 取得 LINE 登入網址 API (JSON) - 保留原方法
     */
    @GetMapping("/login-url")
    public Map<String, String> getLineLoginUrl(HttpSession session) {
        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        session.setAttribute("LINE_STATE", state);
        session.setAttribute("LINE_NONCE", nonce);
        String url = lineAuthService.buildAuthorizeUrl(state, nonce);
        return Collections.singletonMap("url", url);
    }

    /**
     * 前端直接導向的 login endpoint: 產生 state/nonce 並 302 redirect 到 LINE 授權頁
     * GET /api/auth/line/login
     */
    @GetMapping("/login")
    public void login(HttpSession session, jakarta.servlet.http.HttpServletResponse resp) throws IOException {
        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        session.setAttribute("LINE_STATE", state);
        session.setAttribute("LINE_NONCE", nonce);
        String url = lineAuthService.buildAuthorizeUrl(state, nonce);
        resp.sendRedirect(url);
    }

    /**
     * LINE callback endpoint (centralized).
     * GET /api/auth/line/callback
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession session) {

        try {
            if (error != null) {
                String url = frontendBase + "?error=LINE_AUTH_ERROR&message=" + URLEncoder.encode(error, StandardCharsets.UTF_8);
                return ResponseEntity.status(302).location(URI.create(url)).build();
            }

            // validate state
            String savedState = (String) session.getAttribute("LINE_STATE");
            String savedNonce = (String) session.getAttribute("LINE_NONCE");
            if (savedState == null || !savedState.equals(state)) {
                String url = frontendBase + "?error=INVALID_STATE&message=" + URLEncoder.encode("Invalid state", StandardCharsets.UTF_8);
                return ResponseEntity.status(302).location(URI.create(url)).build();
            }

            // Simplified flow: use getProfileFromCode then findOrCreateUser and redirect accordingly
            var profile = lineAuthService.getProfileFromCode(code, state);
            LineUserResult result = lineAuthService.findOrCreateUser(
                    profile.getLineUserId(),
                    profile.getDisplayName(),
                    profile.getEmail(),
                    profile.getPictureUrl()
            );

            AppUsers user = result.user();
            boolean isMember = result.isMember();

            // Mark this server-side session as logged-in for this LINE user
            if (user.getLineUserId() != null) {
                session.setAttribute("LINE_USER_ID", user.getLineUserId());
            }

            String redirectUrl = !isMember
                    ? frontendBase + "/register?userId=" + URLEncoder.encode(user.getUserId() == null ? "" : user.getUserId().toString(), StandardCharsets.UTF_8)
                    : frontendBase + "/apply?userId=" + URLEncoder.encode(user.getUserId() == null ? "" : user.getUserId().toString(), StandardCharsets.UTF_8);

            // clear temporary state attributes
            session.removeAttribute("LINE_STATE");
            session.removeAttribute("LINE_NONCE");

            return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();

        } catch (Exception e) {
            logger.error("Error in LINE callback (api)", e);
            String url = frontendBase + "?error=SERVER_ERROR&message=" + URLEncoder.encode(e.getMessage() == null ? "" : e.getMessage(), StandardCharsets.UTF_8);
            return ResponseEntity.status(302).location(URI.create(url)).build();
        }
    }

    /**
     * 查詢目前登入狀態與 user 資料 API
     */
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(HttpSession session) {
        String lineUserId = (String) session.getAttribute("LINE_USER_ID");
        if (lineUserId == null) {
            return Map.of("isAuthenticated", false, "user", "");
        }
        var userOpt = lineAuthService.getUserByLineUserId(lineUserId);
        if (userOpt.isPresent()) {
            return Map.of("isAuthenticated", true, "user", userOpt.get());
        } else {
            return Map.of("isAuthenticated", false, "user", "");
        }
    }
}
