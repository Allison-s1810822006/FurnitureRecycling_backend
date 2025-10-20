package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.dto.ApplicationRequestDto;
import edu.fcu.furniturerecyclingbackend.model.Application;
import edu.fcu.furniturerecyclingbackend.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // Create
    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody ApplicationRequestDto dto) {
        Application newApp = applicationService.createApplication(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newApp);
    }

    // Read - list
    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.findAll());
    }

    // Read - by id
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable UUID id) {
        Application app = applicationService.findById(id);
        if (app == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(app);
    }

    // Update (部分欄位更新；用同一個 DTO，僅覆蓋有帶值者)
    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable UUID id,
                                                         @RequestBody ApplicationRequestDto dto) {
        try {
            Application saved = applicationService.update(id, dto);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException notFound) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable UUID id) {
        boolean existed = applicationService.delete(id);
        return existed ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
