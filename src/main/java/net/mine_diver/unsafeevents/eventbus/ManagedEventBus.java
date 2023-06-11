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

package net.mine_diver.unsafeevents.eventbus;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.mine_diver.unsafeevents.Event;
import net.mine_diver.unsafeevents.EventBus;
import net.mine_diver.unsafeevents.util.exception.DisabledDispatchCause;
import net.mine_diver.unsafeevents.util.exception.DispatchException;
import net.mine_diver.unsafeevents.util.exception.IllegalDispatchException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * {@link EventBus} implementation that provides more
 * control over dispatch at a little efficiency cost.
 *
 * @author mine_diver
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
public class ManagedEventBus extends EventBus {
    /**
     * Whether dispatch is currently disabled.
     * False by default.
     */
    boolean dispatchDisabled;

    /**
     * A throwable containing a stack trace of {@link #disableDispatch(String)}
     * as a cause for {@link IllegalDispatchException}, so debugging
     * an illegal dispatch is easier.
     */
    DisabledDispatchCause disabledDispatchCause;

    /**
     * Disables dispatch.
     *
     * <p>
     *     Makes {@link #post(Event)} throw {@link IllegalDispatchException}
     *     with the cause being this method's stack trace.
     * </p>
     *
     * <p>
     *     Caller sensitive. Stores current stack trace, including caller class,
     *     for potential future reference if {@link #post(Event)}
     *     gets executed while disabled.
     * </p>
     *
     * <p>
     *     Useful for bulk registering listeners that aren't supposed
     *     to directly or indirectly dispatch an event during registration.
     * </p>
     *
     * @param reason the message used by {@link DisabledDispatchCause} for
     *               potential {@link IllegalDispatchException} throw.
     */
    public void disableDispatch(final @NotNull String reason) {
        dispatchDisabled = true;
        disabledDispatchCause = new DisabledDispatchCause(reason);
    }

    /**
     * Re-enables dispatch and clears disable stack trace.
     */
    public void enableDispatch() {
        dispatchDisabled = false;
        disabledDispatchCause = null;
    }

    /**
     * Managed dispatch.
     *
     * @param event the event to dispatch to this bus's listeners.
     * @return the dispatched event.
     * @param <EVENT> the event type.
     * @throws DispatchException if a listener throws during dispatch.
     * @throws IllegalDispatchException if dispatch was currently disabled.
     */
    @Override
    @Contract("_ -> param1")
    @CanIgnoreReturnValue
    public <EVENT extends Event> @NotNull EVENT post(final @NotNull EVENT event) {
        if (dispatchDisabled) throw new IllegalDispatchException(String.format(
                "Attempted to dispatch event %s when dispatch is disabled!",
                event.getClass().getName()
        ), disabledDispatchCause);
        return super.post(event);
    }
}
