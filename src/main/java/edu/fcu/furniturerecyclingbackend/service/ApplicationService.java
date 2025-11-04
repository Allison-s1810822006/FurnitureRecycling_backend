package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.dto.ApplicationRequestDto;
import edu.fcu.furniturerecyclingbackend.dto.ApplicationResponseDto;
import edu.fcu.furniturerecyclingbackend.model.*;
import edu.fcu.furniturerecyclingbackend.repository.ApplicationRepository;
import edu.fcu.furniturerecyclingbackend.repository.ScheduleRepository;
import edu.fcu.furniturerecyclingbackend.repository.StationRepository;
import edu.fcu.furniturerecyclingbackend.repository.FurnitureItemRepository;
import edu.fcu.furniturerecyclingbackend.repository.ApplicationItemRepository;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StationRepository stationRepository;
    private final ScheduleRepository scheduleRepository;
    private final FurnitureItemRepository furnitureItemRepository;
    private final ApplicationItemRepository applicationItemRepository;
    private final AppUsersRepository appUsersRepository;

    // 使用 Application#isLocked() 檢查是否鎖定（已受理）

    /** 建立新的清運申請 → 回傳 DTO */
    @Transactional
    public ApplicationResponseDto createApplication(ApplicationRequestDto dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("userId 不可為 null");
        }
        if (dto.getStationId() == null) {
            throw new IllegalArgumentException("stationId 不可為 null");
        }
        if (dto.getRequestedDate() == null) {
            throw new IllegalArgumentException("requestedDate 不可為 null");
        }
        // ✅ 新增：清運日期至少為後天（以後端 server 時區為準）
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.plusDays(2);
        if (dto.getRequestedDate().isBefore(minDate)) {
            throw new IllegalArgumentException("清運日期至少需為後天（" + minDate + "）");
        }
        AppUsers user = appUsersRepository.findById(dto.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("找不到使用者: " + dto.getUserId()));
        Station station = stationRepository.findById(dto.getStationId())
            .orElseThrow(() -> new IllegalArgumentException("找不到站點: " + dto.getStationId()));
        Schedule schedule = null;
        if (dto.getScheduleId() != null) {
            schedule = scheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("找不到清運時段: " + dto.getScheduleId()));
            if (!schedule.getScheduleDate().equals(dto.getRequestedDate())) {
                throw new IllegalArgumentException("requestedDate 必須等於 schedule_date");
            }
        }
        // 新邏輯：使用 DTO 明細重算 totals，並合併相同家具（合併 quantity 與 photos）
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("申請至少需有一個家具項目");
        }

        Application app = new Application();
        app.setUser(user);
        app.setStation(station);
        app.setSchedule(schedule); // 可能為 null
        app.setRequestedDate(dto.getRequestedDate());
        app.setStatus(Optional.ofNullable(dto.getStatus()).orElse(ApplicationStatus.SUBMITTED));
        app.setSuggestedVehicle(Optional.ofNullable(dto.getSuggestedVehicle()).orElse("FLATBED"));

        int totalItems = 0;
        BigDecimal totalVolume = BigDecimal.ZERO;

        Map<Integer, ApplicationItem> groupedItems = new HashMap<>();

        for (ApplicationRequestDto.FurnitureItemDto itemDto : dto.getItems()) {
            Integer fid = itemDto.getFurnitureItemId();
            if (fid == null) {
                throw new IllegalArgumentException("無效的家具 ID: null");
            }
            FurnitureItem furn = furnitureItemRepository.findById(fid)
                    .orElseThrow(() -> new IllegalArgumentException("無效的家具 ID: " + fid));

            // 累計數量與體積
            totalItems += itemDto.getQuantity();
            double singleVolume = furn.getLengthM() * furn.getWidthM() * furn.getHeightM();
            BigDecimal volumeForThisItem = BigDecimal.valueOf(singleVolume)
                    .multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalVolume = totalVolume.add(volumeForThisItem);

            // 合併相同家具項目
            groupedItems.compute(fid, (key, existing) -> {
                if (existing == null) {
                    ApplicationItem newItem = new ApplicationItem();
                    newItem.setApplication(app);
                    newItem.setFurnitureItemId(fid);
                    newItem.setItemName(itemDto.getItemName() != null ? itemDto.getItemName() : furn.getItemName());
                    newItem.setQuantity(itemDto.getQuantity());
                    newItem.setPhotos(new ArrayList<>(itemDto.getPhotos() == null ? List.of() : itemDto.getPhotos()));
                    return newItem;
                } else {
                    existing.setQuantity(existing.getQuantity() + itemDto.getQuantity());
                    List<String> mergedPhotos = new ArrayList<>(existing.getPhotos() == null ? List.of() : existing.getPhotos());
                    if (itemDto.getPhotos() != null) mergedPhotos.addAll(itemDto.getPhotos());
                    existing.setPhotos(mergedPhotos);
                    return existing;
                }
            });
        }

        app.setTotalItems(totalItems);
        app.setTotalVolumeM3(totalVolume);
        app.setApplicationItems(new ArrayList<>(groupedItems.values()));

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
         dto.setUserId(app.getUser() != null ? app.getUser().getUserId() : null); // 修正：取得 AppUsers 關聯的 UUID
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
