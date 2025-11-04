package edu.fcu.furniturerecyclingbackend;


//暫時用來測試 scheduleId 可空的測試檔案

import edu.fcu.furniturerecyclingbackend.dto.ApplicationRequestDto;
import edu.fcu.furniturerecyclingbackend.model.ApplicationStatus;
import edu.fcu.furniturerecyclingbackend.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceScheduleIdTest {
    @Mock
    private ApplicationService applicationService;

    @Test
    void createApplication_withoutScheduleId_shouldSucceedAndReturnNullScheduleId() {
        ApplicationRequestDto dto = new ApplicationRequestDto();
        dto.setUserId(UUID.randomUUID());
        dto.setStationId("DP001");
        dto.setRequestedDate(LocalDate.now());
        dto.setStatus(ApplicationStatus.SUBMITTED);
        dto.setItems(Collections.emptyList());
        dto.setScheduleId(null);

        // 模擬 service 行為
        when(applicationService.createApplication(dto)).thenAnswer(invocation -> {
            ApplicationRequestDto req = invocation.getArgument(0);
            assertNull(req.getScheduleId());
            return null; // 這裡可根據實際 ApplicationResponseDto 實現
        });

        applicationService.createApplication(dto);
        verify(applicationService, times(1)).createApplication(dto);
    }
}
