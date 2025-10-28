package edu.fcu.furniturerecyclingbackend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.sql.Time;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stations")
public class Station {

    /**
     * 五個定點，id 寫死為 DP001~DP005，name 對應一號定點~五號定點
     * stationId 欄位型別改為 String，直接存 DP001~DP005
     */
    @Id
    @Column(name = "station_id", length = 8, nullable = false)
    private String stationId; // 直接存 DP001~DP005

    @Column(nullable = false)
    private String name; // 一號定點~五號定點

    @Column(nullable = false)
    private String address;

    @Column(name = "service_start", nullable = false)
    private Time serviceStart;

    @Column(name = "service_end", nullable = false)
    private Time serviceEnd;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "service_weekday", columnDefinition = "integer[]", nullable = false)
    private Integer[] serviceWeekday;

    @Column(nullable = false)
    private Short amount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonManagedReference("station-apps") // 成對對應 Application 的 @JsonBackReference("station-apps")
    private List<Application> applications = new ArrayList<>();

    /**
     * 五個定點，id 寫死為 DP001~DP005，name 對應一號定點~五號定點
     * stationId 欄位型別改為 String，直接存 DP001~DP005
     */
    // stationId: DP001~DP005
    // name: 一號定點~五號定點

    // 移除 UUID 相關邏輯
    @PrePersist
    public void prePersist() {
        // 不再自動產生 UUID，stationId 由資料初始化時直接指定 DP001~DP005
    }
}
