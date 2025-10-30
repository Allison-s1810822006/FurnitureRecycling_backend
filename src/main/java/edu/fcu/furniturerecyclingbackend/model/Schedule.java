package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;     // 對應 timestamptz
import java.time.LocalDate;  // 對應 date
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "schedule_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID scheduleId;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate; // 排程日期

    @Column(name = "eta", nullable = false)
    private Instant eta; // 預估抵達時間 (timestamptz)

    @Column(name = "plate_number", nullable = false)
    private String plateNumber; // 車牌號碼

    // （選擇性）如果之後想讓 Schedule 反向查 Application，可開啟這段：
    /*
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = false)
    @com.fasterxml.jackson.annotation.JsonManagedReference("schedule-apps")
    private List<Application> applications = new ArrayList<>();
    */

    @PrePersist
    public void prePersist() {
        if (this.scheduleId == null) this.scheduleId = UUID.randomUUID();
    }
}
