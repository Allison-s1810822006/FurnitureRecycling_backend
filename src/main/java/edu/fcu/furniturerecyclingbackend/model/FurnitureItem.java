package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FurnitureItem
 * 家具型錄 Entity，對應 furniture 資料表。
 * 包含家具名稱、尺寸、類型、主鍵等欄位。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity // JPA Entity 註解
@Table(name = "furniture") // 對應資料表名稱
public class FurnitureItem {
    /** 家具主鍵，對應 furniture.item_id */
    @Id
    @Column(name = "item_id")
    private Integer itemId;  // 對應資料表欄位名稱

    /** 家具名稱，對應 furniture.item_name */
    @Column(name = "item_name", nullable = false)
    private String itemName;

    /** 家具長度（公尺），對應 furniture.length_m */
    @Column(name = "length_m", nullable = false)
    private Double lengthM;

    /** 家具寬度（公尺），對應 furniture.width_m */
    @Column(name = "width_m", nullable = false)
    private Double widthM;

    /** 家具高度（公尺），對應 furniture.height_m */
    @Column(name = "height_m", nullable = false)
    private Double heightM;

    /** 家具類型，對應 furniture.type */
    @Column(name = "type", nullable = false)
    private String type;
}