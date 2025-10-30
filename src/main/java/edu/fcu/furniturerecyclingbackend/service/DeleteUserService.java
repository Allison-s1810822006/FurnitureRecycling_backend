package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DeleteUserService {

    @Autowired
    private AppUsersRepository userRepository;

    public boolean deleteUserByEmail(String email) {
        // 根據 email 查找用戶
        Optional<AppUsers> userOptional = userRepository.findByEmail(email);

        // 如果用戶不存在，返回 false
        if (userOptional.isEmpty()) {
            return false;
        }

        // 如果用戶存在，刪除該用戶
        userRepository.delete(userOptional.get());

        // 返回 true 表示成功刪除
        return true;
    }

    public boolean deleteUserById(UUID userId) {
        // 根據 userId 查找用戶
        Optional<AppUsers> userOptional = userRepository.findById(userId);

        // 如果用戶不存在，返回 false
        if (userOptional.isEmpty()) {
            return false;
        }

        // 如果用戶存在，刪除該用戶
        userRepository.delete(userOptional.get());

        // 返回 true 表示成功刪除
        return true;
    }
}
