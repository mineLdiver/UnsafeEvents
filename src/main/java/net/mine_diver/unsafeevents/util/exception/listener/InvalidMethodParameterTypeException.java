package net.mine_diver.unsafeevents.util.exception.listener;

import org.jetbrains.annotations.NotNull;

/**
 * Signals that a listener's method argument isn't an event.
 *
 * @author mine_diver
 */
public class InvalidMethodParameterTypeException extends IllegalArgumentException {
    public InvalidMethodParameterTypeException(final @NotNull String message) {
        super(message);
    }
}