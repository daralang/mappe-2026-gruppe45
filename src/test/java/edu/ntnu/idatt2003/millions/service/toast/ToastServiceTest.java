package edu.ntnu.idatt2003.millions.service.toast;

import edu.ntnu.idatt2003.millions.view.component.toast.Toast;
import edu.ntnu.idatt2003.millions.view.component.toast.ToastType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class ToastServiceTest {

    private ToastService service;

    @BeforeEach
    void setUp() {
        service = new ToastService();
    }

    @Nested
    @DisplayName("show")
    class Show {

        @Test
        @DisplayName("invokes all registered listeners")
        void show_invokesAllListeners() {
            List<Toast> received1 = new ArrayList<>();
            List<Toast> received2 = new ArrayList<>();
            service.addListener(received1::add);
            service.addListener(received2::add);

            service.show("Hello", ToastType.SUCCESS);

            assertEquals(1, received1.size());
            assertEquals(1, received2.size());
            assertEquals("Hello", received1.get(0).message());
        }

        @Test
        @DisplayName("assigns monotonically increasing ids")
        void show_assignsMonotonicallyIncreasingIds() {
            List<Toast> received = new ArrayList<>();
            service.addListener(received::add);

            service.show("First", ToastType.SUCCESS);
            service.show("Second", ToastType.ERROR);
            service.show("Third", ToastType.SUCCESS);

            assertEquals(3, received.size());
            assertTrue(received.get(0).id() < received.get(1).id());
            assertTrue(received.get(1).id() < received.get(2).id());
        }
    }

    @Nested
    @DisplayName("removeListener")
    class RemoveListener {

        @Test
        @DisplayName("stops receiving toasts after removal")
        void removeListener_stopsReceivingToasts() {
            List<Toast> received = new ArrayList<>();
            Consumer<Toast> listener = received::add;
            service.addListener(listener);

            service.show("Before removal", ToastType.SUCCESS);
            service.removeListener(listener);
            service.show("After removal", ToastType.ERROR);

            assertEquals(1, received.size());
            assertEquals("Before removal", received.get(0).message());
        }
    }
}
