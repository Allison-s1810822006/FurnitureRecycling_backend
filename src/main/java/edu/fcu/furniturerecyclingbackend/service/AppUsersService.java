package edu.fcu.furniturerecyclingbackend.service;
import edu.fcu.furniturerecyclingbackend.model.App_Users;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AppUsersService {

    private final AppUsersRepository appUsersRepository;

    @Autowired
    public AppUsersService(AppUsersRepository appUsersRepository) {
        this.appUsersRepository = appUsersRepository;
    }

    // 根據 userId 查找使用者
    public Optional<App_Users> getUserById(UUID userId) {
        return appUsersRepository.findById(userId);
    }

    // 根據 email 查找使用者
    public Optional<App_Users> getUserByEmail(String email) {
        return appUsersRepository.findByEmail(email);
    }

    // 創建新的使用者
    public App_Users createUser(App_Users appUsers) {
        return appUsersRepository.save(appUsers);
    }

    // 更新現有的使用者
    public App_Users updateUser(UUID userId, App_Users updatedUser) {
        if (appUsersRepository.existsById(userId)) {
            updatedUser.setUserId(userId);  // 設定 userId 以保證資料正確
            return appUsersRepository.save(updatedUser);
        }
        return null;  // 若沒有此使用者，則返回 null
    }

    // 刪除使用者
    public void deleteUser(UUID userId) {
        appUsersRepository.deleteById(userId);
    }
}
