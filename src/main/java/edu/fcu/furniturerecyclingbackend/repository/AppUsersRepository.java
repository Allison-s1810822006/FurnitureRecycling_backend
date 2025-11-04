package edu.fcu.furniturerecyclingbackend.repository;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AppUsersRepository extends JpaRepository<AppUsers, UUID> {

    // 自訂根據 email 查找使用者的方法
    Optional<AppUsers> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * 根據 LINE userId 查找會員
     * @param lineUserId LINE userId
     * @return Optional<AppUsers>
     */
    Optional<AppUsers> findByLineUserId(String lineUserId);
}
