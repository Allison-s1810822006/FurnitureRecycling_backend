package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.dto.ApplicationItemDto;
import edu.fcu.furniturerecyclingbackend.service.ApplicationItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ApplicationItemController
 * 提供 ApplicationItem 的 RESTful API 介面
 */
@RestController
@RequestMapping("/api/application-items")
@RequiredArgsConstructor
@Tag(name = "application-items-controller", description = "申請明細 API")
public class ApplicationItemController {
    private final ApplicationItemService applicationItemService;

    /** 新增 ApplicationItem */
    @PostMapping
    public ResponseEntity<ApplicationItemDto> create(@RequestBody ApplicationItemDto dto) {
        return ResponseEntity.ok(applicationItemService.create(dto));
    }

    /** 查詢所有 ApplicationItem */
    @GetMapping
    public ResponseEntity<List<ApplicationItemDto>> findAll() {
        return ResponseEntity.ok(applicationItemService.findAll());
    }

    /** 根據 itemId 查詢 ApplicationItem */
    @GetMapping("/{itemId}")
    public ResponseEntity<ApplicationItemDto> findById(@PathVariable UUID itemId) {
        Optional<ApplicationItemDto> result = applicationItemService.findById(itemId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 根據 applicationId 查詢所有 ApplicationItem */
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<ApplicationItemDto>> findByApplicationId(@PathVariable UUID applicationId) {
        return ResponseEntity.ok(applicationItemService.findByApplicationId(applicationId));
    }

    /** 更新 ApplicationItem */
    @PutMapping("/{itemId}")
    public ResponseEntity<ApplicationItemDto> update(@PathVariable UUID itemId, @RequestBody ApplicationItemDto dto) {
        Optional<ApplicationItemDto> result = applicationItemService.update(itemId, dto);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 刪除 ApplicationItem */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable UUID itemId) {
        applicationItemService.delete(itemId);
        return ResponseEntity.noContent().build();
    }
}
