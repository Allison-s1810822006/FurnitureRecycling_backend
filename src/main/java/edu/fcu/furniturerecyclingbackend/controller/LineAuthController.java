package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.service.LineAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/auth/line")
@RequiredArgsConstructor
public class LineAuthController {
    private final LineAuthService lineAuthService;

    @GetMapping("/login")
    public void login(HttpServletResponse resp, HttpSession session) throws IOException {
        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        session.setAttribute("LINE_STATE", state);
        session.setAttribute("LINE_NONCE", nonce);
        resp.sendRedirect(lineAuthService.buildAuthorizeUrl(state, nonce));
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code, @RequestParam String state,
                         HttpSession session, HttpServletResponse resp) throws IOException {
        if (!state.equals(session.getAttribute("LINE_STATE"))) {
            resp.sendError(400, "Invalid state"); return;
        }
        var token = lineAuthService.exchangeCodeForToken(code);
        System.out.println("LINE id_token: " + token.idToken()); // log id_token
        try {
            var profile = lineAuthService.verifyIdTokenAndExtractProfile(
                    token.idToken(), (String) session.getAttribute("LINE_NONCE"));
            String jwt = lineAuthService.bindOrLogin(profile); // 這裡回你家的 JWT
            resp.setContentType("text/html; charset=UTF-8");
            resp.getWriter().write("<html><head><title>登入成功</title></head><body><h2>登入成功！</h2><p>JWT: " + jwt + "</p></body></html>");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "verify id_token failed: " + e.getMessage());
        }
    }
}
