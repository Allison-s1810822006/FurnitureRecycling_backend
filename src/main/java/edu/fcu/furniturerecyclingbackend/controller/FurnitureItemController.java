package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.model.FurnitureItem;
import edu.fcu.furniturerecyclingbackend.repository.FurnitureItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    // 新增家具資料
    @PostMapping
    public FurnitureItem createFurnitureItem(@RequestBody FurnitureItem item) {
        item.setItemId(UUID.randomUUID());
        return furnitureItemRepository.save(item);
    }

    // 更新家具資料
    @PutMapping("/{id}")
    public FurnitureItem updateFurnitureItem(@PathVariable("id") UUID id,
                                             @RequestBody FurnitureItem updated) {
        return furnitureItemRepository.findById(id).map(item -> {
            item.setItemName(updated.getItemName());
            item.setLengthM(updated.getLengthM());
            item.setWidthM(updated.getWidthM());
            item.setHeightM(updated.getHeightM());
            item.setType(updated.getType());
            item.setQuantity(updated.getQuantity());
            item.setCategory(updated.getCategory());
            item.setVariantCode(updated.getVariantCode());
            item.setPhotoUrl(updated.getPhotoUrl());
            return furnitureItemRepository.save(item);
        }).orElseThrow(() -> new RuntimeException("Furniture item not found"));
    }

    // 刪除家具資料
    @DeleteMapping("/{id}")
    public void deleteFurnitureItem(@PathVariable("id") UUID id) {
        furnitureItemRepository.deleteById(id);
    }
}
