package edu.fcu.furniturerecyclingbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class App_Users {
    @Id
    private UUID userId;  // 主鍵，使用 UUID 類型

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String passwordHash;

    private Boolean isAdmin;

    private String email;

    private String phone;

    private String address;

    // Constructor
    public App_Users() {
    }

    public App_Users(String fullName, String email, String encodedPassword) {
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Override toString, equals, and hashCode if necessary
}
