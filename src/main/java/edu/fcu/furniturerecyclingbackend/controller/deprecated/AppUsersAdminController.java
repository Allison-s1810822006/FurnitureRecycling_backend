package edu.fcu.furniturerecyclingbackend.controller.deprecated;

import edu.fcu.furniturerecyclingbackend.service.AppUsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

// Deprecated admin controller: use AppUsersApiController (/api/app-users) or secure admin endpoints
public class AppUsersAdminController {

    private final AppUsersService appUsersService;

    public AppUsersAdminController(AppUsersService appUsersService) {
        this.appUsersService = appUsersService;
    }

    @DeleteMapping("/api/app-users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        try {
            appUsersService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

