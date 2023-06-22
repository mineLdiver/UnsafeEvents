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

package net.mine_diver.unsafeevents.listener;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.jodah.typetools.TypeResolver;
import net.mine_diver.unsafeevents.Event;
import net.mine_diver.unsafeevents.EventBus;
import net.mine_diver.unsafeevents.util.exception.listener.IncompatibleEventTypesException;
import net.mine_diver.unsafeevents.util.exception.listener.InvalidMethodParameterCountException;
import net.mine_diver.unsafeevents.util.exception.listener.InvalidMethodParameterTypeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNullElse;
import static net.mine_diver.unsafeevents.event.EventPhases.DEFAULT_PHASE;
import static net.mine_diver.unsafeevents.listener.EventListener.DEFAULT_PRIORITY;
import static net.mine_diver.unsafeevents.listener.EventListener.Helper.getPhase;
import static net.mine_diver.unsafeevents.listener.EventListener.Helper.getPriority;

/**
 * Default listener builders for {@link EventBus}.
 *
 * <li>Static methods</li>
 * <ul>
 *     Registers class's static methods annotated with
 *     {@link EventListener} as reflection
 *     {@linkplain SingularListener singular listeners}.
 * </ul>
 *
 * <li>Object</li>
 * <ul>
 *     Registers object's non-static methods annotated with
 *     {@link EventListener} as reflection
 *     {@linkplain SingularListener singular listeners}
 *     (including methods from super classes).
 * </ul>
 *
 * <li>Reflection</li>
 * <ul>
 *     Registers a {@linkplain Method reflection method} as
 *     a simple {@linkplain SingularListener singular listener}.
 *     <p>
 *         Can be both static and instance methods. For static,
 *         simply don't pass the listener instance.
 *     </p>
 *     <p>
 *         If the event type isn't specified, it's automatically
 *         inferred from the method's arguments. On the other hand,
 *         if the event type is specified, the method's actual argument
 *         can be any super type of the specified event type.
 *     </p>
 *     <p>
 *         Reflection isn't actually used for the listener invocation.
 *         Instead, a direct method accessor is generated via ASM,
 *         providing fast invocation.
 *     </p>
 * </ul>
 *
 * <li>Simple</li>
 * <ul>
 *     Registers an {@linkplain Consumer event consumer} as
 *     a simple {@linkplain SingularListener singular listener}.
 *     <p>
 *         Simplest form of a listener. Can be implemented
 *         in any way, either through a lambda, or a method
 *         reference, or a class that implements {@linkplain Consumer}.
 *     </p>
 *     <p>
 *         If the event type isn't specified, it's automatically
 *         inferred from the consumer's parameter. On the other hand,
 *         if the event type is specified, the consumer's actual parameter
 *         can be any super type of the specified event type.
 *     </p>
 * </ul>
 */
@UtilityClass
public class Listener {
    @Builder(
            builderMethodName = "staticMethods",
            builderClassName = "StaticListenerBuilder"
    )
    private @NotNull CompositeListener createStatic(
            final @NotNull Class<?> listener,
            final @Nullable String phase,
            final int priority
    ) {
        final String defaultPhase;
        final int defaultPriority;
        if (listener.isAnnotationPresent(EventListener.class)) {
            val eventListener = listener.getAnnotation(EventListener.class);
            defaultPhase = getPhase(eventListener, phase);
            defaultPriority = getPriority(eventListener, priority);
        } else {
            defaultPhase = requireNonNullElse(phase, DEFAULT_PHASE);
            defaultPriority = priority;
        }
        val listeners = ImmutableList.<SingularListener<?>>builder();
        for (val method : listener.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventListener.class) || !Modifier.isStatic(method.getModifiers()))
                continue;
            listeners.add(
                    Listener.reflection()
                            .method(method)
                            .phase(defaultPhase)
                            .priority(defaultPriority)
                            .build()
            );
        }
        return new SimpleCompositeListener(listeners.build(), defaultPhase, defaultPriority);
    }

    @Builder(
            builderMethodName = "object",
            builderClassName = "ObjectListenerBuilder"
    )
    private <T> @NotNull CompositeListener createObject(
            final @NotNull T listener,
            @Nullable String phase,
            int priority
    ) {
        val listeners = ImmutableList.<SingularListener<?>>builder();
        val classDeque = new ArrayDeque<Class<?>>();
        val listenerClass = listener.getClass();
        @Nullable var curClass = listenerClass;
        while (curClass != null) {
            classDeque.push(curClass);
            curClass = curClass.getSuperclass();
        }
        @NotNull var defaultPhase = DEFAULT_PHASE;
        var defaultPriority = DEFAULT_PRIORITY;
        while (!classDeque.isEmpty()) {
            curClass = classDeque.pop();
            @Nullable val compositeListener = curClass.getAnnotation(EventListener.class);
            if (curClass == listenerClass) {
                if (compositeListener != null) {
                    defaultPhase = getPhase(compositeListener, phase);
                    defaultPriority = getPriority(compositeListener, priority);
                } else {
                    defaultPhase = requireNonNullElse(phase, DEFAULT_PHASE);
                    defaultPriority = priority;
                }
            } else if (compositeListener != null) {
                defaultPhase = compositeListener.phase();
                defaultPriority = getPriority(compositeListener);
            }
            for (val method : curClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(EventListener.class) || Modifier.isStatic(method.getModifiers()))
                    continue;
                listeners.add(
                        Listener.reflection()
                                .listener(listener)
                                .method(method)
                                .phase(defaultPhase)
                                .priority(defaultPriority)
                                .build()
                );
            }
        }
        return new SimpleCompositeListener(listeners.build(), defaultPhase, defaultPriority);
    }

    @Builder(
            builderMethodName = "reflection",
            builderClassName = "ReflectionListenerBuilder"
    )
    private <EVENT extends Event> @NotNull SingularListener<@NotNull EVENT> createReflection(
            @Nullable Class<EVENT> eventType,
            final @Nullable Object listener,
            final @NotNull Method method,
            final @Nullable String phase,
            final int priority
    ) {
        if (method.getParameterCount() != 1) throw new InvalidMethodParameterCountException(String.format(
                "Method %s#%s has a wrong amount of parameters!",
                method.getDeclaringClass().getName(), method.getName()
        ));
        val rawEventType = method.getParameterTypes()[0]; // getting the method parameter type
        if (eventType == null) {
            if (!Event.class.isAssignableFrom(rawEventType))
                throw new InvalidMethodParameterTypeException(String.format(
                        "Method %s#%s's parameter type (%s) is not an event!",
                        method.getDeclaringClass().getName(), method.getName(), rawEventType.getName()
                ));
            //noinspection unchecked
            eventType = (Class<EVENT>) rawEventType.asSubclass(Event.class); // casting the method parameter type to the event type
        } else if (!rawEventType.isAssignableFrom(eventType)) throw new IncompatibleEventTypesException(String.format(
                "Method %s#%s's parameter type (%s) is not assignable from the passed event type (%s)!",
                method.getDeclaringClass().getName(), method.getName(), rawEventType.getName(), eventType.getName()
        ));
        final String listenerPhase;
        final int listenerPriority;
        if (method.isAnnotationPresent(EventListener.class)) {
            val eventListener = method.getAnnotation(EventListener.class);
            listenerPhase = getPhase(eventListener, phase);
            listenerPriority = getPriority(eventListener, priority);
        } else {
            listenerPhase = requireNonNullElse(phase, DEFAULT_PHASE);
            listenerPriority = priority;
        }
        return new SimpleSingularListener<>(
                eventType,
                ListenerExecutorFactory.create(listener, method, eventType), // creating a high performance executor for this method
                listenerPhase,
                listenerPriority
        );
    }

    @Builder(
            builderMethodName = "simple",
            builderClassName = "SimpleListenerBuilder"
    )
    private <EVENT extends Event> @NotNull SingularListener<@NotNull EVENT> createSimple(
            @Nullable Class<EVENT> eventType,
            final @NotNull Consumer<@NotNull EVENT> listener,
            final @Nullable String phase,
            final int priority
    ) {
        // resolving the event type from consumer's parameters
        val rawEventType = TypeResolver.resolveRawArgument(Consumer.class, listener.getClass()).asSubclass(Event.class);
        if (eventType == null) {
            if (!Event.class.isAssignableFrom(rawEventType))
                throw new InvalidMethodParameterTypeException(String.format(
                        "Consumer %s's parameter type (%s) is not an event!",
                        listener.getClass().getName(), rawEventType.getName()
                ));
            //noinspection unchecked
            eventType = (Class<EVENT>) rawEventType.asSubclass(Event.class); // casting the consumer parameter type to the event type
        } else if (!rawEventType.isAssignableFrom(eventType)) throw new IncompatibleEventTypesException(String.format(
                "Consumer %s's parameter type (%s) is not assignable from the passed event type (%s)!",
                listener.getClass().getName(), rawEventType.getName(), eventType.getName()
        ));
        return new SimpleSingularListener<>(
                eventType,
                listener,
                requireNonNullElse(phase, DEFAULT_PHASE),
                priority
        );
    }
}
