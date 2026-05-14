package edu.ntnu.idatt2003.millions.model.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    @DisplayName("markAsRead returns a new instance with read set to true")
    void markAsRead_returnsNewInstanceWithReadFlag() {
        Notification original = new Notification(
                1, Notification.Severity.WARNING,
                "title.key", "body.key", List.of("arg1"), 5, false
        );

        Notification read = original.markAsRead();

        assertTrue(read.read());
        assertNotSame(original, read);
    }

    @Test
    @DisplayName("markAsRead preserves all other fields")
    void markAsRead_preservesOtherFields() {
        Notification original = new Notification(
                42, Notification.Severity.SEVERE,
                "title.key", "body.key", List.of("arg1", "arg2"), 7, false
        );

        Notification read = original.markAsRead();

        assertEquals(42, read.id());
        assertEquals(Notification.Severity.SEVERE, read.severity());
        assertEquals("title.key", read.titleKey());
        assertEquals("body.key", read.bodyKey());
        assertEquals(List.of("arg1", "arg2"), read.bodyArgs());
        assertEquals(7, read.week());
    }
}
