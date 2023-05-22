package net.mine_diver.unsafeevents.listener;

import net.mine_diver.unsafeevents.event.EventPhases;

import java.lang.annotation.*;

/**
 * Indicates that a method should be registered
 * as a listener during bulk register and allows
 * to set listener priority.
 *
 * @author mine_diver
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventListener {
    /**
     * The default listener priority.
     *
     * <p>
     *     Should at all times be the same as {@link ListenerPriority#NORMAL#numPriority()}.
     *     Exists separately for the default value in {@link EventListener#numPriority()},
     *     as Java requires defaults to be constant.
     * </p>
     *
     * @see ListenerPriority
     */
    int DEFAULT_PRIORITY = 0;

    /**
     * Returns the event phase of this listener.
     *
     * <p>
     *     Can be any string. Ordering of phases
     *     is defined individually for each
     *     event type.
     * </p>
     *
     * @return the event phase of this listener.
     */
    String phase() default EventPhases.DEFAULT_PHASE;

    /**
     * Returns the enum priority of the listener.
     *
     * <p>
     *     If priority is set to {@link ListenerPriority#CUSTOM},
     *     the {@link EventListener#numPriority()} is used instead.
     * </p>
     *
     * @return the enum priority of the listener.
     */
    ListenerPriority priority() default ListenerPriority.CUSTOM;

    /**
     * Returns the numerical priority of the listener.
     *
     * <p>
     *     Only used if {@link EventListener#priority()} is set to
     *     {@link ListenerPriority#CUSTOM}.
     * </p>
     *
     * @return the numerical priority of the listener.
     */
    int numPriority() default DEFAULT_PRIORITY;
}
