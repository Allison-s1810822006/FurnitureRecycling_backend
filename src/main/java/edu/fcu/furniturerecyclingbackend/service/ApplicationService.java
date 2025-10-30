package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.dto.ApplicationRequestDto;
import edu.fcu.furniturerecyclingbackend.dto.ApplicationResponseDto;
import edu.fcu.furniturerecyclingbackend.model.*;
import edu.fcu.furniturerecyclingbackend.repository.ApplicationRepository;
import edu.fcu.furniturerecyclingbackend.repository.ScheduleRepository;
import edu.fcu.furniturerecyclingbackend.repository.StationRepository;
import edu.fcu.furniturerecyclingbackend.repository.FurnitureItemRepository;
import edu.fcu.furniturerecyclingbackend.repository.ApplicationItemRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StationRepository stationRepository;
    private final ScheduleRepository scheduleRepository;
    private final FurnitureItemRepository furnitureItemRepository;
    private final ApplicationItemRepository applicationItemRepository;

    // 使用 Application#isLocked() 檢查是否鎖定（已受理）

    /** 建立新的清運申請 → 回傳 DTO */
    @Transactional
    public ApplicationResponseDto createApplication(ApplicationRequestDto dto) {
        // 驗證站點（僅驗證存在，不再使用 station 實體）
        if (!stationRepository.existsById(dto.getStationId())) {
            throw new IllegalArgumentException("Invalid stationId");
        }

        // 驗證時段
        Schedule schedule = scheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid scheduleId"));

        // 驗證日期一致
        if (!schedule.getScheduleDate().equals(dto.getRequestedDate())) {
            throw new IllegalArgumentException("requestedDate must equal schedule_date");
        }

        Application app = new Application();
        app.setUserId(dto.getUserId());
        app.setSchedule(schedule);
        app.setRequestedDate(dto.getRequestedDate());
        app.setTotalItems(Optional.ofNullable(dto.getTotalItems()).orElse(0));
        app.setTotalVolumeM3(Optional.ofNullable(dto.getTotalVolumeM3()).orElse(BigDecimal.ZERO));
        app.setSuggestedVehicle(Optional.ofNullable(dto.getSuggestedVehicle()).orElse("FLATBED"));
        app.setStatus(Optional.ofNullable(dto.getStatus()).orElse(ApplicationStatus.SUBMITTED));

        // 驗證 items 家具申請項目
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("申請至少需有一個已添加項目");
        }
        // 驗證：同一申請只能有一個家具主類型及細分選項
        // 已移除 getCategory 驗證，僅保留細分選項驗證
        if (dto.getItems().stream().map(ApplicationRequestDto.FurnitureItemDto::getItemName).distinct().count() > 1) {
            throw new IllegalArgumentException("每次申請只能選擇一個細分選項");
        }
        // 建立 ApplicationItem 實體並驗證
        for (ApplicationRequestDto.FurnitureItemDto itemDto : dto.getItems()) {
            // 驗證數量範圍（不可小於1，不可大於5）
            if (itemDto.getQuantity() == null || itemDto.getQuantity() < 1 || itemDto.getQuantity() > 5) {
                throw new IllegalArgumentException("家具數量必須介於 1 到 5 之間");
            }
            // 驗證照片數量需與家具數量相符
            if (itemDto.getPhotos() == null || itemDto.getPhotos().size() != itemDto.getQuantity()) {
                throw new IllegalArgumentException(
                    String.format("%s 照片數量 (%d) 必須等於選擇的數量 (%d)",
                        itemDto.getItemName(),
                        itemDto.getPhotos() == null ? 0 : itemDto.getPhotos().size(), itemDto.getQuantity()));
            }
            // 驗證照片格式（jpg/png）與大小（由前端或檔案服務驗證）
            for (String url : itemDto.getPhotos()) {
                if (!url.toLowerCase().matches(".+\\.(jpg|jpeg|png)$")) {
                    throw new IllegalArgumentException("照片格式必須為 jpg 或 png，錯誤檔案: " + url);
                }
                // 檔案大小驗證由前端或檔案服務負責
            }
            // 建立 ApplicationItem 實體
            ApplicationItem applicationItem = new ApplicationItem();
            applicationItem.setApplication(app); // 關聯申請
            applicationItem.setItemName(itemDto.getItemName());
            applicationItem.setQuantity(itemDto.getQuantity());
            applicationItem.setPhotos(itemDto.getPhotos());
            // 直接設置 furnitureItemId，確保型別為 Integer
            if (itemDto.getFurnitureItemId() != null) {
                applicationItem.setFurnitureItemId(Integer.valueOf(itemDto.getFurnitureItemId().toString()));
            }
            // 加入申請單
            if (app.getApplicationItems() == null) app.setApplicationItems(new java.util.ArrayList<>());
            app.getApplicationItems().add(applicationItem);
        }

        // 驗證該站點該日期剩餘可收取數量（最多5件）
        Integer alreadyCount = applicationItemRepository.sumQuantityByStationIdAndRequestedDate(dto.getStationId(), dto.getRequestedDate());
        if (alreadyCount == null) alreadyCount = 0;
        int newCount = dto.getItems().stream().mapToInt(ApplicationRequestDto.FurnitureItemDto::getQuantity).sum();
        if (alreadyCount + newCount > 5) {
            throw new IllegalArgumentException(String.format("該站點 %s 日期 %s 最多只能收取5件家具，剩餘可收取數量：%d", dto.getStationId(), dto.getRequestedDate(), 5 - alreadyCount));
        }

        // 時間交給 @PrePersist/@PreUpdate
        Application saved = applicationRepository.save(app);
        return toDto(saved);
    }

    /** 查全部 → 回傳 DTO 列表 */
    public List<ApplicationResponseDto> findAll() {
        return applicationRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /** 查單一 → 回傳 DTO（或 null） */
    public ApplicationResponseDto findById(UUID id) {
        return applicationRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /** 更新（部分欄位） → 回傳 DTO */
    @Transactional
    public ApplicationResponseDto update(UUID id, ApplicationRequestDto dto) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // 在進入編輯流程前，統一由實體驗證是否允許編輯（若已受理會拋例外）
        app.ensureEditable();

        if (dto.getScheduleId() != null) {
            Schedule schedule = scheduleRepository.findById(dto.getScheduleId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid scheduleId"));
            app.setSchedule(schedule);
        }
        if (dto.getRequestedDate() != null) app.setRequestedDate(dto.getRequestedDate());
        if (dto.getTotalItems() != null) app.setTotalItems(dto.getTotalItems());
        if (dto.getTotalVolumeM3() != null) app.setTotalVolumeM3(dto.getTotalVolumeM3());
        if (dto.getSuggestedVehicle() != null) app.setSuggestedVehicle(dto.getSuggestedVehicle());
        if (dto.getStatus() != null) app.setStatus(dto.getStatus()); // enum

        Application saved = applicationRepository.save(app);
        return toDto(saved);
    }

    /** 刪除 → 回傳是否成功 */
    @Transactional
    public boolean delete(UUID id) {
        var opt = applicationRepository.findById(id);
        if (opt.isPresent()) {
            Application app = opt.get();
            // 刪除也統一使用實體的檢查（若已受理會拋例外）
            app.ensureEditable();
            applicationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /* ===================== Private Mapper ===================== */

    /** 實體 → DTO（把關聯物件攤平成 ID，避免 swagger 掃描到雙向關聯） */
    private ApplicationResponseDto toDto(Application app) {
        ApplicationResponseDto dto = new ApplicationResponseDto();
        dto.setApplicationId(app.getApplicationId());
        dto.setUserId(app.getUserId());
        dto.setStationId(app.getStation() != null ? app.getStation().getStationId() : null); // ApplicationResponseDto.stationId 型別已改為 String
        dto.setScheduleId(app.getSchedule() != null ? app.getSchedule().getScheduleId() : null);
        dto.setRequestedDate(app.getRequestedDate());
        dto.setTotalItems(app.getTotalItems());
        dto.setTotalVolumeM3(app.getTotalVolumeM3());
        dto.setSuggestedVehicle(app.getSuggestedVehicle());
        dto.setStatus(app.getStatus());
        dto.setEditable(!app.isLocked());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setUpdatedAt(app.getUpdatedAt());
        return dto;
    }
}
