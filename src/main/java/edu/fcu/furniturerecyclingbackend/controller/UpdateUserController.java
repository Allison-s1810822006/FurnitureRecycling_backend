package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.model.UpdateUserRequest;
import edu.fcu.furniturerecyclingbackend.service.UpdateUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UpdateUserController {

    @Autowired
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