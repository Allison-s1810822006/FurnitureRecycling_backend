package edu.fcu.furniturerecyclingbackend.controller.deprecated;

import edu.fcu.furniturerecyclingbackend.service.DeleteUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

// Deprecated: managed via AppUsersApiController (/api/app-users)
// @RestController
public class DeleteUserController {

    public DeleteUserService deleteUserService;

    @DeleteMapping("/users/delete/email/{email}")
    public ResponseEntity<String> deleteUserByEmail(@PathVariable String email) {
        boolean isDeleted = deleteUserService.deleteUserByEmail(email);

        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

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

