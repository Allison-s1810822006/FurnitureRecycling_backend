package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.App_Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AppUsersRepository extends JpaRepository<App_Users, UUID> {

    // 自訂根據 email 查找使用者的方法
    Optional<App_Users> findByEmail(String email);
    boolean existsByEmail(String email);
}

