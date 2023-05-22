package net.mine_diver.unsafeevents.listener;

import net.mine_diver.unsafeevents.Event;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A simple record implementation of {@link SingularListener}.
 *
 * @param eventType the event type the backing consumer is listening to.
 * @param listener the backing consumer itself.
 * @param phase listener's phase. Can be any string. Ordering of phases
 *              is defined individually for each event type.
 * @param priority listener's priority. Can be any integer number.
 *                 High priority - early execution. 0 is default.
 * @param <EVENT> the event type the backing consumer accepts.
 */
public record SimpleSingularListener<EVENT extends Event>(
        @NotNull Class<EVENT> eventType,
        @NotNull Consumer<@NotNull EVENT> listener,
        @NotNull String phase,
        int priority
) implements SingularListener<EVENT> {}
