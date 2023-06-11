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

import net.mine_diver.unsafeevents.listener.CompositeListener;
import net.mine_diver.unsafeevents.listener.GenericListener;
import net.mine_diver.unsafeevents.listener.Listener;
import net.mine_diver.unsafeevents.listener.SingularListener;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event dispatcher whose listeners scope
 * can be mutated with the defined methods.
 *
 * @author mine_diver
 */
public interface MutableEventBus extends EventDispatcher {
    /**
     * Registers a generic listener to this event bus's scope.
     *
     * @param listener the listener to register.
     * @see Listener
     * @see CompositeListener
     * @see SingularListener
     */
    default void register(final @NotNull GenericListener listener) {
        listener.accept(this::register);
    }

    /**
     * Registers a singular listener to this event bus's scope.
     *
     * @param listener the listener to register.
     * @param <EVENT> the event type the listener accepts.
     * @see Listener
     * @see SingularListener
     */
    <EVENT extends Event> void register(final @NotNull SingularListener<@NotNull EVENT> listener);

    /**
     * Unregisters a generic listener from this event bus's scope.
     *
     * <p>
     *     The listener instance has to be the same one
     *     that was originally registered in this event bus.
     * </p>
     *
     * @param listener the listener to unregister.
     */
    default void unregister(final @NotNull GenericListener listener) {
        listener.accept(this::unregister);
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
    <EVENT extends Event> void unregister(final @NotNull SingularListener<@NotNull EVENT> listener);
}
