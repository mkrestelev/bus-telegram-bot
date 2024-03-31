package com.krestelev.antalyabus.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequest {
    private Long userId;
    private String stopId;
    private String busId;
    private boolean observing;

    public static UserRequest of(Long userId, String stopId) {
        return UserRequest.builder()
            .userId(userId)
            .stopId(stopId)
            .build();
    }

    public boolean isParticularBusRequested() {
        return busId != null && !busId.isEmpty() && !"all".equals(busId);
    }
}
