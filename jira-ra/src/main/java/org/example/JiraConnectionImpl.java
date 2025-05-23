package org.example;

import jakarta.resource.ResourceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JiraConnectionImpl implements JiraConnection {
    private final String jiraBaseUrl;
    private String username;
    private String password;


    public JiraConnectionImpl(String jiraBaseUrl, String username, String password) {
        this.jiraBaseUrl = jiraBaseUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public String createModerationTask(int appId, String appName, String developer) throws ResourceException {
        try {
            String json = """
                    {
                      "fields": {
                        "project": {
                          "key": "MOD"
                        },
                        "summary": "Краткое описание задачи",
                        "issuetype": {
                          "name": "Task"
                        }
                      }
                    }
                    """.formatted(appName, appId, developer);

            URL url = new URL(jiraBaseUrl + "/rest/api/2/issue");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 201) {
                String errorMessage;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    errorMessage = response.toString();
                } catch (IOException ioEx) {
                    errorMessage = "Не удалось прочитать тело ошибки: " + ioEx.getMessage();
                }
                throw new ResourceException("Jira вернула код: " + responseCode + ", сообщение: " + errorMessage);
            }


            // Чтение taskId из ответа
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Находим "key":"JIRA-123"
                String jsonResponse = response.toString();
                int keyIndex = jsonResponse.indexOf("\"key\":\"");
                if (keyIndex != -1) {
                    int start = keyIndex + 7;
                    int end = jsonResponse.indexOf("\"", start);
                    return jsonResponse.substring(start, end);
                } else {
                    throw new ResourceException("Не удалось найти ключ задачи в ответе Jira");
                }
            }
        } catch (IOException e) {
            throw new ResourceException("Ошибка при подключении к Jira", e);
        }
    }

    @Override
    public String getTaskStatus(String taskId) throws ResourceException {
        try {
            URL url = new URL(jiraBaseUrl + "/rest/api/2/issue/" + taskId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");


            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);


            conn.setRequestProperty("Content-Type", "application/json");
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new ResourceException("Jira вернула код: " + responseCode);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String jsonResponse = response.toString();
                int statusIndex = jsonResponse.indexOf("\"status\":{\"self\":");
                if (statusIndex != -1) {
                    int nameIndex = jsonResponse.indexOf("\"name\":\"", statusIndex);
                    if (nameIndex != -1) {
                        int start = nameIndex + 8;
                        int end = jsonResponse.indexOf("\"", start);
                        return jsonResponse.substring(start, end);
                    }
                }

                throw new ResourceException("Не удалось найти статус задачи в ответе Jira");
            }
        } catch (IOException e) {
            throw new ResourceException("Ошибка при получении статуса из Jira", e);
        }
    }

    @Override
    public void close() {
        System.out.println("Closing Jira connection");
    }
}
