package com.example.BLPS.security;

import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.security.Principal;
import java.util.Set;

public class JaasAuthorityGranter implements AuthorityGranter {

    private final String xmlFilePath;

    public JaasAuthorityGranter(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    @Override
    public Set<String> grant(Principal principal) {
        String username = principal.getName();

        try {
            File file = new File(xmlFilePath);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(file);
            NodeList userList = doc.getElementsByTagName("user");

            for (int i = 0; i < userList.getLength(); i++) {
                Element user = (Element) userList.item(i);
                if (user.getAttribute("username").equals(username)) {
                    String role = user.getAttribute("role");

                    return mapRoleToPrivileges(role);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Set.of();
    }

    private Set<String> mapRoleToPrivileges(String role) {
        return switch (role) {
            case "USER" -> Set.of("ROLE_USER");
            case "DEVELOPER" -> Set.of("ROLE_DEVELOPER");
            case "ADMIN" -> Set.of("ROLE_ADMIN");
            default -> Set.of();
        };
    }
}
