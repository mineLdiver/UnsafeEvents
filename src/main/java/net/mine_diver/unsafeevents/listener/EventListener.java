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

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.*;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNullElse;
import static net.mine_diver.unsafeevents.event.EventPhases.DEFAULT_PHASE;

/**
 * Indicates that a method should be registered
 * as a listener during bulk register and allows
 * to set listener priority.
 *
 * <p>
 *     Can also be used for overriding default
 *     phase and priority on composite listeners
 *     such as {@link Listener#staticMethods()} and
 *     {@link Listener#object()}
 * </p>
 *
 * @author mine_diver
 */
@Target({
        ElementType.TYPE,
        ElementType.METHOD
})
@Inherited
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
    String phase() default DEFAULT_PHASE;

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

    /**
     * Common functions for extracting meaningful data from this annotation.
     *
     * @author mine_diver
     */
    final class Helper {
        private Helper() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }

        public static @NotNull String getPhase(final @NotNull EventListener listener) {
            return getPhase(listener, UnaryOperator.identity());
        }

        public static @NotNull String getPhase(final @NotNull EventListener listener, final @Nullable String customDefault) {
            return getPhase(listener, defaultPhase -> requireNonNullElse(customDefault, defaultPhase));
        }

        public static @NotNull String getPhase(final @NotNull EventListener listener, final @NotNull UnaryOperator<@NotNull String> defaultOverride) {
            val listenerPhase = listener.phase();
            return DEFAULT_PHASE.equals(listenerPhase) ? defaultOverride.apply(listenerPhase) : listenerPhase;
        }

        public static int getPriority(final @NotNull EventListener listener) {
            return getPriority(listener, Int2IntFunction.identity());
        }

        public static int getPriority(final @NotNull EventListener listener, final int customDefault) {
            return getPriority(listener, customPriority -> customPriority == EventListener.DEFAULT_PRIORITY ? customDefault : customPriority);
        }

        public static int getPriority(final @NotNull EventListener listener, final @NotNull Int2IntFunction customOverride) {
            val listenerPriority = listener.priority();
            return listenerPriority.custom ? customOverride.get(listener.numPriority()) : listenerPriority.numPriority;
        }
    }
}
