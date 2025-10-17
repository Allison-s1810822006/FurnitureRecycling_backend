package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.service.DeleteUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class DeleteUserController {

    @Autowired
    private DeleteUserService deleteUserService;

    // 根據 email 刪除用戶
    @DeleteMapping("/users/delete/email/{email}")
    public ResponseEntity<String> deleteUserByEmail(@PathVariable String email) {
        boolean isDeleted = deleteUserService.deleteUserByEmail(email);

        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    // 根據 userId 刪除用戶
    @DeleteMapping("/users/delete/{userId}")
    public ResponseEntity<String> deleteUserById(@PathVariable UUID userId) {
        boolean isDeleted = deleteUserService.deleteUserById(userId);

        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }
}

