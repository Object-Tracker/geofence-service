package com.example.geofence_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long objectId;

    private String objectName;
    private String objectType;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String type; // GEOFENCE_EXIT, GEOFENCE_ENTER

    @Builder.Default
    private Boolean read = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
