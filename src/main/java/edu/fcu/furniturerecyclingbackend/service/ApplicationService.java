package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.dto.ApplicationRequestDto;
import edu.fcu.furniturerecyclingbackend.model.Application;
import edu.fcu.furniturerecyclingbackend.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    // 建立
    public Application createApplication(ApplicationRequestDto dto) {
        Application app = new Application();
        app.setUserId(dto.getUserId());
        app.setStationId(dto.getStationId());
        app.setScheduleId(dto.getScheduleId());
        app.setDropPointCode(dto.getDropPointCode());
        app.setRequestedDate(dto.getRequestedDate());
        app.setTotalItems(dto.getTotalItems() != null ? dto.getTotalItems() : 0);
        app.setTotalVolumeM3(dto.getTotalVolumeM3() != null ? dto.getTotalVolumeM3() : java.math.BigDecimal.ZERO);
        app.setSuggestedVehicle(dto.getSuggestedVehicle() != null ? dto.getSuggestedVehicle() : "FLATBED");
        app.setStatus(dto.getStatus() != null ? dto.getStatus() : "SUBMITTED");
        // id / createdAt / updatedAt 交給 @PrePersist / @PreUpdate
        return applicationRepository.save(app);
    }

    // 全部查詢
    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    // 以 id 查詢（找不到回傳 null，交由 Controller 決定回 404）
    public Application findById(UUID id) {
        return applicationRepository.findById(id).orElse(null);
    }

    // 更新（僅覆蓋有帶值的欄位）
    public Application update(UUID id, ApplicationRequestDto dto) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (dto.getDropPointCode() != null) app.setDropPointCode(dto.getDropPointCode());
        if (dto.getRequestedDate() != null) app.setRequestedDate(dto.getRequestedDate());
        if (dto.getTotalItems() != null) app.setTotalItems(dto.getTotalItems());
        if (dto.getTotalVolumeM3() != null) app.setTotalVolumeM3(dto.getTotalVolumeM3());
        if (dto.getSuggestedVehicle() != null) app.setSuggestedVehicle(dto.getSuggestedVehicle());
        if (dto.getStatus() != null) app.setStatus(dto.getStatus());
        if (dto.getStationId() != null) app.setStationId(dto.getStationId());
        if (dto.getScheduleId() != null) app.setScheduleId(dto.getScheduleId());
        // userId 通常不允許改，如要改再打開：
        // if (dto.getUserId() != null) app.setUserId(dto.getUserId());

        return applicationRepository.save(app);
    }

    // 刪除（找不到視為已不存在）
    public boolean delete(UUID id) {
        if (!applicationRepository.existsById(id)) {
            return false;
        }
        applicationRepository.deleteById(id);
        return true;
    }
}
