package edu.fcu.furniturerecyclingbackend.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CurrentUserDto {
    private UUID userId;
    private String fullName;
    private String email;
}

