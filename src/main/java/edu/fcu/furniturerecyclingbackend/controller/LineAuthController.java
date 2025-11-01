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
     * 驗證 state，交換 token，驗證 id_token 並取得 LINE 使用者資料
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
        System.out.println("LINE id_token: " + token.idToken()); // log id_token
        try {
            // 驗證 id_token 並取得 LINE 使用者資料
            var profile = lineAuthService.verifyIdTokenAndExtractProfile(
                    token.idToken(), (String) session.getAttribute("LINE_NONCE"));
            // 綁定或登入本地帳號，取得 JWT
            String jwt = lineAuthService.bindOrLogin(profile); // 這裡回你家的 JWT
            // 回傳登入成功頁面與 JWT
            resp.setContentType("text/html; charset=UTF-8");
            resp.getWriter().write("<html><head><title>登入成功</title></head><body><h2>登入成功！</h2><p>JWT: " + jwt + "</p></body></html>");
        } catch (Exception e) {
            // 驗證失敗，回傳錯誤
            logger.error("verify id_token failed", e);
            resp.sendError(500, "verify id_token failed: " + e.getMessage());
        }
    }
}
