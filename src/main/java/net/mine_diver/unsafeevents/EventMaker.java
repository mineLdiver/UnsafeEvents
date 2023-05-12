package net.mine_diver.unsafeevents;

import org.jetbrains.annotations.NotNull;

/**
 * Functional interface that indicates that the class implementing it
 * is an event maker and can decorate the event with data to dispatch it.
 *
 * @param <T> the type of event that the maker decorates.
 * @author mine_diver
 */
@FunctionalInterface
public interface EventMaker<T extends Event> {
    /**
     * Returns the decorated event instance.
     *
     * @return the decorated event instance.
     */
    @NotNull T make();
}
