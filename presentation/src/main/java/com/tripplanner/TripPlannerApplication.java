package com.tripplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application entry point for Trip Planner.
 * This application follows a 3-tier architecture with layered backend:
 * - Presentation Layer (Controllers)
 * - Business Logic Layer (Services)
 * - Data Access Layer (Repositories)
 */
@SpringBootApplication(scanBasePackages = "com.tripplanner")
public class TripPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripPlannerApplication.class, args);
    }
}
