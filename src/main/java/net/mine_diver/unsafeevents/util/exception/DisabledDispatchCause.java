package net.mine_diver.unsafeevents.util.exception;

import net.mine_diver.unsafeevents.EventBusController;
import net.mine_diver.unsafeevents.ManagedEventBus;
import org.jetbrains.annotations.NotNull;

/**
 * Created and stored when event dispatch gets disabled for a {@link ManagedEventBus}.
 *
 * <p>
 *     Used as a cause for {@link IllegalDispatchException},
 *     providing additional information, such as the stack trace
 *     of {@link EventBusController#disableDispatch(String)}, which led to the
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
