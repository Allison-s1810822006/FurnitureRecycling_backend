package edu.fcu.furniturerecyclingbackend.service;
import edu.fcu.furniturerecyclingbackend.model.AppUsers;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import edu.fcu.furniturerecyclingbackend.dto.RegistrationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.time.OffsetDateTime;

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

    // 更新現有的使用者（merge/update non-null fields）
    public AppUsers updateUser(UUID userId, AppUsers updatedUser) {
        Optional<AppUsers> opt = appUsersRepository.findById(userId);
        if (opt.isEmpty()) return null;
        AppUsers existing = opt.get();

        // Only update fields that are provided (non-null) to avoid wiping data
        if (updatedUser.getFullName() != null) existing.setFullName(updatedUser.getFullName());
        if (updatedUser.getEmail() != null) existing.setEmail(updatedUser.getEmail());
        if (updatedUser.getPhone() != null) existing.setPhone(updatedUser.getPhone());
        // allow updating LINE fields only if provided (rare)
        if (updatedUser.getLineDisplayName() != null) existing.setLineDisplayName(updatedUser.getLineDisplayName());
        if (updatedUser.getLinePictureUrl() != null) existing.setLinePictureUrl(updatedUser.getLinePictureUrl());
        if (updatedUser.getLineEmail() != null) existing.setLineEmail(updatedUser.getLineEmail());
        // if caller explicitly sets isMember true, persist it (registration flow)
        if (updatedUser.getIsMember() != null) existing.setIsMember(updatedUser.getIsMember());

        existing.setUpdatedAt(OffsetDateTime.now());
        return appUsersRepository.save(existing);
    }

    // 刪除使用者 (原為硬刪除) - 改為軟刪除實作在下面
    public void deleteUser(UUID userId) {
        // Soft-delete (downgrade + clear PII). If not found, return silently.
        Optional<AppUsers> userOpt = appUsersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return; // nothing to do
        }
        AppUsers user = userOpt.get();

        // 降級：不再視為會員
        user.setIsMember(false);

        // 清空可識別身分的欄位（視需求可調整）
        user.setFullName(null);
        user.setEmail(null);
        user.setPhone(null);
        // ⭐ 清空 LINE 相關聯欄位，確保下次同一 LINE 帳號會被當成新會員（會重新建立新筆紀錄）
        user.setLineUserId(null);
        user.setLineDisplayName(null);
        user.setLinePictureUrl(null);
        user.setLineEmail(null);

        // 更新時間戳
        user.setUpdatedAt(OffsetDateTime.now());

        // Persist changes (no deleteById)
        appUsersRepository.save(user);
    }

    /**
     * Soft-delete / downgrade user: set isMember=false and clear PII, return ResponseEntity for controller delegation.
     */
    public ResponseEntity<?> deleteUserResponse(UUID userId) {
        Optional<AppUsers> userOpt = appUsersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        AppUsers user = userOpt.get();

        // 軟刪除：降級 + 清空個資
        user.setIsMember(false);
        user.setFullName(null);
        user.setEmail(null);
        user.setPhone(null);
        // 清空 LINE 相關聯欄位
        user.setLineUserId(null);
        user.setLineDisplayName(null);
        user.setLinePictureUrl(null);
        user.setLineEmail(null);

        user.setUpdatedAt(OffsetDateTime.now());

        appUsersRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    /**
     * Find user by LINE userId
     */
    public java.util.Optional<AppUsers> findByLineUserId(String lineUserId) {
        return appUsersRepository.findByLineUserId(lineUserId);
    }

    /**
     * 根據 LINE 註冊資料建立新會員
     * @param registrationDTO LINE 註冊資料
     * @return 新會員物件
     */
    @Deprecated
    public AppUsers createUserFromLine(RegistrationDTO registrationDTO) {
        throw new UnsupportedOperationException("createUserFromLine is deprecated. Use PUT /api/app-users/{userId} to update existing user after LINE login.");
    }

    /**
     * Simple login: find user by email, or create a new user with provided info.
     * If a user exists, update fullName/phone if they differ.
     */
    public AppUsers simpleLoginOrRegister(String fullName, String email, String phone) {
        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            throw new IllegalArgumentException("請填寫 Email 及電話");
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
