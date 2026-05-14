package edu.ntnu.idatt2003.millions.model.notification;

import java.util.List;

public record Notification(
        int id,
        Severity severity,
        String titleKey,
        String bodyKey,
        List<String> bodyArgs,
        int week,
        boolean read
) {
    public enum Severity {
        SEVERE,
        WARNING,
        MILESTONE,
        INFO
    }

    public Notification markAsRead() {
        return new Notification(id, severity, titleKey, bodyKey, bodyArgs, week, true);
    }
}
