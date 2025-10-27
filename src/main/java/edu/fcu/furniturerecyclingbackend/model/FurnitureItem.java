package edu.fcu.furniturerecyclingbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.fcu.furniturerecyclingbackend.config.StringListJsonConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "furniture")
public class FurnitureItem {

    @Id
    @Column(name = "item_id", columnDefinition = "uuid")
    private UUID itemId;  // 對應資料表欄位名稱

    /** 關聯 Application （多對一） */
    @JsonIgnore // 避免雙向 JSON 遞迴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", referencedColumnName = "application_id")
    private Application application;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "length_m")
    private Double lengthM;

    @Column(name = "width_m")
    private Double widthM;

    @Column(name = "height_m")
    private Double heightM;

    @Column(name = "type")
    private String type;

    // 家具細分選項枚舉，包含所有沙發與床架細分選項
    @Getter
    public enum SubType {
        SOFA_SINGLE("單人座沙發", MainType.SOFA),
        SOFA_DOUBLE("雙人座沙發", MainType.SOFA),
        SOFA_TRIPLE("三人座沙發", MainType.SOFA),
        BED_SINGLE("單人床架", MainType.BED),
        BED_DOUBLE("雙人床架", MainType.BED);
        // 顯示名稱
        private final String displayName;
        // 所屬主類型
        private final MainType mainType;
        SubType(String displayName, MainType mainType) {
            this.displayName = displayName;
            this.mainType = mainType;
        }
    }

    // 家具主類型枚舉（沙發、床架）
    @Getter
    public enum MainType {
        SOFA("沙發"),
        BED("床架");
        private final String displayName;
        MainType(String displayName) {
            this.displayName = displayName;
        }
    }

    // 家具主類型（沙發或床架），可由細分選項自動推導
    @Enumerated(EnumType.STRING)
    @Column(name = "main_type")
    private MainType mainType;

    // 家具細分選項（如單人座沙發、雙人床架）
    @Enumerated(EnumType.STRING)
    @Column(name = "sub_type")
    private SubType subType;

    // 張數（數量），代表該細分選項的數量
    @Column(name = "item_count", nullable = false)
    private Integer itemCount = 0;

    // 照片 URL（多張），直接存成 JSON 字串在 furniture 表的 photo_urls 欄位
    // 例如：["url1", "url2", ...]
    @Column(name = "photo_urls", columnDefinition = "TEXT")
    @Convert(converter = StringListJsonConverter.class)
    private java.util.List<String> photoUrls = new java.util.ArrayList<>();

    /** 自動生成 UUID */
    @PrePersist
    public void prePersist() {
        if (this.itemId == null) {
            this.itemId = UUID.randomUUID();
        }
    }
}