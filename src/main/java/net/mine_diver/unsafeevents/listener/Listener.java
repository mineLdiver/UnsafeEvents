package net.mine_diver.unsafeevents.listener;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.experimental.UtilityClass;
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
import java.util.function.Consumer;

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
    private static @NotNull CompositeListener createStatic(
            final @NotNull Class<?> listenerClass,
            final int priority
    ) {
        final @NotNull ImmutableList.Builder<SingularListener<?>> listeners = ImmutableList.builder();
        for (final @NotNull Method method : listenerClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventListener.class) || !Modifier.isStatic(method.getModifiers()))
                continue;
            final @NotNull EventListener eventListener = method.getAnnotation(EventListener.class);
            final @NotNull ListenerPriority listenerPriority = eventListener.priority();
            listeners.add(
                    Listener.reflection()
                            .method(method)
                            .priority(
                                    listenerPriority.custom ?
                                            eventListener.numPriority() == EventListener.DEFAULT_PRIORITY ?
                                                    priority :
                                                    eventListener.numPriority() :
                                            listenerPriority.numPriority
                            )
                            .build()
            );
        }
        return new SimpleCompositeListener(listeners.build(), priority);
    }

    @Builder(
            builderMethodName = "object",
            builderClassName = "ObjectListenerBuilder"
    )
    private static <T> @NotNull CompositeListener createObject(
            final @NotNull T listener,
            int priority
    ) {
        final @NotNull ImmutableList.Builder<SingularListener<?>> listeners = ImmutableList.builder();
        @Nullable Class<?> curClass = listener.getClass();
        while (curClass != null) {
            for (final @NotNull Method method : curClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(EventListener.class) || Modifier.isStatic(method.getModifiers()))
                    continue;
                final @NotNull EventListener eventListener = method.getAnnotation(EventListener.class);
                final @NotNull ListenerPriority listenerPriority = eventListener.priority();
                listeners.add(
                        Listener.reflection()
                                .listener(listener)
                                .method(method)
                                .priority(
                                        listenerPriority.custom ?
                                                eventListener.numPriority() == EventListener.DEFAULT_PRIORITY ?
                                                        priority :
                                                        eventListener.numPriority() :
                                                listenerPriority.numPriority
                                )
                                .build()
                );
            }
            curClass = curClass.getSuperclass();
        }
        return new SimpleCompositeListener(listeners.build(), priority);
    }

    @Builder(
            builderMethodName = "reflection",
            builderClassName = "ReflectionListenerBuilder"
    )
    private static <EVENT extends Event> @NotNull SingularListener<EVENT> createReflection(
            @Nullable Class<EVENT> eventType,
            final @Nullable Object listener,
            final @NotNull Method method,
            final int priority
    ) {
        if (method.getParameterCount() != 1) throw new InvalidMethodParameterCountException(String.format(
                "Method %s#%s has a wrong amount of parameters!",
                method.getDeclaringClass().getName(), method.getName()
        ));
        final @NotNull Class<?> rawEventType = method.getParameterTypes()[0]; // getting the method parameter type
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
        return new SimpleListener<>(
                eventType,
                ListenerExecutorFactory.create(listener, method, eventType), // creating a high performance executor for this method
                priority
        );
    }

    @Builder(
            builderMethodName = "simple",
            builderClassName = "SimpleListenerBuilder"
    )
    private static <EVENT extends Event> @NotNull SingularListener<EVENT> createSimple(
            @Nullable Class<EVENT> eventType,
            final @NotNull Consumer<EVENT> listener,
            final int priority
    ) {
        // resolving the event type from consumer's parameters
        final @NotNull Class<?> rawEventType = TypeResolver.resolveRawArgument(Consumer.class, listener.getClass()).asSubclass(Event.class);
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
        return new SimpleListener<>(
                eventType,
                listener,
                priority
        );
    }
}
