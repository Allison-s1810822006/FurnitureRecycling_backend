package edu.fcu.furniturerecyclingbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "category")
    private String category;

    @Column(name = "variant_code")
    private String variantCode;

    @Column(name = "photo_url")
    private String photoUrl;

    /** 自動生成 UUID */
    @PrePersist
    public void prePersist() {
        if (this.itemId == null) {
            this.itemId = UUID.randomUUID();
        }
    }
}
