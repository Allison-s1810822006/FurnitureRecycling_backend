package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Column(name = "station_id", length = 5, nullable = false)
    private String stationId; // 直接存 DP001~DP005

    @Column(nullable = false)
    private String name; // 一號定點~五號定點

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Short amount;
}
