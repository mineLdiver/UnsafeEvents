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
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Interface defining a generic listener.
 *
 * <p>
 *     Provides a way to interact with
 *     a listener with unknown implementation
 *     as if it was {@linkplain SingularListener singular}.
 * </p>
 *
 * @see SingularListener
 * @see CompositeListener
 * @author mine_diver
 */
public interface GenericListener {
    /**
     * @return listener's phase. Can be any string.
     * Ordering of phases is defined individually for each
     * event type. "default" is default.
     * @see EventPhases#DEFAULT_PHASE
     */
    default String phase() {
        return EventPhases.DEFAULT_PHASE;
    }

    /**
     * @return listener's priority. Can be any integer number.
     * High priority - early execution. 0 is default.
     * @see ListenerPriority
     * @see EventListener#DEFAULT_PRIORITY
     */
    default int priority() {
        return EventListener.DEFAULT_PRIORITY;
    }

    /**
     * Accepts current listener to a consumer
     * as a {@link SingularListener},
     * exposing the listener implementation.
     *
     * <p>
     *     The consumer might be executed multiple
     *     times depending on implementation.
     *     For example, {@link CompositeListener} executes
     *     the consumer for each sub-listener it has.
     * </p>
     *
     * @param consumer the listener consumer.
     */
    void accept(final @NotNull Consumer<@NotNull SingularListener<?>> consumer);
}
