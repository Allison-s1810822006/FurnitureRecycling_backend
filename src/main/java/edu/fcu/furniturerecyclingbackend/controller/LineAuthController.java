package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.service.LineAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LineAuthController
 * 處理 LINE OAuth2 登入流程的 API，包含登入與 callback。
 */
@RestController // 標註為 REST API Controller
@RequestMapping("/auth/line") // 所有 API 路徑前綴
@RequiredArgsConstructor // 自動產生建構子注入 lineAuthService
public class LineAuthController {
    // 注入 LINE 登入服務層
    private final LineAuthService lineAuthService;
    private static final Logger logger = LoggerFactory.getLogger(LineAuthController.class);

    /**
     * LINE 登入 API
     * 產生 state/nonce 並導向 LINE 授權頁面
     * @param resp 回應物件
     * @param session 使用者 session
     */
    @GetMapping("/login")
    public void login(@NonNull HttpServletResponse resp, @NonNull HttpSession session) throws IOException {
        // 產生隨機 state/nonce 並存入 session
        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        session.setAttribute("LINE_STATE", state);
        session.setAttribute("LINE_NONCE", nonce);
        // 導向 LINE 授權頁面
        resp.sendRedirect(lineAuthService.buildAuthorizeUrl(state, nonce));
    }

    /**
     * LINE callback API
     * 處理 LINE 授權回傳，判斷新舊會員，回傳 profile 或 JWT。
     * @param code LINE 授權回傳 code
     * @param state LINE 授權回傳 state
     * @param session 使用者 session
     * @param resp 回應物件
     */
    @GetMapping("/callback")
    public void callback(@RequestParam String code, @RequestParam String state,
                         @NonNull HttpSession session, @NonNull HttpServletResponse resp) throws IOException {
        // 驗證 state 防止 CSRF
        if (!state.equals(session.getAttribute("LINE_STATE"))) {
            resp.sendError(400, "Invalid state"); return;
        }
        // 交換 code 取得 token
        var token = lineAuthService.exchangeCodeForToken(code);
        try {
            // 驗證 id_token 並取得 LINE 使用者資料
            var profile = lineAuthService.verifyIdTokenAndExtractProfile(
                    token.idToken(), (String) session.getAttribute("LINE_NONCE"));
            // 判斷是否已綁定會員
            boolean isBound = lineAuthService.isLineUserBound(profile.getLineUserId());
            if (isBound) {
                // 已綁定：建立登入狀態，回傳 JWT 並導向 application 頁
                String jwt = lineAuthService.loginWithLineUser(profile.getLineUserId());
                session.setAttribute("JWT", jwt); // 建立 session 狀態
                resp.sendRedirect("/application"); // 導向 application 頁
            } else {
                // 未綁定：回傳 LINE profile 給前端，進入基本資料填寫頁
                session.setAttribute("LINE_PROFILE", profile); // 暫存 profile
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(profile));
            }
        } catch (Exception e) {
            logger.error("verify id_token failed", e);
            resp.sendError(500, "verify id_token failed: " + e.getMessage());
        }
    }
}
