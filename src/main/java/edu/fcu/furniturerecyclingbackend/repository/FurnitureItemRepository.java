package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.FurnitureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FurnitureItemRepository extends JpaRepository<FurnitureItem, UUID> {
    /**
     * 查詢某站點某日期已申請的家具總數
     */
    int countByApplication_Station_StationIdAndApplication_RequestedDate(String stationId, java.time.LocalDate requestedDate);
}
