package com.example.geofence_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationUpdateMessage implements Serializable {
    private Long objectId;
    private Long userId;
    private String objectName;
    private String objectType;
    private Double latitude;
    private Double longitude;
    private Double geofenceCenterLat;
    private Double geofenceCenterLng;
    private Double geofenceRadiusMeters;
}
