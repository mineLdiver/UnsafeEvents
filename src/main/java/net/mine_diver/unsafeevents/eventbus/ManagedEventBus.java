package net.mine_diver.unsafeevents.eventbus;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.mine_diver.unsafeevents.Event;
import net.mine_diver.unsafeevents.EventBus;
import net.mine_diver.unsafeevents.util.exception.DisabledDispatchCause;
import net.mine_diver.unsafeevents.util.exception.DispatchException;
import net.mine_diver.unsafeevents.util.exception.IllegalDispatchException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * {@link EventBus} implementation that provides more
 * control over dispatch at a little efficiency cost.
 *
 * @author mine_diver
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
public class ManagedEventBus extends EventBus {
    /**
     * Whether dispatch is currently disabled.
     * False by default.
     */
    boolean dispatchDisabled;

    /**
     * A throwable containing a stack trace of {@link #disableDispatch(String)}
     * as a cause for {@link IllegalDispatchException}, so debugging
     * an illegal dispatch is easier.
     */
    DisabledDispatchCause disabledDispatchCause;

    /**
     * Disables dispatch.
     *
     * <p>
     *     Makes {@link #post(Event)} throw {@link IllegalDispatchException}
     *     with the cause being this method's stack trace.
     * </p>
     *
     * <p>
     *     Caller sensitive. Stores current stack trace, including caller class,
     *     for potential future reference if {@link #post(Event)}
     *     gets executed while disabled.
     * </p>
     *
     * <p>
     *     Useful for bulk registering listeners that aren't supposed
     *     to directly or indirectly dispatch an event during registration.
     * </p>
     *
     * @param reason the message used by {@link DisabledDispatchCause} for
     *               potential {@link IllegalDispatchException} throw.
     */
    public void disableDispatch(final @NotNull String reason) {
        dispatchDisabled = true;
        disabledDispatchCause = new DisabledDispatchCause(reason);
    }

    /**
     * Re-enables dispatch and clears disable stack trace.
     */
    public void enableDispatch() {
        dispatchDisabled = false;
        disabledDispatchCause = null;
    }

    /**
     * Managed dispatch.
     *
     * @param event the event to dispatch to this bus's listeners.
     * @return the dispatched event.
     * @param <EVENT> the event type.
     * @throws DispatchException if a listener throws during dispatch.
     * @throws IllegalDispatchException if dispatch was currently disabled.
     */
    @Override
    @Contract("_ -> param1")
    @CanIgnoreReturnValue
    public <EVENT extends Event> @NotNull EVENT post(final @NotNull EVENT event) {
        if (dispatchDisabled) throw new IllegalDispatchException(String.format(
                "Attempted to dispatch event %s when dispatch is disabled!",
                event.getClass().getName()
        ), disabledDispatchCause);
        return super.post(event);
    }
}
