package com.tripplanner.presentation.config;

import org.camunda.bpm.engine.RepositoryService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class CamundaConfig {

    private final RepositoryService repositoryService;

    public CamundaConfig(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void deployProcesses() {
        try {
            ClassPathResource resource = new ClassPathResource("trip-approval-process.bpmn");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    repositoryService.createDeployment()
                        .addInputStream("trip-approval-process.bpmn", inputStream)
                        .name("Trip Approval Process")
                        .deploy();
                    System.out.println("✓ Successfully deployed Trip Approval Process");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to deploy process: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
