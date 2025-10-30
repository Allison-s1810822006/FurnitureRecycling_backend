package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "furniture")
public class FurnitureItem {

    @Id
    @Column(name = "item_id")
    private Integer itemId;  // 對應資料表欄位名稱

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "length_m", nullable = false)
    private Double lengthM;

    @Column(name = "width_m", nullable = false)
    private Double widthM;

    @Column(name = "height_m", nullable = false)
    private Double heightM;

    @Column(name = "type", nullable = false)
    private String type;
}