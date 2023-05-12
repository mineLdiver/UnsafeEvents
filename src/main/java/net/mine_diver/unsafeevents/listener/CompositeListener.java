package net.mine_diver.unsafeevents.listener;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Listener that consists of multiple {@linkplain SingularListener singular listeners}.
 *
 * <p>
 *     Usually used for representing a class or an object
 *     with multiple {@link EventListener} annotated methods.
 * </p>
 *
 * <p>
 *     {@link #priority()} replaces the default
 *     priority for sub-listeners.
 * </p>
 *
 * @author mine_diver
 */
public interface CompositeListener extends GenericListener {
    /**
     * @return the sub-listeners this listener contains.
     */
    @NotNull ImmutableList<@NotNull SingularListener<?>> subListeners();

    /**
     * Accepts all sub-listeners to a consumer.
     *
     * @param consumer the listener consumer.
     */
    @Override
    default void accept(final @NotNull Consumer<@NotNull SingularListener<?>> consumer) {
        subListeners().forEach(consumer);
    }
}
