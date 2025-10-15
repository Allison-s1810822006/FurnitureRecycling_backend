package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
}
