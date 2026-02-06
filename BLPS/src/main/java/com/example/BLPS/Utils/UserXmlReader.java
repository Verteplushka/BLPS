package com.example.BLPS.Utils;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;

public class UserXmlReader {

    private final Document xmlDocument;

    public UserXmlReader(String xmlFilePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            xmlDocument = builder.parse(new File(xmlFilePath));
            xmlDocument.getDocumentElement().normalize();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке XML файла: " + e.getMessage(), e);
        }
    }

    public int getUserIdByUsername(String username) {
        NodeList userList = xmlDocument.getElementsByTagName("user");

        for (int i = 0; i < userList.getLength(); i++) {
            Node node = userList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element userElement = (Element) node;
                String attrUsername = userElement.getAttribute("username");
                if (username.equals(attrUsername)) {
                    return Integer.parseInt(userElement.getAttribute("id"));
                }
            }
        }

        throw new IllegalArgumentException("Пользователь с именем '" + username + "' не найден.");
    }

    public String getUserRoleByUsername(String username) {
        NodeList userList = xmlDocument.getElementsByTagName("user");

        for (int i = 0; i < userList.getLength(); i++) {
            Node node = userList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element userElement = (Element) node;
                String attrUsername = userElement.getAttribute("username");
                if (username.equals(attrUsername)) {
                    return userElement.getAttribute("role");
                }
            }
        }

        throw new IllegalArgumentException("Пользователь с именем '" + username + "' не найден.");
    }

}

