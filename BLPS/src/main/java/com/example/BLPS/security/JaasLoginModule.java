package com.example.BLPS.security;


import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.security.Principal;
import java.util.Map;

@Slf4j
public class JaasLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private String username;
    private String password;
    private String xmlFilePath;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.xmlFilePath = (String) options.get("xmlFilePath");
    }


    @Override
    public boolean login() throws LoginException {
        // получить логин и пароль
        NameCallback nameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        } catch (Exception e) {
            throw new LoginException("Failed to get credentials");
        }

        username = nameCallback.getName();
        password = new String(passwordCallback.getPassword());

        try {
            File file = new File(xmlFilePath);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(file);
            NodeList userList = doc.getElementsByTagName("user");

            for (int i = 0; i < userList.getLength(); i++) {
                Element user = (Element) userList.item(i);
                if (user.getAttribute("username").equals(username) &&
                        user.getAttribute("password").equals(password)) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new LoginException("Error reading users.xml");
        }

        throw new FailedLoginException("Invalid credentials");
    }

    @Override
    public boolean commit() {
        subject.getPrincipals().add(new UserPrincipal(username));
        return true;
    }

    @Override public boolean abort() { return false; }
    @Override public boolean logout() { return true; }

    public static class UserPrincipal implements Principal {
        private final String name;

        public UserPrincipal(String name) {
            this.name = name;
        }

        @Override public String getName() {
            return name;
        }
    }
}

