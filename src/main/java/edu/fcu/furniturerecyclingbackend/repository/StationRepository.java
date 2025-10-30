package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import java.lang.String;

public interface StationRepository extends JpaRepository<Station, String> {
}
