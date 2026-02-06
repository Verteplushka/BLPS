package com.example.BLPS.Exceptions;

public class ApplicationNotPendingModerationException extends RuntimeException {
    public ApplicationNotPendingModerationException(Long appId) {
        super("Application with ID " + appId + " is not pending moderation.");
    }
}
