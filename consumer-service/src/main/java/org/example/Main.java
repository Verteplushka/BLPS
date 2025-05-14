package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.example.Entity.Application;
import org.example.Entity.Status;

import java.sql.*;

public class Main {

    static final String BROKER_URL = "tcp://localhost:1884";
    static final String TOPIC = "moderation-queue";

    static final String JDBC_URL = "jdbc:postgresql://localhost:5432/studs";
    static final String JDBC_USER = "admin";
    static final String JDBC_PASS = "admin";

    public static void main(String[] args) throws Exception {
        MqttClient client = new MqttClient(BROKER_URL, MqttClient.generateClientId());
        client.connect();

        System.out.println("🎧 Subscribed to MQTT topic: " + TOPIC);

        client.subscribe(TOPIC, (topic, mqttMessage) -> {
            try {
                Integer appId = Integer.parseInt(new String(mqttMessage.getPayload()));

                Application app = getAppFromDatabase(appId);

                if (app != null) {
                    System.out.println("📥 Got app for moderation: " + app.getName());

                    boolean hasBadWords = containsBadWords(app.getName()) || containsBadWords(app.getDescription());

                    updateAppStatus(app.getId(), hasBadWords ? Status.AUTO_MODERATION_FAILED : Status.ADMIN_MODERATION);

                    System.out.println(hasBadWords ? "❌ Bad words detected" : "✅ App is clean");
                } else {
                    System.out.println("❌ App not found in DB");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static boolean containsBadWords(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("shit") || lower.contains("fuck") || lower.contains("nigger");
    }

    private static Application getAppFromDatabase(Integer appId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String sql = "SELECT * FROM applications WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, appId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Application app = new Application();
                app.setId(rs.getInt("id"));
                app.setName(rs.getString("name"));
                app.setDescription(rs.getString("description"));
                app.setStatus(Status.valueOf(rs.getString("moderation_status")));
                return app;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void updateAppStatus(Integer appId, Status status) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String sql = "UPDATE applications SET moderation_status = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status.name());
            ps.setInt(2, appId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
