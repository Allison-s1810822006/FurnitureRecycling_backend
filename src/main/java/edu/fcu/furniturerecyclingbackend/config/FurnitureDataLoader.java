package edu.fcu.furniturerecyclingbackend.config;

import edu.fcu.furniturerecyclingbackend.model.FurnitureItem;
import edu.fcu.furniturerecyclingbackend.repository.FurnitureItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FurnitureDataLoader {
    private final FurnitureItemRepository furnitureItemRepository;

    @PostConstruct
    public void initFurniture() {
        insertIfNotExists(1, "單人座沙發", 0.90, 0.85, 0.90, "沙發");
        insertIfNotExists(2, "雙人座沙發", 1.60, 0.85, 0.90, "沙發");
        insertIfNotExists(3, "三人座沙發", 2.00, 0.90, 0.90, "沙發");
        insertIfNotExists(4, "單人床架不含床墊", 0.90, 1.88, 0.45, "床架");
        insertIfNotExists(5, "雙人床架不含床墊", 1.50, 1.88, 0.45, "床架");
    }

    private void insertIfNotExists(int itemId, String itemName, double lengthM, double widthM, double heightM, String type) {
        if (!furnitureItemRepository.existsById(itemId)) {
            FurnitureItem item = new FurnitureItem();
            item.setItemId(itemId);
            item.setItemName(itemName);
            item.setLengthM(lengthM);
            item.setWidthM(widthM);
            item.setHeightM(heightM);
            item.setType(type);
            furnitureItemRepository.save(item);
        }
    }
}

