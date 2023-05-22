package net.mine_diver.unsafeevents.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.mine_diver.unsafeevents.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Signals that an event type's phase ordering
 * was changed by a third party and event
 * buses should use the new ordering.
 *
 * @see PhaseOrdering#addPhaseOrdering(String, String)
 * @author mine_diver
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PhaseOrderingInvalidationEvent extends Event {
    /**
     * The constant instance of the event.
     */
    static final @NotNull PhaseOrderingInvalidationEvent INSTANCE = new PhaseOrderingInvalidationEvent();

    /**
     * The event ID.
     */
    public static final int ID = nextID();

    /**
     * The event type whose phase ordering was invalidated.
     */
    Class<? extends Event> eventType;

    /**
     * Returns the event type whose
     * event ordering was invalidated.
     *
     * @return the event type whose phase ordering was invalidated.
     */
    public @NotNull Class<? extends Event> getEventType() {
        return Objects.requireNonNull(eventType);
    }

    /**
     * Freeing the event type instance after dispatch.
     */
    @Override
    protected void finish() {
        eventType = null;
    }

    /**
     * Returns the event ID.
     *
     * @return the event ID.
     */
    @Override
    protected int getEventID() {
        return ID;
    }
}
