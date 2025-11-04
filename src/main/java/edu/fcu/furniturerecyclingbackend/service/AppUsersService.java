package edu.fcu.furniturerecyclingbackend.service;
import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import edu.fcu.furniturerecyclingbackend.dto.RegistrationDTO;
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
    public Optional<AppUsers> getUserById(UUID userId) {
        return appUsersRepository.findById(userId);
    }

    // 根據 email 查找使用者
    public Optional<AppUsers> getUserByEmail(String email) {
        return appUsersRepository.findByEmail(email);
    }

    // 創建新的使用者
    public AppUsers createUser(AppUsers appUsers) {
        return appUsersRepository.save(appUsers);
    }

    // 更新現有的使用者
    public AppUsers updateUser(UUID userId, AppUsers updatedUser) {
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

    /**
     * 根據 LINE 註冊資料建立新會員
     * @param registrationDTO LINE 註冊資料
     * @return 新會員物件
     */
    public AppUsers createUserFromLine(RegistrationDTO registrationDTO) {
        // 建立 AppUsers 物件，填入 LINE 資料
        AppUsers user = new AppUsers();
        // 對應資料庫欄位
        user.setLineUserId(registrationDTO.getLineUserId()); // line_user_id
        user.setLineDisplayName(registrationDTO.getDisplayName()); // line_display_name
        user.setLinePictureUrl(registrationDTO.getPictureUrl()); // line_picture_url
        user.setLineEmail(registrationDTO.getEmail()); // line_email
        user.setPhone(registrationDTO.getPhone()); // phone，前端補齊
        user.setLineBoundAt(java.time.OffsetDateTime.now()); // line_bound_at，自動填入綁定時間
        user.setFullName(registrationDTO.getFullName()); // full_name，前端補齊
        // 其他欄位可依需求補齊
        // 儲存至資料庫
        return appUsersRepository.save(user);
    }

    /**
     * Simple login: find user by email, or create a new user with provided info.
     * If a user exists, update fullName/phone if they differ.
     */
    public AppUsers simpleLoginOrRegister(String fullName, String email, String phone) {
        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            throw new IllegalArgumentException("請至少填寫 Email 或電話");
        }

        return appUsersRepository.findFirstByEmailOrPhone(email, phone)
                .orElseGet(() -> {
                    AppUsers u = new AppUsers();
                    u.setFullName(fullName);
                    u.setEmail(email);
                    u.setPhone(phone);
                    return appUsersRepository.save(u);
                });
    }

}
