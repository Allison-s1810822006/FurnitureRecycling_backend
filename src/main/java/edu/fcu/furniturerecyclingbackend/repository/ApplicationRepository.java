package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.Application; // Application Entity
import edu.fcu.furniturerecyclingbackend.model.Station;     // Station Entity（如有關聯查詢可用）
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * ApplicationRepository
 * 提供對 applications 資料表的 CRUD 操作。
 * 型別參數：Application（Entity）、UUID（主鍵型別）。
 * 可擴充自訂查詢方法。
 */
public interface ApplicationRepository extends JpaRepository<Application, UUID> { }
