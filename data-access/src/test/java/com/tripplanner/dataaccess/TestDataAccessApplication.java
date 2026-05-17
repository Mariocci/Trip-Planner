package com.tripplanner.dataaccess;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test configuration for data-access module tests.
 * Provides Spring Boot configuration for repository tests.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "com.tripplanner.dataaccess.repository")
@EntityScan(basePackages = "com.tripplanner.domain.entity")
public class TestDataAccessApplication {
}
