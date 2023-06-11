/*
 * MIT License
 *
 * Copyright (c) 2023 mine_diver
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.mine_diver.unsafeevents;

import com.google.common.collect.ObjectArrays;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import net.mine_diver.unsafeevents.event.EventPhases;
import net.mine_diver.unsafeevents.event.PhaseOrdering;
import net.mine_diver.unsafeevents.event.PhaseOrderingInvalidationEvent;
import net.mine_diver.unsafeevents.listener.*;
import net.mine_diver.unsafeevents.util.Util;
import net.mine_diver.unsafeevents.util.collection.Int2ReferenceArrayMapWrapper;
import net.mine_diver.unsafeevents.util.exception.DispatchException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * The basic {@link EventBus} implementation.
 *
 * <p>
 *     This implementation is usually the fastest for dispatching events and registering listeners.
 * </p>
 *
 * <p>
 *     {@link EventBus} is designed to contain listeners separate from each other,
 *     allowing to have separate sets of listeners for each event per bus.
 *     Unless registered in both buses, listeners aren't shared between them.
 * </p>
 *
 * <p>
 *     Allows extending for other implementations.
 * </p>
 *
 * @see EventListener
 * @see Event
 * @see Listener
 * @author mine_diver
 */
@FieldDefaults(
        level = AccessLevel.PROTECTED,
        makeFinal = true
)
public class EventBus implements MutableEventBus, AutoCloseable {
    /**
     * The mutable access to {@link #MANAGEMENT_BUS}.
     *
     * <p>
     *     Used for registering event buses on instance creation.
     * </p>
     */
    private static final @NotNull MutableEventBus MUTABLE_MANAGEMENT_BUS = new EventBus(true);

    /**
     * The management event bus.
     *
     * <p>
     *     Used for keeping track of all {@link EventBus} instances
     *     and dispatching management events to them,
     *     such as {@link PhaseOrderingInvalidationEvent}.
     * </p>
     */
    public static final @NotNull EventDispatcher MANAGEMENT_BUS = MUTABLE_MANAGEMENT_BUS;

    /**
     * {@link DeadEvent} fallback.
     *
     * <p>
     *     This is the default listener for {@link DeadEvent},
     *     made to prevent recursion when there's no {@link DeadEvent} listeners registered.
     * </p>
     */
    protected static final @NotNull Consumer<@NotNull Event> DEAD_EVENT_FALLBACK = event -> { /* no need for implementation, empty */ };

    /**
     * Default listener for all events.
     *
     * <p>
     *     When there's no listeners registered for an event,
     *     this listener is executed instead to notify all {@link DeadEvent} listeners.
     * </p>
     */
    @NotNull Consumer<@NotNull Event> DEAD_EVENT = event -> {
        // setting up DeadEvent
        val deadEvent = DeadEvent.INSTANCE;
        deadEvent.event = event;
        // dispatching
        post(deadEvent);
    };

    /**
     * {@link Reference2ReferenceMap} containing this bus's listeners.
     * Key is the event type, value is the array of listeners.
     *
     * <p>
     *     Used for generating dispatch registries with {@link ListenerRegistryFactory},
     *     not used for dispatching listeners directly due to low performance
     *     of iterating over an array and looking up a value in a map.
     * </p>
     *
     * @see SingularListener
     */
    @NotNull Reference2ReferenceMap<Class<? extends Event>, @NotNull SingularListener<?> @NotNull []> listeners = new Reference2ReferenceOpenHashMap<>();

    /**
     * The field containing high performance registries generated by {@link ListenerRegistryFactory}.
     *
     * <p>
     *     The index is the event ID, the element is the registry.
     * </p>
     *
     * <p>
     *     All interactions with this array (except for getting an element)
     *     must be done through {@link #registries} array map wrapper,
     *     as it contains the necessary safeguards and array reallocation functionality needed to operate
     *     with this similar to a {@link Int2ReferenceMap} but with better performance and
     *     JIT inlining capabilities.
     * </p>
     *
     * <p>
     *     The default element of this array is {@link #DEAD_EVENT}.
     *     The {@link DeadEvent} registry is set to {@link #DEAD_EVENT_FALLBACK} by default.
     * </p>
     *
     * @see #registries
     * @see Int2ReferenceArrayMapWrapper
     */
    @SuppressWarnings("unchecked")
    @NonFinal @NotNull Consumer<? extends @NotNull Event> @NotNull [] registriesArray = new Consumer[0];

    /**
     * Array map wrapper of {@link #registriesArray}.
     *
     * <p>
     *     Used for map-like interaction with {@link #registriesArray}.
     * </p>
     *
     * <p>
     *     Default value is {@link #DEAD_EVENT}, initial capacity is set to {@link DeadEvent#ID} + 1.
     *     The {@link DeadEvent} registry is set to {@link #DEAD_EVENT_FALLBACK} by default.
     * </p>
     *
     * @see #registriesArray
     * @see Int2ReferenceArrayMapWrapper
     */
    @NotNull Int2ReferenceArrayMapWrapper<@NotNull Consumer<? extends @NotNull Event>> registries = Util.make(new Int2ReferenceArrayMapWrapper<>(
            () -> registriesArray,                                                        // the array getter
            arr -> registriesArray = arr,                                                 // the array setter
            (IntFunction<Consumer<? extends @NotNull Event> @NotNull []>) Consumer[]::new,          // the array constructor
            DEAD_EVENT, DeadEvent.ID + 1
    ), wrapper -> wrapper.put(DeadEvent.ID, DEAD_EVENT_FALLBACK));

    /**
     * {@link ReferenceSet} of event types under which high performance registries
     * inside {@link #registriesArray} do not match raw
     * {@link #listeners} and require recompilation.
     * 
     * <p>
     *     This is done to allow for lazy recompilation of high performance registries
     *     during event dispatching, saving memory and CPU operations during
     *     registration of new listeners.
     * </p>
     * 
     * @see #post(Event)
     * @see #invalidated
     * @see #compileRegistries()
     */
    @NotNull ReferenceSet<Class<? extends Event>> invalidatedRegistries = new ReferenceOpenHashSet<>();

    /**
     * Indicates that there were new listeners registered and {@link #invalidatedRegistries} is not empty.
     *
     * <p>
     *     This allows for a high performance check during event dispatching.
     * </p>
     *
     * @see #post(Event)
     * @see #compileRegistries()
     * @see #invalidatedRegistries
     */
    @NonFinal boolean invalidated;

    /**
     * The {@link #MANAGEMENT_BUS} listener of this event bus.
     *
     * <p>
     *     Used for unregistering the event bus
     *     from {@link #MANAGEMENT_BUS} on
     *     discard through {@link #close()}.
     * </p>
     */
    private CompositeListener managedEntry;

    /**
     * Default constructor.
     */
    public EventBus() {
        this(false);
    }

    /**
     * Internal constructor used for {@link #MANAGEMENT_BUS} initialization.
     *
     * @param isManagementBus whether it's the management bus itself.
     */
    private EventBus(final boolean isManagementBus) {
        (isManagementBus ? this : MUTABLE_MANAGEMENT_BUS).register(
                managedEntry = Listener.object()
                        .listener(this)
                        .build()
        );
    }

    /**
     * Registers a singular listener to this event bus's scope.
     *
     * @param listener the listener to register.
     * @param <EVENT> the event type the listener accepts.
     * @see Listener
     * @see SingularListener
     */
    @Override
    public <EVENT extends Event> void register(final @NotNull SingularListener<@NotNull EVENT> listener) {
        Class<EVENT> eventType = listener.eventType();
        // putting the listener into raw listeners array
        listeners.compute(
                eventType,
                (id, containers) -> containers == null ?
                        new SingularListener<?>[] { listener } :
                        ObjectArrays.concat(containers, listener)
        );
        // invalidating the state to schedule a recompile of high performance registries during the next event dispatch
        invalidatedRegistries.add(eventType);
        invalidated = true;
    }

    /**
     * Unregisters a singular listener from this event bus's scope.
     *
     * <p>
     *     The listener instance has to be the same one
     *     that was originally registered in this event bus.
     * </p>
     *
     * @param listener the listener to unregister.
     * @param <EVENT> the event type the listener accepts.
     */
    @Override
    public <EVENT extends Event> void unregister(@NotNull SingularListener<@NotNull EVENT> listener) {
        Class<EVENT> eventType = listener.eventType();
        listeners.compute(
                eventType,
                (id, eventListeners) -> {
                    if (eventListeners != null) {
                        int indexToRemove = -1;
                        for (int i = 0; i < eventListeners.length; i++)
                            if (eventListeners[i] == listener) {
                                indexToRemove = i;
                                break;
                            }
                        if (indexToRemove > -1) {
                            SingularListener<?>[] newArray = new SingularListener[eventListeners.length - 1];
                            System.arraycopy(eventListeners, 0, newArray, 0, indexToRemove);
                            System.arraycopy(eventListeners, indexToRemove + 1, newArray, indexToRemove, eventListeners.length - indexToRemove - 1);
                            return newArray;
                        }
                    }
                    throw new IllegalArgumentException("The event bus doesn't contain this listener!");
                }
        );
        // invalidating the state to schedule a recompile of high performance registries during the next event dispatch
        invalidatedRegistries.add(eventType);
        invalidated = true;
    }

    /**
     * Compiles high performance registries that are invalidated through {@link #invalidatedRegistries}.
     *
     * @see #post(Event)
     * @see #invalidated
     * @see #invalidatedRegistries
     */
    protected void compileRegistries() {
        invalidatedRegistries.iterator().forEachRemaining(this::compileRegistry);
        // validating the state
        invalidatedRegistries.clear();
        invalidated = false;
    }

    /**
     * Compiles a high-performance registry for the specified event type.
     *
     * <p>
     *     During the compilation process, the listener containers for the specified event
     *     type are sorted according to the event type's phase ordering using the
     *     {@link PhaseOrdering#sort(SingularListener[])} method. Then,
     *     if there's more than 1 listener in the list, the sorted list of
     *     listeners is used to create a listener registry using the
     *     {@link ListenerRegistryFactory#create(Consumer[])} method.
     *     Otherwise, the listener itself is used as a registry.
     * </p>
     *
     * @param eventType the event type to compile the registry for.
     * @param <EVENT> the event type to compile the registry for.
     * @see #compileRegistries()
     * @see PhaseOrdering#sort(SingularListener[])
     * @see ListenerRegistryFactory#create(Consumer[])
     */
    private <EVENT extends Event> void compileRegistry(Class<EVENT> eventType) {
        //noinspection unchecked
        final SingularListener<EVENT>[] listenerContainers = (SingularListener<EVENT>[]) listeners.get(eventType);
        PhaseOrdering.of(eventType).sort(listenerContainers);
        //noinspection unchecked
        registries.put(
                Event.getEventID(eventType),
                listenerContainers.length == 1 ?
                        listenerContainers[0].listener() :
                        ListenerRegistryFactory.create(
                                Arrays.stream(listenerContainers)
                                        .map(SingularListener::listener)
                                        .toArray(Consumer[]::new)
                        )
        );
    }

    /**
     * Management method. Shouldn't be called directly.
     *
     * <p>
     *     Invalidates current ordering of an event's listeners.
     *     Only invoked if an event's phase ordering was changed
     *     by a third party. {@link EventPhases}-defined phases don't
     *     need this.
     * </p>
     *
     * @param event the phase ordering invalidation event.
     * @see PhaseOrdering#addPhaseOrdering(String, String)
     */
    @EventListener
    private void onOrderingInvalidation(PhaseOrderingInvalidationEvent event) {
        if (listeners.containsKey(event.getEventType())) {
            invalidatedRegistries.add(event.getEventType());
            invalidated = true;
        }
    }

    /**
     * Used for freeing this bus's instance.
     *
     * <p>
     *     Only useful for short-living buses.
     *     Buses that exist throughout
     *     the entire lifecycle of the application
     *     don't really need to call this ever.
     * </p>
     *
     * <p>
     *     The bus shouldn't be used after this
     *     method is invoked, as it may lead
     *     to unexpected behavior.
     * </p>
     */
    @Override
    public void close() {
        MUTABLE_MANAGEMENT_BUS.unregister(managedEntry);
        listeners.clear();
        //noinspection unchecked
        registriesArray = new Consumer[0];
    }

    /**
     * Event dispatch method.
     *
     * <p>
     *     Invokes all listeners of the specified event through a high performance registry.
     * </p>
     *
     * <p>
     *     If the bus state is invalidated, the invalidated high performance registries are recompiled.
     *     If the event ID exceeds {@link #registriesArray} length, the array is resized.
     *     Both checks are simple and don't add too much overhead to the dispatch.
     * </p>
     *
     * <p>
     *     After the dispatch, {@link Event#finish()} is executed,
     *     allowing the event to perform some post-dispatch action,
     *     for example, a clean up of event parameters.
     * </p>
     *
     * @param event the event to dispatch to this bus's listeners.
     * @return the dispatched event.
     * @param <EVENT> the event type.
     * @throws DispatchException if a listener throws during dispatch.
     */
    @Override
    @Contract("_ -> param1")
    @CanIgnoreReturnValue
    public <EVENT extends Event> @NotNull EVENT post(final @NotNull EVENT event) {
        if (invalidated) compileRegistries(); // compiling high performance registries if the state is invalidated
        val eventId = event.getEventID();
        if (eventId >= registriesArray.length) registries.resizeArray(eventId + 1); // resizing the array to fit the new event id
        try {
            //noinspection unchecked
            ((Consumer<EVENT>) registriesArray[eventId]).accept(event); // dispatch
        } catch (final Throwable throwable) {
            throw new DispatchException(String.format(
                    "An exception occurred during a dispatch of %s to %s",
                    event, this
            ), throwable);
        }
        return MutableEventBus.super.post(event); // performing a finalization and returning the event to allow for a one line check of a parameter in the event
    }



    // DEPRECATED

    /**
     * Registers only static methods annotated with {@link EventListener}
     * in the specified class as listeners in this bus.
     *
     * @deprecated Use {@link #register(GenericListener)} with {@link Listener#staticMethods()} instead.
     *
     * @param listenerClass the class containing static {@link EventListener} methods.
     * @throws IllegalArgumentException if an {@link EventListener} method in the hierarchy
     *                                  has no or more than 1 parameter, or if the method parameter is not an event
     */
    @Deprecated
    public void register(
            final @NotNull Class<?> listenerClass
    ) {
        register(
                Listener.staticMethods()
                        .listener(listenerClass)
                        .build()
        );
    }

    /**
     * Registers only non-static methods annotated with {@link EventListener}
     * in the class of the specified object.
     *
     * <p>
     *     The listeners are only registered under this object's instance.
     * </p>
     *
     * @deprecated Use {@link #register(GenericListener)} with {@link Listener#object()} instead.
     *
     * @param listener the object which class contains non-static {@link EventListener} methods.
     * @throws IllegalArgumentException if an {@link EventListener} method in the hierarchy
     *                                  has no or more than 1 parameter, or if the method parameter is not an event
     */
    @Deprecated
    public void register(
            final @NotNull Object listener
    ) {
        register(
                Listener.object()
                        .listener(listener)
                        .build()
        );
    }

    /**
     * Registers this object's methods from a class somewhere in the hierarchy of this object.
     *
     * <p>
     *     Allows to limit the bus to only registering methods starting from a specific point in the hierarchy of this object.
     *     For example, class B extends class A. If {@code register(A.class, new B())} is run,
     *     only the {@link EventListener} methods of class A are going to be registered, but associated to an instance of B.
     *     If {@code register(B.class, new B())} is run, both class A and class B {@link EventListener} methods are going to
     *     be registered.
     * </p>
     *
     * <p>
     *     If listener instance is null, only static {@link EventListener} methods are going to be registered,
     *     otherwise static methods are ignored and only non-static methods are registered.
     * </p>
     *
     * @deprecated Use {@link #register(GenericListener)} with {@link Listener#staticMethods()} or {@link Listener#object()} instead.
     *
     * @param listenerClass the listener class starting from which in the hierarchy the {@link EventListener} methods are registered.
     * @param listener the listener object. If null, static {@link EventListener} methods are registered instead.
     * @param <T> the instance type.
     * @param <U> a child type of instance, allows to specify higher hierarchy types, including {@link T}, for listener class type.
     * @throws IllegalArgumentException if an {@link EventListener} method in the hierarchy
     *                                  has no or more than 1 parameter, or if the method parameter is not an event
     */
    @Deprecated
    public <T, U extends T> void register(
            final @NotNull Class<? super U> listenerClass,
            final @Nullable T listener
    ) {
        register(
                listener == null ?
                        Listener.staticMethods()
                                .listener(listenerClass)
                                .build() :
                        Listener.object()
                                .listener(listener)
                                .build()
        );
    }

    /**
     * Registers a static {@link EventListener} {@link Method} with {@link EventListener#DEFAULT_PRIORITY} priority.
     *
     * @deprecated Use {@link #register(SingularListener)} with {@link Listener#reflection()} instead.
     *
     * @param method the static {@link EventListener} method to register as a listener.
     * @throws IllegalArgumentException if the method has no or more than 1 parameter,
     *                                  or if the method parameter is not an event
     */
    @Deprecated
    public void register(
            final @NotNull Method method
    ) {
        register(
                Listener.reflection()
                        .method(method)
                        .build()
        );
    }

    /**
     * Registers a static {@link EventListener} {@link Method} with a custom priority.
     *
     * @deprecated Use {@link #register(SingularListener)} with {@link Listener#reflection()} instead.
     *
     * @param method the static {@link EventListener} method to register as a listener.
     * @param priority the priority to assign to this listener.
     * @throws IllegalArgumentException if the method has no or more than 1 parameter,
     *                                  or if the method parameter is not an event
     * @see ListenerPriority
     */
    @Deprecated
    public void register(
            final @NotNull Method method,
            final int priority
    ) {
        register(
                Listener.reflection()
                        .method(method)
                        .priority(priority)
                        .build()
        );
    }

    /**
     * Registers an {@link EventListener} {@link Method} with {@link EventListener#DEFAULT_PRIORITY} priority.
     *
     * <p>
     *     If listener instance is null, the {@link EventListener} method is registered as static.
     * </p>
     *
     * @deprecated Use {@link #register(SingularListener)} with {@link Listener#reflection()} instead.
     *
     * @param method the {@link EventListener} method to register as a listener.
     * @param listener the listener object. If null, the method is registered as static.
     * @throws IllegalArgumentException if the method has no or more than 1 parameter,
     *                                  or if the method parameter is not an event
     */
    @Deprecated
    public void register(
            final @NotNull Method method,
            final @Nullable Object listener
    ) {
        register(
                Listener.reflection()
                        .method(method)
                        .listener(listener)
                        .build()
        );
    }

    /**
     * Registers an {@link EventListener} {@link Method} with a custom priority.
     *
     * <p>
     *     If listener instance is null, the {@link EventListener} method is registered as static.
     * </p>
     *
     * @deprecated Use {@link #register(SingularListener)} with {@link Listener#reflection()} instead.
     *
     * @param method the {@link EventListener} method to register as a listener.
     * @param listener the listener object. If null, the method is registered as static.
     * @param priority the priority to assign to this listener.
     * @param <EVENT> the event type.
     * @throws IllegalArgumentException if the method has no or more than 1 parameter,
     *                                  or if the method parameter is not an event
     */
    @Deprecated
    public <EVENT extends Event> void register(
            final @NotNull Method method,
            final @Nullable Object listener,
            final int priority
    ) {
        register(
                Listener.reflection()
                        .method(method)
                        .listener(listener)
                        .priority(priority)
                        .build()
        );
    }

    /**
     * Registers an {@link Event} {@link Consumer} as a listener with {@link EventListener#DEFAULT_PRIORITY} priority.
     *
     * <p>
     *     This method automatically resolves the event type from the consumer's parameters.
     * </p>
     *
     * @deprecated Use {@link #register(SingularListener)} with {@link Listener#simple()} instead.
     *
     * @param listener the consumer to register as a listener.
     * @param <EVENT> the event type.
     */
    @Deprecated
    public <EVENT extends Event> void register(
            final @NotNull Consumer<@NotNull EVENT> listener
    ) {
        register(
                Listener.<EVENT>simple()
                        .listener(listener)
                        .build()
        );
    }

    /**
     * Registers an {@link Event} {@link Consumer} as a listener with a custom priority.
     *
     * <p>
     *     This method automatically resolves the event type from the consumer's parameters.
     * </p>
     *
     * @deprecated Use {@link #register(SingularListener)} with {@link Listener#simple()} instead.
     *
     * @param listener the consumer to register as a listener.
     * @param priority the priority to assign to this listener.
     * @param <EVENT> the event type.
     */
    @Deprecated
    public <EVENT extends Event> void register(
            final @NotNull Consumer<@NotNull EVENT> listener,
            final int priority
    ) {
        register(
                Listener.<EVENT>simple()
                        .listener(listener)
                        .priority(priority)
                        .build()
        );
    }

    /**
     * Registers an {@link Event} {@link Consumer} as a listener with {@link EventListener#DEFAULT_PRIORITY} priority.
     *
     * @deprecated Use {@link #register(SingularListener)} with {@link Listener#simple()} instead.
     *
     * @param eventType the event type class. Skips the automatic resolving.
     * @param listener the consumer to register as a listener.
     * @param <EVENT> the event type.
     */
    @Deprecated
    public <EVENT extends Event> void register(
            final @NotNull Class<EVENT> eventType,
            final @NotNull Consumer<@NotNull EVENT> listener
    ) {
        register(
                Listener.<EVENT>simple()
                        .eventType(eventType)
                        .listener(listener)
                        .build()
        );
    }

    /**
     * Registers an {@link Event} {@link Consumer} as a listener with a custom priority.
     *
     * @deprecated Use {@link #register(SingularListener)} with {@link Listener#simple()} instead.
     *
     * @param eventType the event type class. Skips the automatic resolving.
     * @param listener the consumer to register as a listener.
     * @param priority the priority to assign to this listener.
     * @param <EVENT> the event type.
     */
    @Deprecated
    public <EVENT extends Event> void register(
            final @NotNull Class<EVENT> eventType,
            final @NotNull Consumer<@NotNull EVENT> listener,
            final int priority
    ) {
        register(
                Listener.<EVENT>simple()
                        .eventType(eventType)
                        .listener(listener)
                        .priority(priority)
                        .build()
        );
    }
}
