package net.mine_diver.unsafeevents;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.mine_diver.unsafeevents.util.UnsafeProvider;
import net.mine_diver.unsafeevents.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The basic abstract event class.
 *
 * <p>
 *     Every event type must implement {@link Event#getEventID()} that returns
 *     a constant ID of the event obtained through invoking
 *     {@link Event#NEXT_ID}'s {@link AtomicInteger#incrementAndGet()} method once
 *     and storing the value in a static final field.
 * </p>
 *
 * <p>
 *     An event type can declare itself as cancelable by overriding
 *     {@link Event#isCancelable()} and returning true.
 * </p>
 *
 * <p>
 *     An instance of an event type can carry any data in the fields
 *     and have any method declared.
 * </p>
 *
 * <p>
 *     An event type can be either created via a constructor each time it's dispatched,
 *     or store a constant instance of itself and use an {@link EventMaker}
 *     to set data to the instance and then dispatch the decorated instance.
 *     The first way is easy to use and allows for final fields,
 *     but adds work to GC due to creating new instances on each dispatch.
 *     The second way is harder to use, requires to use getters for fields
 *     that are supposed to be immutable, and requires to clear the constant instance
 *     after each dispatch with an {@link Event#finish()} implementation,
 *     but saves GC from having to clear up its instances after each dispatch.
 * </p>
 *
 * @see EventBus
 * @author mine_diver
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Event {

    /**
     * The next ID {@link AtomicInteger} field.
     *
     * <p>
     *     Used for obtaining an ID by event types through {@link AtomicInteger#incrementAndGet()}.
     * </p>
     *
     * @see Event#getEventID()
     */
    @NotNull
    protected static final AtomicInteger NEXT_ID = new AtomicInteger();

    /**
     * Global event type to event ID lookup,
     *
     * @see Event#getEventID(Class)
     */
    @NotNull
    private static final Reference2IntMap<Class<? extends Event>> EVENT_ID_LOOKUP = Util.make(new Reference2IntOpenHashMap<>(), map -> map.defaultReturnValue(-1));

    /**
     * Returns the event ID of the specified event type from {@link Event#EVENT_ID_LOOKUP}.
     *
     * <p>
     *     If the event ID of this event type is absent in the global lookup,
     *     allocates a dummy instance of this event type, retrieves the ID
     *     using {@link Event#getEventID()}, and stores it in the lookup.
     * </p>
     *
     * @param eventType the event type class of which the ID must be returned.
     * @return the ID of the specified event type.
     * @param <T> the event type.
     */
    public static <T extends Event> int getEventID(
            final @NotNull Class<T> eventType
    ) {
        return EVENT_ID_LOOKUP.computeIfAbsent(eventType, aClass -> {
            try {
                //noinspection unchecked
                return ((T) UnsafeProvider.theUnsafe.allocateInstance(eventType)).getEventID();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Whether the event type is cancelable.
     */
    @Getter
    private final boolean cancelable = false;

    /**
     * Whether the event is currently canceled.
     */
    @Getter
    private boolean canceled;

    /**
     * Sets whether the event is canceled.
     *
     * @param canceled whether the event is canceled or resumed.
     * @throws UnsupportedOperationException if the event type isn't cancelable.
     */
    public void setCanceled(final boolean canceled) {
        if (isCancelable()) this.canceled = canceled;
        else throw new UnsupportedOperationException(String.format("Trying to cancel a not cancellable event! (%s)", getClass().getName()));
    }

    /**
     * Cancels the event.
     *
     * @throws UnsupportedOperationException if the event type isn't cancelable.
     */
    public final void cancel() {
        setCanceled(true);
    }

    /**
     * Resumes the event.
     *
     * @throws UnsupportedOperationException if the event type isn't cancelable.
     */
    public final void resume() {
        setCanceled(false);
    }

    /**
     * Called after the event's dispatch
     * to perform post-dispatch actions,
     * such as event parameters clean up.
     */
    protected void finish() {}

    /**
     * Returns the constant event ID obtained through {@link Event#NEXT_ID}.
     *
     * <p>
     *     Must be constant for each event type,
     *     otherwise, the behavior is unpredictable.
     * </p>
     *
     * @return the constant event ID obtained through {@link Event#NEXT_ID}.
     */
    protected abstract int getEventID();
}
