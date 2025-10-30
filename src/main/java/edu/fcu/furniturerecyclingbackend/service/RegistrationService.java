package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import edu.fcu.furniturerecyclingbackend.dto.RegistrationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    @Autowired
    private AppUsersRepository userRepository;

    // 回傳註冊結果狀態："OK" / "INVALID_EMAIL" / "EMAIL_EXISTS"
    public String registerUser(RegistrationDTO registrationDTO) {
        String email = registrationDTO.getEmail();

        // 檢查 email 格式
        if (email == null || !isValidEmail(email)) {
            return "INVALID_EMAIL";
        }

        // 檢查用戶是否已經註冊
        if (userRepository.existsByEmail(email)) {
            return "EMAIL_EXISTS";  // 郵箱已註冊
        }

        // 使用 jBCrypt (本地類別 shim) 加密密碼
        String salt = org.mindrot.jbcrypt.BCrypt.gensalt();
        String encodedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(registrationDTO.getPassword(), salt);

        // 創建新用戶實體
        AppUsers newUser = new AppUsers(
                registrationDTO.getFullName(),
                registrationDTO.getEmail(),
                encodedPassword
        );

        // 保存到資料庫
        userRepository.save(newUser);
        return "OK";  // 註冊成功
    }

    // 簡單的 email 格式驗證
    private boolean isValidEmail(String email) {
        // 使用簡潔且實用的正則：允許常見的 local-part 與 domain
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
}
