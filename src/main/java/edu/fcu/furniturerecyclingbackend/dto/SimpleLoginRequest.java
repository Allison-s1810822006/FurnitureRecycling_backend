package edu.fcu.furniturerecyclingbackend.dto;

import lombok.Data;

@Data
public class SimpleLoginRequest {
    private String fullName;
    private String email;
    private String phone;
}

