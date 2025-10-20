package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.dto.ApplicationRequestDto;
import edu.fcu.furniturerecyclingbackend.dto.ApplicationResponseDto;
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

    // 建立

    @PostMapping
    public ResponseEntity<ApplicationResponseDto> createApplication(@RequestBody ApplicationRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.createApplication(dto));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponseDto>> getAllApplications() {
        return ResponseEntity.ok(applicationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDto> getApplicationById(@PathVariable UUID id) {
        ApplicationResponseDto dto = applicationService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponseDto> updateApplication(
            @PathVariable UUID id, @RequestBody ApplicationRequestDto dto) {
        return ResponseEntity.ok(applicationService.update(id, dto));
    }

    // 刪除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable UUID id) {
        boolean deleted = applicationService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
