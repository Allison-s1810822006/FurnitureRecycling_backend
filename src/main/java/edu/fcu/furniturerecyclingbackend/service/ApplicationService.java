package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.dto.ApplicationRequestDto;
import edu.fcu.furniturerecyclingbackend.dto.ApplicationResponseDto;
import edu.fcu.furniturerecyclingbackend.model.*;
import edu.fcu.furniturerecyclingbackend.repository.ApplicationRepository;
import edu.fcu.furniturerecyclingbackend.repository.ScheduleRepository;
import edu.fcu.furniturerecyclingbackend.repository.StationRepository;
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

    // 使用 Application#isLocked() 檢查是否鎖定（已受理）

    /** 建立新的清運申請 → 回傳 DTO */
    @Transactional
    public ApplicationResponseDto createApplication(ApplicationRequestDto dto) {
        // 驗證 DropPoint 代碼（用 enum）
        if (!DropPoint.isValid(dto.getDropPointCode())) {
            throw new IllegalArgumentException("Invalid dropPointCode");
        }

        // 驗證站點
        Station station = stationRepository.findById(dto.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid stationId"));

        // 驗證時段
        Schedule schedule = scheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid scheduleId"));

        // 驗證日期一致
        if (!schedule.getScheduleDate().equals(dto.getRequestedDate())) {
            throw new IllegalArgumentException("requestedDate must equal schedule_date");
        }

        Application app = new Application();
        app.setUserId(dto.getUserId());
        app.setStation(station);                  // 關聯實體
        app.setSchedule(schedule);                // 關聯實體
        app.setDropPointCode(dto.getDropPointCode());
        app.setRequestedDate(dto.getRequestedDate());
        app.setTotalItems(Optional.ofNullable(dto.getTotalItems()).orElse(0));
        app.setTotalVolumeM3(Optional.ofNullable(dto.getTotalVolumeM3()).orElse(BigDecimal.ZERO));
        app.setSuggestedVehicle(Optional.ofNullable(dto.getSuggestedVehicle()).orElse("FLATBED"));
        app.setStatus(Optional.ofNullable(dto.getStatus()).orElse(ApplicationStatus.SUBMITTED));

        // 驗證 items 家具申請項目
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("申請至少需有一個已添加項目");
        }
        // 建立家具項目並驗證
        for (ApplicationRequestDto.FurnitureItemDto itemDto : dto.getItems()) {
            // 驗證數量範圍
            if (itemDto.getQuantity() == null || itemDto.getQuantity() < 1 || itemDto.getQuantity() > 5) {
                throw new IllegalArgumentException("家具數量必須介於 1 到 5 之間");
            }
            // 驗證照片數量
            if (itemDto.getPhotoUrls() == null || itemDto.getPhotoUrls().size() != itemDto.getQuantity()) {
                throw new IllegalArgumentException(
                    String.format("%s-%s 照片數量 (%d) 必須等於選擇的數量 (%d)",
                        itemDto.getCategory(), itemDto.getSubType(),
                        itemDto.getPhotoUrls() == null ? 0 : itemDto.getPhotoUrls().size(), itemDto.getQuantity()));
            }
            // 驗證照片格式與大小（假設 URL 末尾為檔名，實際大小需前端或檔案服務驗證）
            for (String url : itemDto.getPhotoUrls()) {
                if (!url.toLowerCase().endsWith(".jpg")) {
                    throw new IllegalArgumentException("照片格式必須為 jpg，錯誤檔案: " + url);
                }
                // 可加強：若有檔案服務 API，可查詢檔案大小，這裡僅驗證格式
            }
            // 建立 FurnitureItem 實體
            FurnitureItem furnitureItem = new FurnitureItem();
            furnitureItem.setApplication(app); // 關聯申請
            furnitureItem.setMainType(FurnitureItem.MainType.valueOf(itemDto.getCategory()));
            furnitureItem.setSubType(FurnitureItem.SubType.valueOf(itemDto.getSubType()));
            furnitureItem.setItemCount(itemDto.getQuantity());
            furnitureItem.setPhotoUrls(itemDto.getPhotoUrls());
            app.getFurnitureItems().add(furnitureItem);
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

        if (dto.getStationId() != null) {
            Station station = stationRepository.findById(dto.getStationId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid stationId"));
            app.setStation(station);
        }
        if (dto.getScheduleId() != null) {
            Schedule schedule = scheduleRepository.findById(dto.getScheduleId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid scheduleId"));
            app.setSchedule(schedule);
            // 你若希望更新時也檢查日期一致，可打開以下檢查：
            // if (dto.getRequestedDate() != null && !schedule.getScheduleDate().equals(dto.getRequestedDate())) {
            //     throw new IllegalArgumentException("requestedDate must equal schedule_date");
            // }
        }

        if (dto.getRequestedDate() != null) app.setRequestedDate(dto.getRequestedDate());
        if (dto.getDropPointCode() != null) {
            if (!DropPoint.isValid(dto.getDropPointCode())) {
                throw new IllegalArgumentException("Invalid dropPointCode");
            }
            app.setDropPointCode(dto.getDropPointCode());
        }
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
        dto.setStationId(app.getStation() != null ? app.getStation().getStationId() : null);

        dto.setScheduleId(app.getSchedule() != null ? app.getSchedule().getScheduleId() : null);

        dto.setDropPointCode(app.getDropPointCode());
        dto.setRequestedDate(app.getRequestedDate());
        dto.setTotalItems(app.getTotalItems());
        dto.setTotalVolumeM3(app.getTotalVolumeM3());
        dto.setSuggestedVehicle(app.getSuggestedVehicle());
        dto.setStatus(app.getStatus());
        // 是否可進入編輯畫面（若已受理則不可）
        dto.setEditable(!app.isLocked());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setUpdatedAt(app.getUpdatedAt());
        return dto;
    }
}
