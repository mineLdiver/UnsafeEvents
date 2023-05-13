package net.mine_diver.unsafeevents.util.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Signals that a listener threw an exception during an event dispatch.
 *
 * @author mine_diver
 */
public class DispatchException extends RuntimeException {
    public DispatchException(final @NotNull String message, final @NotNull Throwable cause) {
        super(message, cause);
    }
}
