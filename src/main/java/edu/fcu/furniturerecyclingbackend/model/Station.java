package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "station")  // 映射資料庫中的 station 表
public class Station {

    @Id
    private UUID stationId;  // 主鍵，使用 UUID 類型

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String serviceStart;  // 使用 String 儲存時間（例如 "09:00"）
    private String serviceEnd;    // 使用 String 儲存時間（例如 "17:00"）

    @Column(name = "service_weekday")
    private int[] serviceWeekday; // 用 int[] 儲存一週中哪些天提供服務

    private Short amount;  // 使用 Short 類型來儲存 amount，對應到 PostgreSQL 的 int2

    // Constructor
    public Station() {
    }

    // Getters and Setters
    public UUID getStationId() {
        return stationId;
    }

    public void setStationId(UUID stationId) {
        this.stationId = stationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getServiceStart() {
        return serviceStart;
    }

    public void setServiceStart(String serviceStart) {
        this.serviceStart = serviceStart;
    }

    public String getServiceEnd() {
        return serviceEnd;
    }

    public void setServiceEnd(String serviceEnd) {
        this.serviceEnd = serviceEnd;
    }

    public int[] getServiceWeekday() {
        return serviceWeekday;
    }

    public void setServiceWeekday(int[] serviceWeekday) {
        this.serviceWeekday = serviceWeekday;
    }

    public Short getAmount() {
        return amount;
    }

    public void setAmount(Short amount) {
        this.amount = amount;
    }

    // Override toString, equals, and hashCode if necessary
}

