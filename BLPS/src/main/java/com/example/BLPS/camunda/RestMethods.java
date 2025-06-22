package com.example.BLPS.camunda;

import com.example.BLPS.Dto.ApplicationDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RestMethods {

    private final RestTemplate restTemplate;
    private final String CAMUNDA_BASE_URL = "http://localhost:8085/engine-rest";

    public String startProcess(String url, Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        return startProcessWithVariables(url, variables);
    }

    public String startProcessWithVariables(String url, Map<String, Object> variables) {
        // Преобразуем переменные в формат Camunda
        Map<String, Map<String, Object>> camundaVars = variables.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Map.of("value", e.getValue())
                ));

        // Запускаем процесс
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url + "/start",
                Map.of("variables", camundaVars),
                Map.class
        );

        return (String) response.getBody().get("id");
    }

    public void completeTask(String processInstanceId, Map<String, Object> variables) {
        // 1. Находим задачу
        ResponseEntity<List<Map>> tasksResponse = restTemplate.exchange(
                CAMUNDA_BASE_URL + "/task?processInstanceId=" + processInstanceId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map>>() {
                }
        );

        // 2. Завершаем все найденные задачи
        tasksResponse.getBody().forEach(task -> {
            String taskId = (String) task.get("id");

            // Преобразуем переменные
            Map<String, Map<String, Object>> camundaVars = variables.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Map.of("value", e.getValue())
                    ));

            restTemplate.postForEntity(
                    CAMUNDA_BASE_URL + "/task/" + taskId + "/complete",
                    Map.of("variables", camundaVars),
                    Void.class
            );
        });
    }

    public Map<String, Object> startProcessAndWaitForResult(String url, Map<String, Object> inputVariables) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. Стартуем процесс
            String processInstanceId = startProcess(url, inputVariables);
            result.put("processInstanceId", processInstanceId);

            // 2. Выполняем таску и ждём результат
            return completeTaskAndWaitForResult(processInstanceId, null);

        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            return result;
        }
    }

    public Map<String, Object> completeTaskAndWaitForResult(String processInstanceId, Map<String, Object> taskVariables) {
        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", processInstanceId);

        try {
            // 1. Завершаем таску
            Map<String, Object> variables = new HashMap<>();
            if (taskVariables != null) {
                variables.putAll(taskVariables);
            }
            variables.put("By", "admin");
            variables.put("Date", new Date().toString());
            completeTask(processInstanceId, variables);

            // 2. Ожидаем результат
            int retries = 30;
            int delayMs = 500;

            for (int i = 0; i < retries; i++) {
                Thread.sleep(delayMs);

                try {
                    // Проверяем состояние процесса
                    ResponseEntity<Map> historyResponse = restTemplate.getForEntity(
                            CAMUNDA_BASE_URL + "/history/process-instance/" + processInstanceId,
                            Map.class
                    );

                    Map<String, Object> history = historyResponse.getBody();
                    if (history != null && history.get("state") != null) {
                        String state = (String) history.get("state");

                        if ("COMPLETED".equalsIgnoreCase(state)) {
                            // Проверяем инциденты
                            ResponseEntity<List<Map<String, Object>>> incResp = restTemplate.exchange(
                                    CAMUNDA_BASE_URL + "/history/incident?processInstanceId=" + processInstanceId,
                                    HttpMethod.GET,
                                    null,
                                    new ParameterizedTypeReference<>() {
                                    }
                            );

                            List<Map<String, Object>> incidents = incResp.getBody();
                            if (incidents != null && !incidents.isEmpty()) {
                                Map<String, Object> incident = incidents.get(0);
                                result.put("status", "FAILED");
                                result.put("error", incident.get("incidentMessage"));
                                result.put("incidentType", incident.get("incidentType"));
                            } else {
                                result.put("status", "COMPLETED");
                                result.put("message", "Process completed successfully");
                            }
                            return result;
                        }

                        // Неожиданный статус
                        result.put("status", state);
                        result.put("message", "Unexpected process state: " + state);
                        return result;
                    }

                } catch (HttpClientErrorException.NotFound ignored) {
                    // Продолжаем ждать
                } catch (Exception e) {
                    result.put("status", "FAILED");
                    result.put("error", e.getMessage());
                    return result;
                }
            }

            result.put("status", "TIMEOUT");
            result.put("error", "No result after waiting");

        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
        }

        return result;
    }

    public Object getVariableByProcessId(String processInstanceId, String variableName) {
        int retries = 30;
        int delayMs = 500;

        for (int i = 0; i < retries; i++) {
            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(
                        CAMUNDA_BASE_URL + "/process-instance/" + processInstanceId + "/variables/" + variableName,
                        Map.class
                );

                Map<String, Object> varData = response.getBody();
                if (varData != null && varData.containsKey("value")) {
                    return varData.get("value");
                }

            } catch (HttpClientErrorException.NotFound e) {
                // переменная ещё не появилась — ждём и повторим
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch variable: " + e.getMessage(), e);
            }

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return null; // по истечении ожидания переменная не появилась
    }

    public void setParams(String processInstanceId, Map<String, Object> params){
        params.forEach((key, value) -> {
            Map<String, Object> var = Map.of("value", value, "type", value instanceof Boolean ? "Boolean" : "String");
            restTemplate.postForEntity(
                    "http://localhost:8085/engine-rest/process-instance/" + processInstanceId + "/variables/" + key,
                    var,
                    Void.class
            );
        });
    }
}
