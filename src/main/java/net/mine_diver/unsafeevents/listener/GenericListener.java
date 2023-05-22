package net.mine_diver.unsafeevents.listener;

import net.mine_diver.unsafeevents.event.EventPhases;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Interface defining a generic listener.
 *
 * <p>
 *     Provides a way to interact with
 *     a listener with unknown implementation
 *     as if it was {@linkplain SingularListener singular}.
 * </p>
 *
 * @see SingularListener
 * @see CompositeListener
 * @author mine_diver
 */
public interface GenericListener {
    /**
     * @return listener's phase. Can be any string.
     * Ordering of phases is defined individually for each
     * event type. "default" is default.
     * @see EventPhases#DEFAULT_PHASE
     */
    default String phase() {
        return EventPhases.DEFAULT_PHASE;
    }

    /**
     * @return listener's priority. Can be any integer number.
     * High priority - early execution. 0 is default.
     * @see ListenerPriority
     * @see EventListener#DEFAULT_PRIORITY
     */
    default int priority() {
        return EventListener.DEFAULT_PRIORITY;
    }

    /**
     * Accepts current listener to a consumer
     * as a {@link SingularListener},
     * exposing the listener implementation.
     *
     * <p>
     *     The consumer might be executed multiple
     *     times depending on implementation.
     *     For example, {@link CompositeListener} executes
     *     the consumer for each sub-listener it has.
     * </p>
     *
     * @param consumer the listener consumer.
     */
    void accept(final @NotNull Consumer<@NotNull SingularListener<?>> consumer);
}
