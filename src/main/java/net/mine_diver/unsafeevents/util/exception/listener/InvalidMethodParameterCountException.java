package net.mine_diver.unsafeevents.util.exception.listener;

import org.jetbrains.annotations.NotNull;

/**
 * Signals that a listener's method doesn't contain arguments
 * or contains more than 1.
 *
 * @author mine_diver
 */
public class InvalidMethodParameterCountException extends IllegalArgumentException {
    public InvalidMethodParameterCountException(final @NotNull String message) {
        super(message);
    }
}