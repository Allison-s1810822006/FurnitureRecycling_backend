package edu.fcu.furniturerecyclingbackend.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LineProfile {
    String lineUserId;
    String displayName;
    String pictureUrl;
    String email;
}
