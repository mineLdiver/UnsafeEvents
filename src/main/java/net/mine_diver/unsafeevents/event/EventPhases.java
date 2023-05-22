package net.mine_diver.unsafeevents.event;

import java.lang.annotation.*;

/**
 * Used for defining default phases
 * of an event type.
 *
 * <p>
 *     Inherited. If an event type extends
 *     another event type annotated with this,
 *     it inherits its phases, unless
 *     the sub event type annotates itself as well.
 * </p>
 *
 * <p>
 *     Phases can be changed by a third party
 *     via {@link PhaseOrdering#addPhaseOrdering(String, String)}.
 * </p>
 *
 * @author mine_diver
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EventPhases {
    /**
     * The default event phase.
     */
    String DEFAULT_PHASE = "default";

    /**
     * @return statically defined event's phases.
     * A phase can be any string. The phase ordering
     * is the same as defined in this array.
     */
    String[] value() default DEFAULT_PHASE;
}
