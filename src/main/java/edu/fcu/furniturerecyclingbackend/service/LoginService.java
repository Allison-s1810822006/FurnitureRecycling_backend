package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.model.App_Users;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private AppUsersRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 驗證用戶名和密碼，並返回是否登入成功
    public boolean authenticateUser(String email, String password) {
        Optional<App_Users> userOptional = userRepository.findByEmail(email);

        // 如果用戶不存在，返回 false
        if (!userOptional.isPresent()) {
            return false;
        }

        // 獲取實際的 App_Users 實例
        App_Users user = userOptional.get();

        // 使用 BCryptPasswordEncoder 比對密碼
        return passwordEncoder.matches(password, user.getPasswordHash());
    }
}


