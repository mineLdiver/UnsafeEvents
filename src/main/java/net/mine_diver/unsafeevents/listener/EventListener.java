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
