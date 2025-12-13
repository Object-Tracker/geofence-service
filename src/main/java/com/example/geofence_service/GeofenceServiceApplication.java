package com.example.geofence_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GeofenceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeofenceServiceApplication.class, args);
	}

}
