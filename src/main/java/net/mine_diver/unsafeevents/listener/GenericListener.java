package net.mine_diver.unsafeevents.listener;

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
     * @return listener's priority. Can be any integer number.
     * High priority - early execution. 0 is default.
     * @see ListenerPriority
     * @see EventListener#DEFAULT_PRIORITY
     */
    int priority();

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
