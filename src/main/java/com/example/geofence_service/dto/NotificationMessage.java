package com.example.geofence_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage implements Serializable {
    private Long userId;
    private Long objectId;
    private String objectName;
    private String objectType;
    private String message;
    private String type; // GEOFENCE_EXIT, GEOFENCE_ENTER
    private LocalDateTime timestamp;
}
