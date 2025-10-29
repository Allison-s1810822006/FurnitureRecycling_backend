package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.ApplicationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
