package com.example.geofence_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tracked_objects")
public class TrackedObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String icon;
    private Double latitude;
    private Double longitude;

    @Builder.Default
    private Boolean outsideGeofence = false;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
