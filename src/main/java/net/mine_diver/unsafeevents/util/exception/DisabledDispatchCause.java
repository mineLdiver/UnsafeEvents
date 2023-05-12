package net.mine_diver.unsafeevents.util.exception;

import net.mine_diver.unsafeevents.EventBus;
import org.jetbrains.annotations.NotNull;

/**
 * Created and stored when event dispatch gets disabled for an {@link EventBus}.
 *
 * <p>
 *     Used as a cause for {@link IllegalDispatchException},
 *     providing additional information, such as the stack trace
 *     of {@link EventBus#disableDispatch()}, which led to the
 *     illegal state in the first place.
 * </p>
 *
 * @see IllegalDispatchException
 * @author mine_diver
 */
public class DisabledDispatchCause extends Throwable {
    public DisabledDispatchCause(final @NotNull String message) {
        super(message);
    }
}
