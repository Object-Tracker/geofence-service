package com.example.geofence_service.repository;

import com.example.geofence_service.entity.TrackedObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackedObjectRepository extends JpaRepository<TrackedObject, Long> {
}
