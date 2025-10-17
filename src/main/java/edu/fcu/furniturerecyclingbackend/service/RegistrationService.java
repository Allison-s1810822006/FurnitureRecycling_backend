package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.model.App_Users;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import edu.fcu.furniturerecyclingbackend.dto.RegistrationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    @Autowired
    private AppUsersRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public boolean registerUser(RegistrationDTO registrationDTO) {
        // 檢查用戶是否已經註冊
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            return false;  // 郵箱已註冊
        }

        // 加密密碼
        String encodedPassword = passwordEncoder.encode(registrationDTO.getPassword());

        // 創建新用戶實體
        App_Users newUser = new App_Users(
                registrationDTO.getFullName(),
                registrationDTO.getEmail(),
                encodedPassword
        );

        // 保存到資料庫
        userRepository.save(newUser);
        return true;  // 註冊成功
    }
}

