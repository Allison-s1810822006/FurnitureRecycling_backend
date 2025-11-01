package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;     // 對應 timestamptz
import java.time.LocalDate;  // 對應 date
import java.util.UUID;

/**
 * Schedule
 * 清運行程 Entity，對應 schedules 資料表。
 * 包含行程主鍵、日期、預估抵達時間、車牌號碼等欄位。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "schedules")
public class Schedule {
    /** 行程主鍵 UUID，對應 schedules.schedule_id */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "schedule_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID scheduleId;

    /** 行程日期，對應 schedules.schedule_date */
    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate; // 排程日期

    /** 預估抵達時間，對應 schedules.eta */
    @Column(name = "eta", nullable = false)
    private Instant eta; // 預估抵達時間 (timestamptz)

    /** 車牌號碼，對應 schedules.plate_number */
    @Column(name = "plate_number", nullable = false)
    private String plateNumber; // 車牌號碼

    // （選擇性）如果之後想讓 Schedule 反向查 Application，可開啟這段：
    /*
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = false)
    @com.fasterxml.jackson.annotation.JsonManagedReference("schedule-apps")
    private List<Application> applications = new ArrayList<>();
    */

    /** 新增時自動設定主鍵 UUID */
    @PrePersist
    public void prePersist() {
        if (this.scheduleId == null) this.scheduleId = UUID.randomUUID();
    }
}
