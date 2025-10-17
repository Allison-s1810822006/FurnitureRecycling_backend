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
        boolean success = registrationService.registerUser(registrationDTO);
        if (success) {
            return ResponseEntity.ok("註冊成功");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("該郵箱已經註冊");
        }
    }
}

