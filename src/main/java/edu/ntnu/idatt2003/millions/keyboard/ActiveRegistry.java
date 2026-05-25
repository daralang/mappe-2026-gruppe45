package edu.ntnu.idatt2003.millions.keyboard;

import java.util.function.Consumer;

/**
 * Generic single-active-provider registry.
 *
 * <p>Holds at most one active provider of type {@code T} at a time.
 * Callers set the active provider via {@link #setActive(Object)} and
 * trigger behaviour on it via {@link #ifActive(Consumer)}.
 * Passing {@code null} to {@link #setActive} clears the active provider.</p>
 *
 * @param <T> the type of provider this registry holds
 */
public final class ActiveRegistry<T> {

    private T active;

    /**
     * Registers the given provider as the currently active one,
     * replacing any previous registration.
     * Pass {@code null} to clear the active provider.
     *
     * @param provider the provider to activate, or {@code null}
     */
    public void setActive(T provider) {
        this.active = provider;
    }

    /**
     * Runs {@code action} on the active provider if one is registered.
     * Does nothing when no provider is active.
     *
     * @param action the operation to perform on the active provider
     */
    public void ifActive(Consumer<T> action) {
        if (active != null) {
            action.accept(active);
        }
    }
}
