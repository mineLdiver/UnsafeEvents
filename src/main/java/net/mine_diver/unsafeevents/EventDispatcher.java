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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.mine_diver.unsafeevents.util.exception.DispatchException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event dispatcher with a listeners scope.
 *
 * @see MutableEventBus
 * @author mine_diver
 */
public interface EventDispatcher {
    /**
     * Event dispatch method.
     *
     * <p>
     *     Invokes all listeners of the specified event
     *     that are registered in the scope of this dispatcher.
     * </p>
     *
     * <p>
     *     After the dispatch, {@link Event#finish()} is executed,
     *     allowing the event to perform some post-dispatch action,
     *     for example, a clean up of event parameters.
     * </p>
     *
     * @param event the event to dispatch to this dispatcher's scope.
     * @param <EVENT> the event type.
     * @return the dispatched event.
     * @throws DispatchException if a listener throws during dispatch.
     */
    @Contract("_ -> param1")
    @CanIgnoreReturnValue
    default <EVENT extends Event> @NotNull EVENT post(final @NotNull EVENT event) {
        event.finish();
        return event;
    }
}
