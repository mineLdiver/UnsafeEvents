package net.mine_diver.unsafeevents;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The dead event carries an event that was dispatched
 * but didn't have listeners in the bus.
 *
 * @author mine_diver
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeadEvent extends Event {

    /**
     * The constant instance of the event.
     */
    static final DeadEvent INSTANCE = new DeadEvent();

    /**
     * The event ID.
     */
    public static final int ID = nextID();

    /**
     * The event that didn't have listeners.
     */
    Event event;

    /**
     * Returns the event that was dispatched
     * but didn't have listeners in the bus.
     *
     * @return the event that was dispatched but didn't have listeners in the bus.
     */
    public @NotNull Event getEvent() {
        return Objects.requireNonNull(event);
    }

    /**
     * Freeing the event instance after dispatch, so it can be GC'd.
     */
    @Override
    protected void finish() {
        event = null;
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
