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
