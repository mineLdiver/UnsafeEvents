package net.mine_diver.unsafeevents.eventbus;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
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
 * @see Controller
 * @author mine_diver
 */
@FieldDefaults(
        level = AccessLevel.PROTECTED,
        makeFinal = true
)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ManagedEventBus extends EventBus {
    /**
     * Creates a new {@linkplain ManagedEventBus managed event bus}
     * and a new {@linkplain Controller event bus controller} attached to it.
     *
     * <p>
     *     This is used for hiding the controller methods
     *     in a separate interface.
     * </p>
     *
     * @return a new managed event bus and an event bus controller attached to it.
     */
    public static @NotNull WithController create() {
        val controller = new ControllerImpl();
        return new WithController(new ManagedEventBus(controller), controller);
    }

    @NotNull ControllerImpl controller;

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
        if (controller.dispatchDisabled) throw new IllegalDispatchException("Attempted to dispatch event " + event.getClass().getName() + " when dispatch is disabled!", controller.disabledDispatchCause);
        return super.post(event);
    }

    /**
     * Used for returning both the new managed event bus and
     * the event bus controller attached to it
     * in a factory method.
     *
     * @param eventBus the new managed event bus.
     * @param eventBusController the controller attached to the new event bus.
     * @author mine_diver
     */
    public record WithController(@NotNull ManagedEventBus eventBus, @NotNull ManagedEventBus.Controller eventBusController) {}

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
    public interface Controller {
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

    /**
     * Controller implementation for default managed event bus.
     *
     * @author mine_diver
     */
    @FieldDefaults(level = AccessLevel.PROTECTED)
    protected static class ControllerImpl implements Controller {
        /**
         * Whether dispatch is currently disabled.
         * False by default.
         */
        boolean dispatchDisabled;

        /**
         * A throwable containing a stack trace of {@link Controller#disableDispatch(String)}
         * as a cause for {@link IllegalDispatchException}, so debugging
         * an illegal dispatch is easier.
         */
        DisabledDispatchCause disabledDispatchCause;

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
        @Override
        public void disableDispatch(final @NotNull String reason) {
            dispatchDisabled = true;
            disabledDispatchCause = new DisabledDispatchCause(reason);
        }

        /**
         * Re-enables dispatch and clears disable stack trace.
         */
        @Override
        public void enableDispatch() {
            dispatchDisabled = false;
            disabledDispatchCause = null;
        }
    }
}
