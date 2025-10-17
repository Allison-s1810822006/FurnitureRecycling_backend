package edu.fcu.furniturerecyclingbackend.controller;
import edu.fcu.furniturerecyclingbackend.model.LoginRequest;
import edu.fcu.furniturerecyclingbackend.service.LoginService;
import edu.fcu.furniturerecyclingbackend.service.JWTService;  // 用來生成 JWT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private JWTService jwtService;  // 用來生成 JWT

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        boolean isAuthenticated = loginService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

        if (!isAuthenticated) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // 登入成功，生成 JWT
        String token = jwtService.generateToken(loginRequest.getEmail());
        return ResponseEntity.ok().body("Bearer " + token);  // 返回 JWT token
    }
}

