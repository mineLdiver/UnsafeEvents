package net.mine_diver.unsafeevents;

import net.mine_diver.unsafeevents.listener.CompositeListener;
import net.mine_diver.unsafeevents.listener.GenericListener;
import net.mine_diver.unsafeevents.listener.Listener;
import net.mine_diver.unsafeevents.listener.SingularListener;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event dispatcher whose listeners scope
 * can be mutated with the defined methods.
 *
 * @author mine_diver
 */
public interface MutableEventBus extends EventDispatcher {
    /**
     * Registers a generic listener to this event bus's scope.
     *
     * @param listener the listener to register.
     * @see Listener
     * @see CompositeListener
     * @see SingularListener
     */
    default void register(final @NotNull GenericListener listener) {
        listener.accept(this::register);
    }

    /**
     * Registers a singular listener to this event bus's scope.
     *
     * @param listener the listener to register.
     * @param <EVENT> the event type the listener accepts.
     * @see Listener
     * @see SingularListener
     */
    <EVENT extends Event> void register(final @NotNull SingularListener<@NotNull EVENT> listener);

    /**
     * Unregisters a generic listener from this event bus's scope.
     *
     * <p>
     *     The listener instance has to be the same one
     *     that was originally registered in this event bus.
     * </p>
     *
     * @param listener the listener to unregister.
     */
    default void unregister(final @NotNull GenericListener listener) {
        listener.accept(this::unregister);
    }

    /**
     * Unregisters a singular listener from this event bus's scope.
     *
     * <p>
     *     The listener instance has to be the same one
     *     that was originally registered in this event bus.
     * </p>
     *
     * @param listener the listener to unregister.
     * @param <EVENT> the event type the listener accepts.
     */
    <EVENT extends Event> void unregister(final @NotNull SingularListener<@NotNull EVENT> listener);
}
