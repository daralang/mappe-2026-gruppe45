package edu.ntnu.idatt2003.millions.service.toast;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Thin publisher that delivers toast notifications to registered listeners.
 *
 * <p>Has no JavaFX dependency so it can be unit-tested without the toolkit.
 * Callers are responsible for passing already-translated message strings.</p>
 */
public class ToastService {

    private final List<Consumer<Toast>> listeners = new ArrayList<>();
    private long nextId = 1;

    public void show(String message, ToastType type) {
        Toast toast = new Toast(nextId++, type, message);
        listeners.forEach(l -> l.accept(toast));
    }

    public void addListener(Consumer<Toast> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<Toast> listener) {
        listeners.remove(listener);
    }
}
