package org.example;

import jakarta.resource.ResourceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
                "summary": "Модерация приложения #%d: %s",
                "description": "Приложение: %s\\nРазработчик: %s\\nID: %d",
                "issuetype": {
                  "name": "Task"
                }
              }
            }
            """.formatted(appId, appName, appName, developer, appId);

            URL url = new URL(jiraBaseUrl + "/rest/api/2/issue");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
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

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

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
    public void completeTask(int appId) throws ResourceException {
        try {
            String jql = URLEncoder.encode(
                    String.format("project=MOD AND description ~ \"%d\"", appId),
                    StandardCharsets.UTF_8
            );

            URL searchUrl = new URL(jiraBaseUrl + "/rest/api/2/search?jql=" + jql + "&fields=key");
            HttpURLConnection searchConn = (HttpURLConnection) searchUrl.openConnection();
            searchConn.setRequestMethod("GET");

            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            searchConn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            searchConn.setRequestProperty("Content-Type", "application/json");

            int searchCode = searchConn.getResponseCode();
            if (searchCode != 200) {
                throw new ResourceException("Не удалось найти задачу для appId=" + appId + ". Код: " + searchCode);
            }

            String taskId;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(searchConn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String json = response.toString();
                int keyIndex = json.indexOf("\"key\":\"");
                if (keyIndex != -1) {
                    int start = keyIndex + 7;
                    int end = json.indexOf("\"", start);
                    taskId = json.substring(start, end);
                } else {
                    throw new ResourceException("Задача с appId=" + appId + " не найдена в Jira");
                }
            }

            URL transitionsUrl = new URL(jiraBaseUrl + "/rest/api/2/issue/" + taskId + "/transitions");
            HttpURLConnection conn = (HttpURLConnection) transitionsUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new ResourceException("Не удалось получить переходы для задачи " + taskId + ". Код: " + responseCode);
            }

            String transitionId = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String json = response.toString();
                int index = json.indexOf("\"name\":\"Done\"");
                if (index != -1) {
                    int idIndex = json.lastIndexOf("\"id\":\"", index);
                    if (idIndex != -1) {
                        int start = idIndex + 6;
                        int end = json.indexOf("\"", start);
                        transitionId = json.substring(start, end);
                    }
                }
            }

            if (transitionId == null) {
                throw new ResourceException("Не найден переход в статус 'Done' для задачи " + taskId);
            }

            URL completeUrl = new URL(jiraBaseUrl + "/rest/api/2/issue/" + taskId + "/transitions");
            HttpURLConnection completeConn = (HttpURLConnection) completeUrl.openConnection();
            completeConn.setRequestMethod("POST");
            completeConn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            completeConn.setRequestProperty("Content-Type", "application/json");
            completeConn.setDoOutput(true);

            String payload = """
            {
              "transition": {
                "id": "%s"
              }
            }
            """.formatted(transitionId);

            try (OutputStream os = completeConn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int completeResponseCode = completeConn.getResponseCode();
            if (completeResponseCode != 204) {
                throw new ResourceException("Ошибка при завершении задачи " + taskId + ". Код: " + completeResponseCode);
            }

        } catch (IOException e) {
            throw new ResourceException("Ошибка при завершении задачи для appId=" + appId, e);
        }
    }


    @Override
    public void close() {
        System.out.println("Closing Jira connection");
    }
}
