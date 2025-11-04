package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.model.UpdateUserRequest;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * UpdateUserService
 * 用戶資料更新服務，只允許更新姓名、電話。
 * 不處理密碼相關欄位。
 */
@Service
public class UpdateUserService {

    @Autowired
    private AppUsersRepository userRepository;

    /**
     * 根據 email 查找用戶並更新資料（不含密碼）
     * @param email 用戶 email
     * @param updateRequest 更新資料物件
     * @return 更新後的用戶資料，或 null
     */
    public AppUsers updateUser(String email, UpdateUserRequest updateRequest) {
        // 通过 email 查找用户
        Optional<AppUsers> optionalUser = userRepository.findByEmail(email);

        // 如果用户不存在，返回 null
        if (optionalUser.isEmpty()) {
            return null;
        }

        // 获取实际的 AppUsers 对象
        AppUsers user = optionalUser.get();

        // 更新用户信息（不處理密碼與地址）
        user.setFullName(updateRequest.getFullName());
        user.setPhone(updateRequest.getPhone());

        // 保存更新后的用户并返回
        return userRepository.save(user);
    }
}
