package net.mine_diver.unsafeevents.listener;

import net.mine_diver.unsafeevents.Event;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A listener backed by a single consumer.
 *
 * @param <EVENT> the event type the backing consumer accepts.
 * @author mine_diver
 */
public interface SingularListener<EVENT extends Event> extends GenericListener {
    /**
     * @return the event type the backing consumer is listening to.
     */
    Class<@NotNull EVENT> eventType();
    /**
     * @return the backing consumer itself.
     */
    Consumer<@NotNull EVENT> listener();

    /**
     * Accepts this listener to a consumer.
     *
     * @param consumer the listener consumer.
     */
    @Override
    default void accept(final @NotNull Consumer<@NotNull SingularListener<?>> consumer) {
        consumer.accept(this);
    }
}
