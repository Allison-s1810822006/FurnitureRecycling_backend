package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.FurnitureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FurnitureItemRepository extends JpaRepository<FurnitureItem, UUID> {
}
