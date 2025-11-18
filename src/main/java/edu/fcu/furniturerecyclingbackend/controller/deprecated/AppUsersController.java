package edu.fcu.furniturerecyclingbackend.controller.deprecated;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.service.AppUsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.fcu.furniturerecyclingbackend.dto.SimpleLoginRequest;
import edu.fcu.furniturerecyclingbackend.dto.CurrentUserDto;
import edu.fcu.furniturerecyclingbackend.dto.UpdateUserRequest;

import java.util.Optional;
import java.util.UUID;

// Deprecated: use AppUsersApiController (/api/app-users) instead
// @Tag(name = "app-users-controller", description = "使用者資料 API")
// @RestController
// @RequestMapping("/api/users")
public class AppUsersController {

    private final AppUsersService appUsersService;

    public AppUsersController(AppUsersService appUsersService) {
        this.appUsersService = appUsersService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AppUsers> getUserById(@PathVariable UUID userId) {
        Optional<AppUsers> appUser = appUsersService.getUserById(userId);
        return appUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<AppUsers> getUserByEmail(@PathVariable String email) {
        Optional<AppUsers> appUser = appUsersService.getUserByEmail(email);
        return appUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<AppUsers> createUser(@RequestBody AppUsers appUsers) {
        AppUsers createdUser = appUsersService.createUser(appUsers);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<AppUsers> updateUser(@PathVariable UUID userId, @RequestBody AppUsers updatedUser) {
        AppUsers updated = appUsersService.updateUser(userId, updatedUser);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        try {
            appUsersService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/app-users/{userId}")
    public ResponseEntity<?> deleteUserByAppUsersPath(@PathVariable UUID userId) {
        try {
            appUsersService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/simple-login")
    public ResponseEntity<CurrentUserDto> simpleLogin(@RequestBody SimpleLoginRequest req) {
        AppUsers user = appUsersService.simpleLoginOrRegister(
                req.getFullName(),
                req.getEmail(),
                req.getPhone()
        );

        CurrentUserDto dto = new CurrentUserDto();
        dto.setUserId(user.getUserId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());

        return ResponseEntity.ok(dto);
    }

    @PutMapping(value = "/app-users/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUserFromLine(@PathVariable UUID userId, @RequestBody UpdateUserRequest req) {
        var opt = appUsersService.getUserById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        var user = opt.get();
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        var saved = appUsersService.updateUser(userId, user);
        return ResponseEntity.ok(saved);
    }
}

