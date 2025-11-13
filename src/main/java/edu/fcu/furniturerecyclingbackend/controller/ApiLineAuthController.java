package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.service.LineAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

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

    /**
     * 取得 LINE 登入網址 API
     * 前端呼叫此 API 取得 LINE 授權頁面網址，並引導使用者前往登入
     * 會自動產生 state/nonce 並存入 session，防止 CSRF 與重放攻擊
     * @param session 當前使用者的 session
     * @return 包含 url 欄位的 JSON 物件
     */
    @GetMapping("/login-url")
    public Map<String, String> getLineLoginUrl(HttpSession session) {
        // 產生隨機 state/nonce，並存入 session
        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        session.setAttribute("LINE_STATE", state);
        session.setAttribute("LINE_NONCE", nonce);
        // 組合 LINE 授權頁面網址
        String url = lineAuthService.buildAuthorizeUrl(state, nonce);
        // 回傳 JSON 格式 { "url": "..." }
        return Collections.singletonMap("url", url);
    }

    /**
     * 查詢目前登入狀態與 user 資料 API
     * 前端可呼叫此 API 取得目前 session 是否已登入，以及對應的 user 資料
     * @param session 當前使用者的 session
     * @return { isAuthenticated: true/false, user: user物件或空字串 }
     */
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(HttpSession session) {
        // 從 session 取得 LINE_USER_ID，判斷是否已登入
        String lineUserId = (String) session.getAttribute("LINE_USER_ID");
        if (lineUserId == null) {
            // 尚未登入，回傳 isAuthenticated: false
            return Map.of("isAuthenticated", false, "user", "");
        }
        // 已登入，查詢 user 資料
        var user = lineAuthService.getUserByLineUserId(lineUserId);
        boolean isAuthenticated = user != null;
        // 回傳 isAuthenticated 與 user 物件
        return Map.of(
                "isAuthenticated", isAuthenticated,
                "user", user
        );
    }
}
