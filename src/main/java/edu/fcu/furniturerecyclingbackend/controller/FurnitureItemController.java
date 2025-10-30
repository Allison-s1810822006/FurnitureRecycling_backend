package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.model.FurnitureItem;
import edu.fcu.furniturerecyclingbackend.repository.ApplicationItemRepository;
import edu.fcu.furniturerecyclingbackend.repository.FurnitureItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/furniture")
public class FurnitureItemController {

    @Autowired
    private FurnitureItemRepository furnitureItemRepository;

    @Autowired
    private ApplicationItemRepository applicationItemRepository;

    // 取得全部家具資料
    @GetMapping
    public java.util.List<edu.fcu.furniturerecyclingbackend.model.FurnitureItem> getAllFurnitureItems() {
        return furnitureItemRepository.findAll();
    }

    // 依 ID 查詢單筆家具
    @GetMapping("/{id}")
    public Optional<FurnitureItem> getFurnitureItemById(@PathVariable("id") Integer id) {
        return furnitureItemRepository.findById(id);
    }

    // 新增家具資料（item_id 由資料庫自動生成）
    @PostMapping
    public ResponseEntity<?> createFurnitureItem(@RequestBody FurnitureItem item) {
        FurnitureItem saved = furnitureItemRepository.save(item);
        return ResponseEntity.ok(saved);
    }

    // 更新家具資料
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFurnitureItem(@PathVariable("id") Integer id,
                                             @RequestBody FurnitureItem updated) {
        return furnitureItemRepository.findById(id).map(item -> {
            item.setItemName(updated.getItemName());
            item.setLengthM(updated.getLengthM());
            item.setWidthM(updated.getWidthM());
            item.setHeightM(updated.getHeightM());
            item.setType(updated.getType());
            return ResponseEntity.ok(furnitureItemRepository.save(item));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 刪除家具資料
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFurnitureItem(@PathVariable("id") Integer id) {
        if (!furnitureItemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        furnitureItemRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 查詢某站點某日期剩餘可收取家具數量
     * @param stationId 站點主鍵（DP001~DP005，String 型別）
     * @param date 日期（yyyy-MM-dd）
     */
    @GetMapping("/remaining")
    public ResponseEntity<Map<String, Object>> getRemainingFurnitureCount(@RequestParam String stationId, @RequestParam String date) {
        java.time.LocalDate localDate = java.time.LocalDate.parse(date);
        Integer alreadyCount = applicationItemRepository.sumQuantityByStationIdAndRequestedDate(stationId, localDate);
        if (alreadyCount == null) alreadyCount = 0;
        int remaining = Math.max(0, 5 - alreadyCount);
        return ResponseEntity.ok(Map.of("stationId", stationId, "date", date, "remainingCount", remaining));
    }
}
