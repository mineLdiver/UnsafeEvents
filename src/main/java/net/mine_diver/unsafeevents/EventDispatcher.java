package net.mine_diver.unsafeevents;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.mine_diver.unsafeevents.util.exception.DispatchException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event dispatcher with a listeners scope.
 *
 * @see MutableEventBus
 * @author mine_diver
 */
public interface EventDispatcher {
    /**
     * Event dispatch method.
     *
     * <p>
     *     Invokes all listeners of the specified event
     *     that are registered in the scope of this dispatcher.
     * </p>
     *
     * <p>
     *     After the dispatch, {@link Event#finish()} is executed,
     *     allowing the event to perform some post-dispatch action,
     *     for example, a clean up of event parameters.
     * </p>
     *
     * @param event the event to dispatch to this dispatcher's scope.
     * @param <EVENT> the event type.
     * @return the dispatched event.
     * @throws DispatchException if a listener throws during dispatch.
     */
    @Contract("_ -> param1")
    @CanIgnoreReturnValue
    default <EVENT extends Event> @NotNull EVENT post(final @NotNull EVENT event) {
        event.finish();
        return event;
    }
}
