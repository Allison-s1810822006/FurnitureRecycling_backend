package edu.fcu.furniturerecyclingbackend.controller;

/**
 * ApplicationController 負責處理家具回收申請的 REST API 請求
 * 包含新增、查詢、更新、刪除申請等功能
 */

import edu.fcu.furniturerecyclingbackend.dto.ApplicationRequestDto;
import edu.fcu.furniturerecyclingbackend.dto.ApplicationResponseDto;
import edu.fcu.furniturerecyclingbackend.service.ApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController // 標註為 REST API Controller
@RequestMapping("/api/applications") // 所有 API 路徑前綴
@RequiredArgsConstructor // 自動產生建構子注入 applicationService
@Tag(name = "applications-controller", description = "申請單 API")
public class ApplicationController {

    // 注入申請服務層，負責業務邏輯
    private final ApplicationService applicationService;

    /**
     * 新增申請單
     * @param dto 申請資料（ApplicationRequestDto）
     * @return 新增後的申請資料（ApplicationResponseDto）
     */
    @PostMapping
    public ResponseEntity<ApplicationResponseDto> createApplication(@RequestBody ApplicationRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.createApplication(dto));
    }

    /**
     * 查詢所有申請單
     * @return 申請單列表
     */
    @GetMapping
    public ResponseEntity<List<ApplicationResponseDto>> getAllApplications() {
        return ResponseEntity.ok(applicationService.findAll());
    }

    /**
     * 查詢指定申請單
     * @param id 申請單主鍵 UUID
     * @return 申請單資料，若不存在則回傳 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDto> getApplicationById(@PathVariable UUID id) {
        ApplicationResponseDto dto = applicationService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    /**
     * 更新申請單
     * @param id 申請單主鍵 UUID
     * @param dto 更新資料
     * @return 更新後的申請資料
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponseDto> updateApplication(
            @PathVariable UUID id, @RequestBody ApplicationRequestDto dto) {
        return ResponseEntity.ok(applicationService.update(id, dto));
    }

    /**
     * 刪除申請單
     * @param id 申請單主鍵 UUID
     * @return 刪除成功回傳 204，失敗回傳 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable UUID id) {
        boolean deleted = applicationService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
