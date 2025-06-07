package com.example.BLPS.Controllers;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    // Стартуем процесс модерирования вручную (например, по кнопке в UI)
    @PostMapping("/start-moderation")
    public ResponseEntity<Void> startModerationProcess(@RequestParam Long applicationId,
                                                       @RequestParam Integer developerId) {
        runtimeService.startProcessInstanceByKey("moderationProcess", Map.of(
                "applicationId", applicationId,
                "developerId", developerId
        ));
        return ResponseEntity.ok().build();
    }

    // Выполняем задачу администратора — ручной выбор действия (approve/reject/ban)
    @PostMapping("/complete-review/{taskId}")
    public ResponseEntity<Void> completeReviewTask(@PathVariable String taskId,
                                                   @RequestParam String action) {
        taskService.complete(taskId, Map.of("adminAction", action));
        return ResponseEntity.ok().build();
    }
}
