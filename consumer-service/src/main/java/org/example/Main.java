package org.example;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.example.Entity.Application;
import org.example.Entity.Status;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class Main {

    static final String BROKER_URL = "tcp://localhost:16161";
    static final String QUEUE_NAME = "queue.moderation-queue";

    static final String JDBC_URL = "jdbc:postgresql://localhost:5432/studs";
    static final String JDBC_USER = "admin";
    static final String JDBC_PASS = "admin";

    private static final List<String> BAD_WORDS = Arrays.asList(
            "shit", "fuck", "nigger", "asshole", "bitch", "cunt",
            "dick", "piss", "cock", "pussy", "faggot", "whore"
    );

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        javax.jms.Connection connection = connectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination queue = session.createQueue(QUEUE_NAME);

        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(message -> {
            if (message instanceof BytesMessage bytesMessage) {
                try {
                    byte[] data = new byte[(int) bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(data);
                    String text = new String(data);

                    Integer appId = Integer.parseInt(text);

                    Application app = getAppFromDatabase(appId);

                    if (app != null) {
                        System.out.println("📥 Got app: " + app.getName());

                        boolean hasBadWords = containsBadWords(app.getName()) || containsBadWords(app.getDescription());

                        updateAppStatus(app.getId(), hasBadWords ? Status.AUTO_MODERATION_FAILED : Status.ADMIN_MODERATION);

                        System.out.println(hasBadWords ? "❌ Bad words detected" : "✅ App is clean");
                    } else {
                        System.out.println("❌ App not found in DB");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("✅ JMS listener started. Waiting for messages...");
    }

    public static boolean containsBadWords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        return BAD_WORDS.stream().anyMatch(lowerText::contains);
    }
        // реализовать через список плохих слов +
        // jira или любой task-tracker +
        // задача попадает в task-tracker и висит там до тех пор пока админ не закончит модерацию, чтобы там изменялись статусы задачи +


    private static Application getAppFromDatabase(Integer appId) {
        try (java.sql.Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
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
        try (java.sql.Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
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
