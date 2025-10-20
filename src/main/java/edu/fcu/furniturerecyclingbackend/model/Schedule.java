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
    @Column(name = "schedule_id", columnDefinition = "uuid")
    private UUID scheduleId;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    @Column(name = "eta", nullable = false)
    private Instant eta;      // timestamptz -> Instant

    @Column(name = "plate_number", nullable = false)
    private String plateNumber;

    // 如需自動產生 UUID，可加這段
    @PrePersist
    public void prePersist() {
        if (this.scheduleId == null) this.scheduleId = UUID.randomUUID();
    }
}
