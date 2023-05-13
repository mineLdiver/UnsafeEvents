package net.mine_diver.unsafeevents;

import net.mine_diver.unsafeevents.util.exception.DisabledDispatchCause;
import net.mine_diver.unsafeevents.util.exception.IllegalDispatchException;
import org.jetbrains.annotations.NotNull;

/**
 * Interface that provides additional control over {@link ManagedEventBus}
 * in a form that lets the creator of the event bus
 * encapsulate the access to itself.
 *
 * <p>
 *     Must only be obtained with the creation of
 *     a {@linkplain ManagedEventBus managed event bus},
 *     using factory methods such as
 *     {@link ManagedEventBus#create()}.
 * </p>
 *
 * @author mine_diver
 */
public interface EventBusController {
    /**
     * Disables dispatch.
     *
     * <p>
     *     Makes {@link EventBus#post(Event)} throw {@link IllegalDispatchException}
     *     with the cause being this method's stack trace.
     * </p>
     *
     * <p>
     *     Caller sensitive. Stores current stack trace, including caller class,
     *     for potential future reference if {@link EventBus#post(Event)}
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
    void disableDispatch(final @NotNull String reason);

    /**
     * Re-enables dispatch and clears disable stack trace.
     */
    void enableDispatch();
}
