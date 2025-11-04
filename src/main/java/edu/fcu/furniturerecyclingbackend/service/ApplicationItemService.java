package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.dto.ApplicationItemDto;
import edu.fcu.furniturerecyclingbackend.model.Application;
import edu.fcu.furniturerecyclingbackend.model.ApplicationItem;
import edu.fcu.furniturerecyclingbackend.repository.ApplicationItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ApplicationItemService
 * 提供 ApplicationItem 的業務邏輯與 CRUD 操作
 */
@Service
@RequiredArgsConstructor
public class ApplicationItemService {
    private final ApplicationItemRepository applicationItemRepository;

    /** 新增 ApplicationItem */
    public ApplicationItemDto create(ApplicationItemDto dto) {
        ApplicationItem entity = new ApplicationItem();
        entity.setItemName(dto.getItemName());
        entity.setQuantity(dto.getQuantity());
        entity.setPhotos(dto.getPhotos());
        if (dto.getApplicationId() != null) {
            Application app = new Application();
            app.setApplicationId(dto.getApplicationId());
            entity.setApplication(app);
        }
        if (dto.getFurnitureItemId() != null) {
            entity.setFurnitureItemId(dto.getFurnitureItemId());
        }
        ApplicationItem saved = applicationItemRepository.save(entity);
        return toDto(saved);
    }

    /** 查詢所有 ApplicationItem */
    public List<ApplicationItemDto> findAll() {
        return applicationItemRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** 根據 itemId 查詢 ApplicationItem */
    public Optional<ApplicationItemDto> findById(UUID itemId) {
        return applicationItemRepository.findById(itemId).map(this::toDto);
    }

    /** 根據 applicationId 查詢所有 ApplicationItem */
    public List<ApplicationItemDto> findByApplicationId(UUID applicationId) {
        return applicationItemRepository.findByApplication_ApplicationId(applicationId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /** 更新 ApplicationItem */
    public Optional<ApplicationItemDto> update(UUID itemId, ApplicationItemDto dto) {
        return applicationItemRepository.findById(itemId).map(entity -> {
            BeanUtils.copyProperties(dto, entity);
            ApplicationItem updated = applicationItemRepository.save(entity);
            ApplicationItemDto result = new ApplicationItemDto();
            BeanUtils.copyProperties(updated, result);
            return result;
        });
    }

    /** 刪除 ApplicationItem */
    public void delete(UUID itemId) {
        applicationItemRepository.deleteById(itemId);
    }

    /** Entity 轉 DTO */
    private ApplicationItemDto toDto(ApplicationItem entity) {
        ApplicationItemDto dto = new ApplicationItemDto();
        dto.setItemId(entity.getItemId());
        dto.setItemName(entity.getItemName());
        dto.setQuantity(entity.getQuantity());
        dto.setPhotos(entity.getPhotos());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        if (entity.getApplication() != null) {
            dto.setApplicationId(entity.getApplication().getApplicationId());
        }
        dto.setFurnitureItemId(entity.getFurnitureItemId());
        return dto;
    }
}
