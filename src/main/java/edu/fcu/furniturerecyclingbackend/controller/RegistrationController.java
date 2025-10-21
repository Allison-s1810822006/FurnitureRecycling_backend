package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.dto.RegistrationDTO;
import edu.fcu.furniturerecyclingbackend.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationDTO registrationDTO) {
        String result = registrationService.registerUser(registrationDTO);
        switch (result) {
            case "OK":
                return ResponseEntity.ok("註冊成功");
            case "INVALID_EMAIL":
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("無效的電子郵件格式");
            case "EMAIL_EXISTS":
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("該郵箱已經註冊");
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("註冊失敗");
        }
    }
}
