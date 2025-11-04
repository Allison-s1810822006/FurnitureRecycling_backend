package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.ApplicationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * ApplicationItemRepository
 * 提供 ApplicationItem 資料表的 CRUD 操作
 */
@Repository
public interface ApplicationItemRepository extends JpaRepository<ApplicationItem, UUID> {
    /**
     * 根據 applicationId 查詢所有 ApplicationItem
     */
    List<ApplicationItem> findByApplication_ApplicationId(UUID applicationId);

    /**
     * 查詢指定站點與日期的家具申請總數
     */
    @Query("SELECT SUM(ai.quantity) FROM ApplicationItem ai WHERE ai.application.station.stationId = :stationId AND ai.application.requestedDate = :requestedDate")
    Integer sumQuantityByStationIdAndRequestedDate(@Param("stationId") String stationId, @Param("requestedDate") LocalDate requestedDate);
}
