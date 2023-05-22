package net.mine_diver.unsafeevents.listener;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

/**
 * A simple record implementation of {@link CompositeListener}.
 *
 * @param subListeners an immutable list of sub-listeners.
 * @param phase the default phase for sub-listeners.
 * @param priority the default priority for sub-listeners.
 * @author mine_diver
 */
public record SimpleCompositeListener(
        @NotNull ImmutableList<@NotNull SingularListener<?>> subListeners,
        @NotNull String phase,
        int priority
) implements CompositeListener {}
