package edu.fcu.furniturerecyclingbackend.config;

import edu.fcu.furniturerecyclingbackend.model.FurnitureItem;
import edu.fcu.furniturerecyclingbackend.repository.FurnitureItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 啟動時自動初始化家具型錄資料。
 * 若資料表已存在指定 item_id 則不重複插入。
 */
@Component
@RequiredArgsConstructor
public class FurnitureDataLoader {
    // 注入家具型錄的 Repository
    private final FurnitureItemRepository furnitureItemRepository;

    /**
     * 啟動時執行，插入五筆預設家具資料。
     * 若資料已存在則不重複插入。
     */
    @PostConstruct
    public void initFurniture() {
        // 單人座沙發
        insertIfNotExists(1, "單人座沙發", 0.90, 0.85, 0.90, "沙發");
        // 雙人座沙發
        insertIfNotExists(2, "雙人座沙發", 1.60, 0.85, 0.90, "沙發");
        // 三人座沙發
        insertIfNotExists(3, "三人座沙發", 2.00, 0.90, 0.90, "沙發");
        // 單人床架不含床墊
        insertIfNotExists(4, "單人床架不含床墊", 0.90, 1.88, 0.45, "床架");
        // 雙人床架不含床墊
        insertIfNotExists(5, "雙人床架不含床墊", 1.50, 1.88, 0.45, "床架");
    }

    /**
     * 若指定 item_id 不存在，則插入一筆家具資料。
     * @param itemId 家具型錄主鍵
     * @param itemName 家具名稱
     * @param lengthM 長度（公尺）
     * @param widthM 寬度（公尺）
     * @param heightM 高度（公尺）
     * @param type 家具類型
     */
    private void insertIfNotExists(int itemId, String itemName, double lengthM, double widthM, double heightM, String type) {
        // 檢查 item_id 是否已存在
        if (!furnitureItemRepository.existsById(itemId)) {
            // 建立家具實體
            FurnitureItem item = new FurnitureItem();
            item.setItemId(itemId);
            item.setItemName(itemName);
            item.setLengthM(lengthM);
            item.setWidthM(widthM);
            item.setHeightM(heightM);
            item.setType(type);
            // 寫入資料庫
            furnitureItemRepository.save(item);
        }
    }
}
