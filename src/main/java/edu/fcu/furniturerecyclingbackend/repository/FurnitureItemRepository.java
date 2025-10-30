package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.FurnitureItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FurnitureItemRepository extends JpaRepository<FurnitureItem, Integer> {
}
