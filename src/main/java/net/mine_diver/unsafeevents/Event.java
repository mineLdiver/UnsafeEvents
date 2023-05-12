package net.mine_diver.unsafeevents;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.mine_diver.unsafeevents.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;

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
 *     There's also a way that combines features from both, which is using {@link SuperBuilder}
 *     from lombok. The advantages are easier to create and nicer looking event classes,
 *     which is one of the benefits of the first method mentioned,
 *     and the ability to add fields to the event in future without breaking constructor
 *     signatures, which is an advantage of the second method,
 *     but it also provides a disadvantage of having to create
 *     a new object each time the event is dispatched,
 *     but most of the time it's even more efficient
 *     than looking the object up from a cache.
 * </p>
 *
 * @see EventBus
 * @author mine_diver
 */
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Event {
    /**
     * Event ID counter.
     *
     * <p>
     *     Incremented for each event class.
     * </p>
     *
     * <p>
     *     Acts as a fast lookup key for event listeners during {@link EventBus#post(Event)}.
     * </p>
     *
     * @see #nextID()
     * @see #getEventID()
     * @see #getEventID(Class)
     */
    @NotNull
    private static final AtomicInteger NEXT_ID = new AtomicInteger();

    /**
     * Global event type to event ID lookup,
     *
     * @see Event#getEventID(Class)
     */
    @NotNull
    private static final Reference2IntMap<Class<? extends Event>> EVENT_ID_LOOKUP = Util.make(new Reference2IntOpenHashMap<>(), map -> map.defaultReturnValue(-1));

    @NotNull
    private static final ToIntFunction<Class<? extends Event>> ID_GENERATOR = eventClass -> NEXT_ID.incrementAndGet();

    /**
     * Returns the event ID of the specified event type from {@link Event#EVENT_ID_LOOKUP}.
     *
     * <p>
     *     If the event ID of this event type is absent in the global lookup,
     *     allocates a dummy instance of this event type, retrieves the ID
     *     using {@link Event#getEventID()}, and stores it in the lookup.
     * </p>
     *
     * @param eventClass the event class of which the ID must be returned.
     * @return the ID of the specified event type.
     * @param <T> the event type.
     */
    public static <T extends Event> int getEventID(
            final @NotNull Class<T> eventClass
    ) {
        return EVENT_ID_LOOKUP.computeIfAbsent(eventClass, ID_GENERATOR);
    }

    /**
     * Caller sensitive, and thus protected, version of {@link #getEventID(Class)}.
     *
     * <p>
     *     Main purpose of this method is to get the ID of the caller event
     *     so it can be cached in a final field and then be returned
     *     in a {@link #getEventID()} override to minimize the event ID lookup overhead.
     * </p>
     *
     * @return generated, or cached ID of the caller event.
     */
    protected static int nextID() {
        Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        if (!Event.class.isAssignableFrom(callerClass)) throw new IllegalCallerException();
        return getEventID(callerClass.asSubclass(Event.class));
    }

    /**
     * Whether the event type is cancelable.
     */
    @Getter
    private final boolean cancelable = false;

    /**
     * Whether the event is currently canceled.
     */
    @NotNull
    private final AtomicBoolean canceled = new AtomicBoolean();

    /**
     * Returns whether the event is currently canceled.
     *
     * @return whether the event is currently canceled.
     */
    public boolean isCanceled() {
        return canceled.get();
    }

    /**
     * Sets whether the event is canceled.
     *
     * @param canceled whether the event is canceled or resumed.
     * @throws UnsupportedOperationException if the event type isn't cancelable.
     */
    public void setCanceled(final boolean canceled) {
        if (isCancelable()) this.canceled.set(canceled);
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
     * Returns the event's ID.
     *
     * <p>
     *     This method exists and is inheritable to allow for
     *     events to cache their IDs using {@link #nextID()}
     *     in order to prevent map lookup when dispatching.
     * </p>
     *
     * @return the event's ID.
     */
    protected int getEventID() {
        return EVENT_ID_LOOKUP.computeIfAbsent(getClass(), ID_GENERATOR);
    }
}
