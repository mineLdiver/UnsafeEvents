package net.mine_diver.unsafeevents;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Contains the listener and the listener's priority
 * to sort it in the event bus.
 *
 * @param invoker the listener.
 * @param priority the listener's priority.
 * @author mine_diver
 */
record ListenerContainer(@NotNull Consumer<@NotNull Event> invoker, int priority) implements Comparable<ListenerContainer> {

    /**
     * Compares listener containers by priority in backwards order,
     * so that high priority comes first.
     *
     * @param o the container to be compared.
     * @return the comparison result.
     */
    @Override
    public int compareTo(final @NotNull ListenerContainer o) {
        return Integer.compare(o.priority, priority);
    }
}
