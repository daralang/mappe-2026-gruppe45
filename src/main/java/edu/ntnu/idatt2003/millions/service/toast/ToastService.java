// Javadoc generated with AI assistance - reviewed and approved by author.
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

    /**
     * Creates a {@link Toast} with an auto-assigned ID and delivers it to all registered listeners.
     *
     * @param message the already-translated text to display
     * @param type    severity category of the notification
     */
    public void show(String message, ToastType type) {
        Toast toast = new Toast(nextId++, type, message);
        listeners.forEach(l -> l.accept(toast));
    }

    /**
     * Registers a listener to receive future toasts.
     *
     * @param listener the consumer to invoke when a toast is published
     */
    public void addListener(Consumer<Toast> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a previously registered listener; no-op if not registered.
     *
     * @param listener the consumer to remove
     */
    public void removeListener(Consumer<Toast> listener) {
        listeners.remove(listener);
    }
}
