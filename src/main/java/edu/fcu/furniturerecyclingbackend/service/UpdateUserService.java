package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.model.UpdateUserRequest;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateUserService {

    @Autowired
    private AppUsersRepository userRepository;

    public AppUsers updateUser(String email, UpdateUserRequest updateRequest) {
        // 通过 email 查找用户
        Optional<AppUsers> optionalUser = userRepository.findByEmail(email);

        // 如果用户不存在，返回 null
        if (optionalUser.isEmpty()) {
            return null;
        }

        // 获取实际的 AppUsers 对象
        AppUsers user = optionalUser.get();

        // 更新用户信息
        user.setFullName(updateRequest.getFullName());
        user.setPhone(updateRequest.getPhone());
        user.setAddress(updateRequest.getAddress());

        // 保存更新后的用户并返回
        return userRepository.save(user);
    }
}
