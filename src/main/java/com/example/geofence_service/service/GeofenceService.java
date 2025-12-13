package com.example.geofence_service.service;

import com.example.geofence_service.dto.LocationUpdateMessage;
import com.example.geofence_service.dto.NotificationMessage;
import com.example.geofence_service.entity.Notification;
import com.example.geofence_service.entity.TrackedObject;
import com.example.geofence_service.repository.NotificationRepository;
import com.example.geofence_service.repository.TrackedObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeofenceService {

    private final TrackedObjectRepository trackedObjectRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FirebasePushService firebasePushService;

    private static final double EARTH_RADIUS_METERS = 6371000;

    @RabbitListener(queues = "location.queue")
    @Transactional
    public void processLocationUpdate(LocationUpdateMessage message) {
        log.info("Received location update for object {} from user {}", message.getObjectId(), message.getUserId());

        // Check if geofence is configured
        if (message.getGeofenceCenterLat() == null || message.getGeofenceCenterLng() == null
                || message.getGeofenceRadiusMeters() == null) {
            log.info("No geofence configured for user {}", message.getUserId());
            return;
        }

        // Calculate distance from geofence center
        double distance = calculateDistance(
                message.getGeofenceCenterLat(),
                message.getGeofenceCenterLng(),
                message.getLatitude(),
                message.getLongitude()
        );

        boolean isOutside = distance > message.getGeofenceRadiusMeters();

        // Get current status from database
        TrackedObject trackedObject = trackedObjectRepository.findById(message.getObjectId())
                .orElse(null);

        if (trackedObject == null) {
            log.warn("Tracked object {} not found", message.getObjectId());
            return;
        }

        boolean wasOutside = Boolean.TRUE.equals(trackedObject.getOutsideGeofence());

        // Update the outsideGeofence status
        trackedObject.setOutsideGeofence(isOutside);
        trackedObjectRepository.save(trackedObject);

        // Send notification if status changed
        if (isOutside && !wasOutside) {
            // Object just left the geofence
            sendNotification(message, "GEOFENCE_EXIT",
                    String.format("'%s' has left the safe zone!", message.getObjectName()));
        } else if (!isOutside && wasOutside) {
            // Object just entered the geofence
            sendNotification(message, "GEOFENCE_ENTER",
                    String.format("'%s' has returned to the safe zone.", message.getObjectName()));
        }

        log.info("Object {} is {} geofence (distance: {} meters, radius: {} meters)",
                message.getObjectId(),
                isOutside ? "OUTSIDE" : "INSIDE",
                String.format("%.2f", distance),
                message.getGeofenceRadiusMeters());
    }

    private void sendNotification(LocationUpdateMessage message, String type, String notificationMessage) {
        // Save notification to database
        Notification notification = Notification.builder()
                .userId(message.getUserId())
                .objectId(message.getObjectId())
                .objectName(message.getObjectName())
                .objectType(message.getObjectType())
                .message(notificationMessage)
                .type(type)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        NotificationMessage wsMessage = NotificationMessage.builder()
                .userId(message.getUserId())
                .objectId(message.getObjectId())
                .objectName(message.getObjectName())
                .objectType(message.getObjectType())
                .message(notificationMessage)
                .type(type)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/notifications/" + message.getUserId(), wsMessage);
        log.info("Sent {} notification for object {} to user {}", type, message.getObjectId(), message.getUserId());

        // Send Firebase Push notification
        String title = type.equals("GEOFENCE_EXIT") ? "Object Left Safe Zone!" : "Object Returned";
        firebasePushService.sendPushNotification(message.getUserId(), title, notificationMessage, type, message.getObjectId());
    }

    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}
