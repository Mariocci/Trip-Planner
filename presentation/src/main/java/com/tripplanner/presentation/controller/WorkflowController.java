package com.tripplanner.presentation.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public WorkflowController(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startTripApprovalProcess(@RequestBody Map<String, Object> request) {
        Integer tripId = (Integer) request.get("tripId");
        Integer userId = (Integer) request.get("userId");
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("tripId", tripId);
        variables.put("userId", userId);
        variables.put("approved", null);
        variables.put("bookingConfirmed", null);
        
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
            "TripApprovalProcess", 
            variables
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", processInstance.getProcessInstanceId());
        response.put("tripId", tripId);
        response.put("status", "STARTED");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Map<String, Object>>> getTasks() {
        List<Task> tasks = taskService.createTaskQuery().list();
        
        List<Map<String, Object>> taskList = tasks.stream().map(task -> {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("taskId", task.getId());
            taskMap.put("taskName", task.getName());
            taskMap.put("processInstanceId", task.getProcessInstanceId());
            taskMap.put("assignee", task.getAssignee());
            
            Map<String, Object> variables = taskService.getVariables(task.getId());
            taskMap.put("tripId", variables.get("tripId"));
            taskMap.put("userId", variables.get("userId"));
            
            return taskMap;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(taskList);
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Map<String, String>> completeTask(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> variables) {
        
        taskService.complete(taskId, variables);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "COMPLETED");
        response.put("taskId", taskId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/message/booking-confirmation")
    public ResponseEntity<Map<String, String>> sendBookingConfirmation(@RequestBody Map<String, Object> request) {
        String processInstanceId = (String) request.get("processInstanceId");
        Boolean bookingConfirmed = (Boolean) request.get("bookingConfirmed");
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("bookingConfirmed", bookingConfirmed);
        
        runtimeService.createMessageCorrelation("BookingConfirmationMessage")
            .processInstanceId(processInstanceId)
            .setVariables(variables)
            .correlate();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "MESSAGE_SENT");
        response.put("processInstanceId", processInstanceId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/process/{processInstanceId}/status")
    public ResponseEntity<Map<String, Object>> getProcessStatus(@PathVariable String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
        
        Map<String, Object> response = new HashMap<>();
        
        if (processInstance == null) {
            response.put("status", "COMPLETED_OR_NOT_FOUND");
        } else {
            response.put("status", "ACTIVE");
            response.put("processInstanceId", processInstance.getProcessInstanceId());
            
            Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
            response.put("variables", variables);
            
            List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
            
            if (!tasks.isEmpty()) {
                response.put("currentTask", tasks.get(0).getName());
            }
        }
        
        return ResponseEntity.ok(response);
    }
}
