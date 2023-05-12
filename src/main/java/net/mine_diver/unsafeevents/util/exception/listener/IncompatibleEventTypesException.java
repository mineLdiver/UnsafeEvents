package net.mine_diver.unsafeevents.util.exception.listener;

import org.jetbrains.annotations.NotNull;

/**
 * Signals that the event that a listener is registered to
 * isn't a subclass of the listener's method argument type.
 *
 * @author mine_diver
 */
public class IncompatibleEventTypesException extends IllegalArgumentException {
    public IncompatibleEventTypesException(final @NotNull String message) {
        super(message);
    }
}