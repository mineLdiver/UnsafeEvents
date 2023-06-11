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

package net.mine_diver.unsafeevents.util.exception;

import net.mine_diver.unsafeevents.eventbus.ManagedEventBus;
import org.jetbrains.annotations.NotNull;

/**
 * Created and stored when event dispatch gets disabled for a {@link ManagedEventBus}.
 *
 * <p>
 *     Used as a cause for {@link IllegalDispatchException},
 *     providing additional information, such as the stack trace
 *     of {@link ManagedEventBus#disableDispatch(String)}, which led to the
 *     illegal state in the first place.
 * </p>
 *
 * @see IllegalDispatchException
 * @author mine_diver
 */
public class DisabledDispatchCause extends Throwable {
    public DisabledDispatchCause(final @NotNull String message) {
        super(message);
    }
}
