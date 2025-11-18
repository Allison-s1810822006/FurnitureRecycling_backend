package edu.fcu.furniturerecyclingbackend.controller.deprecated;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.model.UpdateUserRequest;
import edu.fcu.furniturerecyclingbackend.service.UpdateUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

// Deprecated: managed via AppUsersApiController (/api/app-users)
// @RestController
public class UpdateUserController {

    private UpdateUserService updateUserService;

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest updateRequest,
                                        @RequestHeader(value = "X-User-Email", required = false) String email) {
        if (email == null) {
            return ResponseEntity.status(401).body("Missing email header");
        }
        AppUsers updatedUser = updateUserService.updateUser(email, updateRequest);
        if (updatedUser == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        return ResponseEntity.ok(updatedUser);
    }
}

