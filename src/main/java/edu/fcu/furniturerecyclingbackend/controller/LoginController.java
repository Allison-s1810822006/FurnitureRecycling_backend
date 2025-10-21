package edu.fcu.furniturerecyclingbackend.controller;
import edu.fcu.furniturerecyclingbackend.model.LoginRequest;
import edu.fcu.furniturerecyclingbackend.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        boolean isAuthenticated = loginService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

        if (!isAuthenticated) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // 登入成功，回傳簡單訊息（系統不再使用 JWT）
        return ResponseEntity.ok().body("Login successful");
    }
}
