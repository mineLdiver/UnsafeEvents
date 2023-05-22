package net.mine_diver.unsafeevents.util.exception;

import net.mine_diver.unsafeevents.eventbus.ManagedEventBus;
import org.jetbrains.annotations.NotNull;

/**
 * Signals that an event dispatch has been invoked at an illegal or inappropriate time.
 *
 * <p>
 *     An example of such illegal state is a {@link ManagedEventBus}
 *     with dispatch disabled through {@link ManagedEventBus.Controller#disableDispatch(String)}.
 * </p>
 *
 * @see DisabledDispatchCause
 * @author mine_diver
 */
public class IllegalDispatchException extends IllegalStateException {
    public IllegalDispatchException(final @NotNull String message, final @NotNull Throwable cause) {
        super(message, cause);
    }
}
