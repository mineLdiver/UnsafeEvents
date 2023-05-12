package net.mine_diver.unsafeevents.util.exception;

import net.mine_diver.unsafeevents.EventBus;
import org.jetbrains.annotations.NotNull;

/**
 * Signals that an event dispatch has been invoked at an illegal or inappropriate time.
 *
 * <p>
 *     An example of such illegal state is an {@link EventBus}
 *     with dispatch disabled through {@link EventBus#disableDispatch()}.
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
