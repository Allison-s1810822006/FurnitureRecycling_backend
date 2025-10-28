package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.model.FurnitureItem;
import edu.fcu.furniturerecyclingbackend.repository.FurnitureItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/furniture")
public class FurnitureItemController {

    @Autowired
    private FurnitureItemRepository furnitureItemRepository;

    // 取得全部家具資料
    @GetMapping
    public List<FurnitureItem> getAllFurnitureItems() {
        return furnitureItemRepository.findAll();
    }

    // 依 ID 查詢單筆家具
    @GetMapping("/{id}")
    public Optional<FurnitureItem> getFurnitureItemById(@PathVariable("id") UUID id) {
        return furnitureItemRepository.findById(id);
    }

    // 新增家具資料（包含細分選項、數量、照片網址）
    // 前端需傳 mainType、subType、itemCount、photoUrls
    @PostMapping
    public ResponseEntity<?> createFurnitureItem(@RequestBody FurnitureItem item) {
        item.setItemId(UUID.randomUUID());
        // 根據 subType 自動推導 mainType
        if (item.getSubType() != null) {
            item.setMainType(item.getSubType().getMainType());
        }
        // 驗證照片數量是否與 itemCount 相符
        if (item.getPhotoUrls() == null || item.getPhotoUrls().size() != item.getItemCount()) {
            return ResponseEntity.badRequest().body(Map.of("error", "照片數量需與家具數量相符"));
        }
        // 驗證每張照片格式必須為 jpg 或 png
        for (String url : item.getPhotoUrls()) {
            if (!url.matches("(?i).+\\.(jpg|jpeg|png)$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "照片格式僅支援 jpg 或 png"));
            }
        }
        FurnitureItem saved = furnitureItemRepository.save(item);
        return ResponseEntity.ok(saved);
    }

    // 更新家具資料
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFurnitureItem(@PathVariable("id") UUID id,
                                             @RequestBody FurnitureItem updated) {
        return furnitureItemRepository.findById(id).map(item -> {
            item.setItemName(updated.getItemName());
            item.setLengthM(updated.getLengthM());
            item.setWidthM(updated.getWidthM());
            item.setHeightM(updated.getHeightM());
            item.setType(updated.getType());
            item.setMainType(updated.getMainType());
            item.setSubType(updated.getSubType());
            item.setItemCount(updated.getItemCount());
            item.setPhotoUrls(updated.getPhotoUrls());
            return ResponseEntity.ok(furnitureItemRepository.save(item));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 刪除家具資料
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFurnitureItem(@PathVariable("id") UUID id) {
        if (!furnitureItemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        furnitureItemRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // 取得所有家具主類型及細分選項
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getFurnitureTypes() {
        List<Map<String, String>> types = new ArrayList<>();
        for (FurnitureItem.SubType subType : FurnitureItem.SubType.values()) {
            types.add(Map.of(
                "mainType", subType.getMainType().name(),
                "mainTypeLabel", subType.getMainType().getDisplayName(),
                "subType", subType.name(),
                "subTypeLabel", subType.getDisplayName()
            ));
        }
        return ResponseEntity.ok(types);
    }

    /**
     * 查詢某站點某日期剩餘可收取家具數量
     */
    @GetMapping("/remaining")
    public ResponseEntity<Map<String, Object>> getRemainingFurnitureCount(@RequestParam UUID stationId, @RequestParam String date) {
        java.time.LocalDate localDate = java.time.LocalDate.parse(date);
        int alreadyCount = furnitureItemRepository.countByApplication_Station_StationIdAndApplication_RequestedDate(stationId, localDate);
        int remaining = Math.max(0, 5 - alreadyCount);
        return ResponseEntity.ok(Map.of("stationId", stationId, "date", date, "remainingCount", remaining));
    }
}
