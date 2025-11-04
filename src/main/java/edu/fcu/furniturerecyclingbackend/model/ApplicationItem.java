package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ApplicationItem Entity
 * 對應 application_items 資料表，紀錄申請單中的家具細項內容
 * 包含家具類型、數量、照片等欄位
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "application_items")
public class ApplicationItem {
    /** 主鍵 UUID */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "item_id", columnDefinition = "uuid")
    private UUID itemId;

    /** 關聯 Application (多對一) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", referencedColumnName = "application_id", nullable = false)
    private Application application;

    /** 關聯 FurnitureItem (以 integer 儲存) */
    @Column(name = "furniture_item_id")
    private Integer furnitureItemId;

    /** 家具細分名稱 (例：單人沙發、雙人床架) */
    @Column(name = "item_name", nullable = false)
    private String itemName;

    /** 家具數量，預設 0，不可為 NULL */
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    /** 家具照片（多張 URL），直接存成 JSON 字串在 photos 欄位 */
    @Column(name = "photos", columnDefinition = "TEXT")
    @Convert(converter = edu.fcu.furniturerecyclingbackend.config.StringListJsonConverter.class)
    private List<String> photos = new ArrayList<>();

    /** 建立時間（含時區），預設 NOW() */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /** 更新時間（含時區），自動更新 NOW() */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
