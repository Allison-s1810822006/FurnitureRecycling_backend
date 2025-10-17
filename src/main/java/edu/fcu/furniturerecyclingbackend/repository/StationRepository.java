package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface StationRepository extends JpaRepository<Station, UUID> {
    // 可以在這裡加入一些自定義查詢方法
}

